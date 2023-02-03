package com.epam.client.mapper;

import com.epam.client.dto.OrderDto;
import com.epam.client.dto.OrderStatusDto;
import com.epam.client.dto.PlaceOrderRequestDto;
import com.epam.client.model.Order;
import com.epam.client.model.OrderStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", constant = "CREATED")
    Order toOrder(PlaceOrderRequestDto placeOrderRequestDto);

    OrderDto toOrderDto(Order order);

    OrderStatusDto toOrderStatusDto(OrderStatus orderStatus);
}
