package com.epam.client.controller;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Durations;
import org.testcontainers.utility.DockerImageName;

import com.epam.api.dto.OrderDto;
import com.epam.client.TestDataProvider;
import com.epam.client.dispatcher.OrderHandler;
import com.epam.client.model.Order;
import com.epam.client.repository.OrderRepository;
import com.epam.client.service.OrderService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class OrderControllerTest {

    private static final String URL_TEMPLATE = "/api/v1/orders";

    private static final String TEST_TOPIC = "test-orders";

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag("5.4.3"));

    @Container
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo").withExposedPorts(27017);

    @Autowired
    private WebTestClient webTestClient;

    @SpyBean
    private OrderService orderService;

    @SpyBean
    private OrderRepository orderRepository;

    @SpyBean
    private OrderHandler orderHandler;

    private KafkaConsumer<String, OrderDto> kafkaConsumer;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeTestClass
    public void setupTest() {
        kafkaContainer.waitingFor(Wait.forListeningPort()).start();
        mongoDBContainer.start();
        mongoDBContainer.waitingFor(Wait.forListeningPort()
                .withStartupTimeout(Duration.ofSeconds(180L)));
    }

    @AfterAll
    static void tearDown() {
        kafkaContainer.stop();
        mongoDBContainer.stop();
    }

    @BeforeEach
    void init() {
        Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConsumer = new KafkaConsumer<>(kafkaProperties, new StringDeserializer(), new JsonDeserializer<>(
                OrderDto.class, false));
        kafkaConsumer.subscribe(List.of(TEST_TOPIC));
    }

    @Test
    void testPlaceOrder_Created() {
        final var placeOrderRequestDto = TestDataProvider.getPlaceOrderRequestDtoStub();
        final var orderDto = TestDataProvider.getOrderDtoStub(UUID.randomUUID().toString());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path((URL_TEMPLATE)).build())
                .bodyValue(placeOrderRequestDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated();

        verify(orderService).placeOrder(placeOrderRequestDto);
        verify(orderRepository).save(orderCaptor.capture());
        final var orderId = orderCaptor.getValue().getId();
        orderDto.setId(orderId);
        verify(orderHandler).sendOrder(orderDto);

        final var consumerRecords = kafkaConsumer.poll(Durations.TEN_SECONDS);
        final boolean contains = consumerRecords.records(new TopicPartition(TEST_TOPIC, 0)).stream()
                .anyMatch(consumerRecord -> orderId.equals(consumerRecord.key())
                        && orderDto.equals(consumerRecord.value()));

        assertTrue(contains);
    }

    @Test
    void testPlaceOrder_BadRequest() {
        final var placeOrderRequestDto = TestDataProvider.getPlaceOrderRequestDtoStub();
        placeOrderRequestDto.getPizzas().get(0).setName(null);

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path((URL_TEMPLATE)).build())
                .bodyValue(placeOrderRequestDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

}
