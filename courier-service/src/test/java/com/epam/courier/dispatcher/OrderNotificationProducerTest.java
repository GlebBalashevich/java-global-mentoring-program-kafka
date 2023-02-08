package com.epam.courier.dispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Durations;

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class OrderNotificationProducerTest {

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
            .withEnv("KAFKA_KRAFT_CLUSTER_ID", "ppfbPpQSTVSoFZ4IT8Xbtg");

    private KafkaConsumer<String, OrderStatusDto> kafkaConsumer;

    @Autowired
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
        TopicPartition topicPartition = new TopicPartition(TEST_TOPIC, 0);
        Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        kafkaProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group-in-2");
        kafkaConsumer = new KafkaConsumer<>(kafkaProperties, new StringDeserializer(), new JsonDeserializer<>(
                OrderStatusDto.class, false));
        kafkaConsumer.assign(List.of(topicPartition));
    }

    @Test
    void testProduceWithOrderStatus_DeliveryInProgress() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatus = new OrderStatusDto(Status.DELIVERY_IN_PROGRESS);

        orderNotificationProducer.sendNotification(orderId, orderStatus).subscribe();

        final var consumerRecords = kafkaConsumer.poll(Durations.TEN_SECONDS);
        final var topicPartition = new TopicPartition(TEST_TOPIC, 0);
        final boolean contains = consumerRecords.records(topicPartition).stream()
                .anyMatch(consumerRecord -> orderId.equals(consumerRecord.key())
                        && orderStatus.equals(consumerRecord.value()));

        assertTrue(contains);
    }

}
