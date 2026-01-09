package com.hotel.services;

import java.util.List;

import com.hotel.entity.dtos.orders.OrderRequestDto;
import com.hotel.entity.dtos.orders.OrderResponseDto;

public interface OrderService {
    OrderResponseDto createOrder(OrderRequestDto orderRequestDto);
    OrderResponseDto createOrderWithContact(Long hotelId, List<Long> tableIds, List<Long> chairIds, List<Long> itemIds, List<String> itemTypes, List<Integer> quantities, String customerContact);
    OrderResponseDto addItems(Long orderId, List<com.hotel.entity.dtos.orders.OrderItemRequestDto> items);
    OrderResponseDto updateItem(Long orderId, Long orderItemId, Integer quantity);
    OrderResponseDto billOrder(Long orderId, com.hotel.entity.dtos.orders.BillingRequestDto billingRequest);
    OrderResponseDto cancelOrder(Long orderId);
    List<OrderResponseDto> listOrders(String status, Long hotelId);

    List<OrderResponseDto> getOrdersForAuditByHotelId(Long hotelId);
    OrderResponseDto getOrderById(Long orderId);
}
