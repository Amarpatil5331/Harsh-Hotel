package com.hotel.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.entity.dtos.orders.BillingRequestDto;
import com.hotel.entity.dtos.orders.OrderItemRequestDto;
import com.hotel.entity.dtos.orders.OrderRequestDto;
import com.hotel.entity.dtos.orders.OrderResponseDto;
import com.hotel.services.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@CrossOrigin("*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create-order")
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto responseDto = orderService.createOrder(orderRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    // Alternative endpoint that accepts customer contact as a separate parameter
    @PostMapping("/create-order-with-contact")
    public ResponseEntity<OrderResponseDto> createOrderWithContact(
            @RequestParam Long hotelId,
            @RequestParam List<Long> tableIds,
            @RequestParam List<Long> chairIds,
            @RequestParam List<Long> itemIds,
            @RequestParam List<String> itemTypes,
            @RequestParam List<Integer> quantities,
            @RequestParam String customerContact) {
        OrderResponseDto responseDto = orderService.createOrderWithContact(hotelId, tableIds, chairIds, itemIds, itemTypes, quantities, customerContact);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/add-items")
    public ResponseEntity<OrderResponseDto> addItems(
            @RequestParam Long orderId,
            @RequestBody List<OrderItemRequestDto> items) {
        OrderResponseDto responseDto = orderService.addItems(orderId, items);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/update-item")
    public ResponseEntity<OrderResponseDto> updateItem(
            @RequestParam Long orderId,
            @RequestParam Long orderItemId,
            @RequestParam Integer quantity) {
        OrderResponseDto responseDto = orderService.updateItem(orderId, orderItemId, quantity);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/bill")
    public ResponseEntity<OrderResponseDto> billOrder(
            @RequestParam Long orderId,
            @RequestBody BillingRequestDto billingRequest) {
        OrderResponseDto responseDto = orderService.billOrder(orderId, billingRequest);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping("/cancel")
    public ResponseEntity<OrderResponseDto> cancelOrder(@RequestParam Long orderId) {
        OrderResponseDto responseDto = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(responseDto);
    }

    
    //this will get order by hotel,
    @GetMapping("/get-orders")
    public ResponseEntity<List<OrderResponseDto>> listOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long hotelId) {
        if (status != null) {
            status = status.toUpperCase();
        }
        List<OrderResponseDto> orders = orderService.listOrders(status, hotelId);
        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(orders);
    }

    // New method to get order details by orderId only
    @GetMapping("/get-order")
    public ResponseEntity<OrderResponseDto> getOrderById(@RequestParam Long orderId) {
        OrderResponseDto order = orderService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    // New endpoint for auditing - get all orders with tables, chairs, bill and status by hotelId
    @GetMapping("/audit")
    public ResponseEntity<List<OrderResponseDto>> getOrdersForAuditByHotelId(@RequestParam Long hotelId) {
        List<OrderResponseDto> orders = orderService.getOrdersForAuditByHotelId(hotelId);
        if (orders.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(orders);
    }
}
