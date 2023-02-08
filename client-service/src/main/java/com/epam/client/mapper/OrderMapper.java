package com.epam.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.epam.api.dto.OrderDto;
import com.epam.api.dto.OrderStatusDto;
import com.epam.api.dto.PlaceOrderRequestDto;
import com.epam.client.model.Order;
import com.epam.client.model.Status;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", constant = "CREATED")
    Order toOrder(PlaceOrderRequestDto placeOrderRequestDto);

    OrderDto toOrderDto(Order order);

    OrderStatusDto toOrderStatusDto(Status orderStatus);

}
