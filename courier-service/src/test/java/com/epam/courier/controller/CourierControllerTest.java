package com.epam.courier.controller;

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
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Durations;
import org.testcontainers.utility.DockerImageName;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.courier.dispatcher.NotificationHandler;
import com.epam.courier.service.CourierService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
class CourierControllerTest {

    private static final String URL_TEMPLATE = "/api/v1/couriers";

    private static final String TEST_TOPIC = "test-notifications";

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag("5.4.3"));

    @Autowired
    private WebTestClient webTestClient;

    @SpyBean
    private CourierService courierService;

    @SpyBean
    private NotificationHandler notificationHandler;

    private KafkaConsumer<String, OrderStatusDto> kafkaConsumer;

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.stream.kafka.binder.brokers", kafkaContainer::getBootstrapServers);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @BeforeTestClass
    public void setupTest() {
        kafkaContainer.waitingFor(Wait.forListeningPort()).start();
    }

    @AfterAll
    static void tearDown() {
        kafkaContainer.stop();
    }

    @BeforeEach
    void init() {
        Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        kafkaConsumer = new KafkaConsumer<>(kafkaProperties, new StringDeserializer(), new JsonDeserializer<>(
                OrderStatusDto.class, false));
        kafkaConsumer.subscribe(List.of(TEST_TOPIC));
    }

    @Test
    void testUpdateOrderStatus_Ok() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatusDto = new OrderStatusDto(Status.DELIVERED);

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path((URL_TEMPLATE))
                        .pathSegment(orderId)
                        .pathSegment("delivery")
                        .build())
                .bodyValue(orderStatusDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        verify(courierService).updateDeliveryOrderStatus(orderId, orderStatusDto);
        verify(notificationHandler).sendNotification(orderId, orderStatusDto);

        final var consumerRecords = kafkaConsumer.poll(Durations.TEN_SECONDS);
        final boolean contains = consumerRecords.records(new TopicPartition(TEST_TOPIC, 0)).stream()
                .anyMatch(consumerRecord -> orderStatusDto.equals(consumerRecord.value()));

        assertTrue(contains);
    }

    @Test
    void testUpdateOrderStatus_BadRequest() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatusDto = new OrderStatusDto(Status.CREATED);

        webTestClient.put()
                .uri(uriBuilder -> uriBuilder.path((URL_TEMPLATE))
                        .pathSegment(orderId)
                        .pathSegment("delivery")
                        .build())
                .bodyValue(orderStatusDto)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();

        verify(courierService).updateDeliveryOrderStatus(orderId, orderStatusDto);
        verify(notificationHandler, never()).sendNotification(orderId, orderStatusDto);
    }

}
