package com.khaldoun.ecommerce.order.mapper;

import com.khaldoun.ecommerce.order.domain.Order;
import com.khaldoun.ecommerce.order.domain.OrderItem;
import com.khaldoun.ecommerce.order.dto.OrderItemResponse;
import com.khaldoun.ecommerce.order.dto.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", expression = "java(String.valueOf(order.getStatus()))")
    @Mapping(target = "items", expression = "java(itemsToItemResponseList(order.getItems()))")
    OrderResponse toOrderResponse(Order order);

    OrderItemResponse toOrderItemResponse(OrderItem item);

    List<OrderItemResponse> itemsToItemResponseList(List<OrderItem> items);
}
