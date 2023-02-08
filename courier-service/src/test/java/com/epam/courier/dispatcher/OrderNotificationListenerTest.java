package com.epam.courier.dispatcher;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Durations;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;
import com.epam.courier.service.CourierService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OrderNotificationListenerTest {

    private static final String TEST_TOPIC = "test-topic";

    @Container
    public static GenericContainer kafkaContainer = new GenericContainer("bitnami/kafka:latest")
            .withEnv("KAFKA_ENABLE_KRAFT", "yes")
            .withEnv("KAFKA_CFG_PROCESS_ROLES", "broker,controller")
            .withEnv("KAFKA_CFG_BROKER_ID", "101")
            .withEnv("KAFKA_CFG_CONTROLLER_LISTENER_NAMES", "CONTROLLER")
            .withEnv("KAFKA_CFG_LISTENERS", "EXTERNAL://:9092,CONTROLLER://:9093")
            .withEnv("KAFKA_CFG_LISTENER_SECURITY_PROTOCOL_MAP", "EXTERNAL:PLAINTEXT,CONTROLLER:PLAINTEXT")
            .withEnv("KAFKA_CFG_CONTROLLER_QUORUM_VOTERS", "101@kafka:9093")
            .withEnv("KAFKA_CFG_ADVERTISED_LISTENERS", "EXTERNAL://localhost:9092")
            .withEnv("KAFKA_KRAFT_CLUSTER_ID", "ppfbPpQSTVSoFZ4IT8Xbtg");;

    private KafkaProducer<String, OrderStatusDto> kafkaProducer;

    @SpyBean
    private CourierService courierService;

    @SpyBean
    private OrderNotificationProducer orderNotificationProducer;

    @BeforeAll
    static void setUp() {
        kafkaContainer.start();
    }

    @AfterAll
    static void tearDown() {
        kafkaContainer.stop();
    }

    @BeforeEach
    void init() {
        Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        kafkaProducer = new KafkaProducer<>(kafkaProperties);
    }

    @Test
    void testConsumeWithOrderStatus_ReadyForDelivery() {
        final var orderId = UUID.randomUUID().toString();
        final var inboundOrderStatus = new OrderStatusDto(Status.READY_FOR_DELIVERY);
        final var outboundOrderStatus = new OrderStatusDto(Status.DELIVERY_IN_PROGRESS);
        final var header = new RecordHeader(KafkaHeaders.CORRELATION_ID, orderId.getBytes());
        final var producerRecord = new ProducerRecord<>(TEST_TOPIC, 0,
                Instant.now().toEpochMilli(), orderId, inboundOrderStatus, List.of(header));

        kafkaProducer.send(producerRecord);

        await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
            verify(courierService).updateDeliveryOrderStatus(orderId, outboundOrderStatus);
            verify(orderNotificationProducer).sendNotification(orderId, outboundOrderStatus);
        });
    }

    @Test
    void testConsumeWithOrderStatus_Created() {
        final var orderId = UUID.randomUUID().toString();
        final var inboundOrderStatus = new OrderStatusDto(Status.CREATED);
        final var header = new RecordHeader(KafkaHeaders.CORRELATION_ID, orderId.getBytes());
        final var producerRecord = new ProducerRecord<>(TEST_TOPIC, 0,
                Instant.now().toEpochMilli(), orderId, inboundOrderStatus, List.of(header));

        kafkaProducer.send(producerRecord);

        await().atMost(Durations.TEN_SECONDS).untilAsserted(() -> {
            verify(courierService, never()).updateDeliveryOrderStatus(eq(orderId), any());
            verify(orderNotificationProducer, never()).sendNotification(eq(orderId), any());
        });
    }

}
