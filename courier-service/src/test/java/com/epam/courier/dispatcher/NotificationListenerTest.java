package com.epam.courier.dispatcher;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.courier.service.CourierService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Durations;
import org.testcontainers.utility.DockerImageName;

import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class NotificationListenerTest {

    private static final String TEST_TOPIC = "test-notifications";

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag("5.4.3"));

    private KafkaProducer<String, OrderStatusDto> kafkaProducer;

    @SpyBean
    private CourierService courierService;

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
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        kafkaProducer = new KafkaProducer<>(kafkaProperties);
    }

    @Test
    void testConsumeWithOrderStatus_ReadyForDelivery() {
        final var orderId = UUID.randomUUID().toString();
        final var inboundOrderStatus = new OrderStatusDto(Status.READY_FOR_DELIVERY);
        final var header = new RecordHeader(KafkaHeaders.CORRELATION_ID, orderId.getBytes());
        final var producerRecord = new ProducerRecord<>(TEST_TOPIC, 0,
                Instant.now().toEpochMilli(), orderId, inboundOrderStatus, List.of(header));

        kafkaProducer.send(producerRecord);

        await().atMost(Durations.TEN_SECONDS).untilAsserted(
                () -> verify(courierService)
                        .updateDeliveryOrderStatus(orderId, new OrderStatusDto(Status.DELIVERY_IN_PROGRESS)));
    }

}
