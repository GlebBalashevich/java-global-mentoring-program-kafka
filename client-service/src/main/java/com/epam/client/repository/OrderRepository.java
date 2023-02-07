package com.epam.client.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.epam.client.model.Order;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<Order, String> {
}
