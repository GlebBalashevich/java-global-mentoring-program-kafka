package com.epam.palmetto.dispatcher;

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
import org.springframework.kafka.support.serializer.JsonDeserializer;
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

import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.Status;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class NotificationHandlerTest {

    private static final String TEST_TOPIC = "test-notifications";

    @Container
    public static KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka").withTag("5.4.3"));

    @Autowired
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
    void testSendNotification() {
        final var orderId = UUID.randomUUID().toString();
        final var orderStatusDto = new OrderStatusDto(Status.DELIVERY_IN_PROGRESS);

        notificationHandler.sendNotification(orderId, orderStatusDto).subscribe();

        final var consumerRecords = kafkaConsumer.poll(Durations.TEN_SECONDS);
        final boolean contains = consumerRecords.records(new TopicPartition(TEST_TOPIC, 0)).stream()
                .anyMatch(consumerRecord -> orderStatusDto.equals(consumerRecord.value()));

        assertTrue(contains);
    }

}
