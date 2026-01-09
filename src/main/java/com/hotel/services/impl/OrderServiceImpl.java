package com.hotel.services.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.entity.Chairs;
import com.hotel.entity.Hotel;
import com.hotel.entity.Menu;
import com.hotel.entity.Order;
import com.hotel.entity.OrderItem;
import com.hotel.entity.OrderTable;
import com.hotel.entity.TableOccupancy;
import com.hotel.entity.Tables;
import com.hotel.entity.Thali;
import com.hotel.entity.dtos.orders.BillingDto;
import com.hotel.entity.dtos.orders.OrderItemRequestDto;
import com.hotel.entity.dtos.orders.OrderItemResponseDto;
import com.hotel.entity.dtos.orders.OrderRequestDto;
import com.hotel.entity.dtos.orders.OrderResponseDto;
import com.hotel.entity.dtos.orders.OrderTableDto;
import com.hotel.entity.enums.ChairStatus;
import com.hotel.entity.enums.ItemType;
import com.hotel.entity.enums.OccupancyStatus;
import com.hotel.entity.enums.OrderItemStatus;
import com.hotel.entity.enums.OrderStatus;
import com.hotel.entity.enums.TableStatus;
import com.hotel.repositories.ChairRepository;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.MenuRepository;
import com.hotel.repositories.OrderItemRepository;
import com.hotel.repositories.OrderRepository;
import com.hotel.repositories.TableOccupancyRepository;
import com.hotel.repositories.TableRepository;
import com.hotel.repositories.ThaliRepository;
import com.hotel.services.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableOccupancyRepository tableOccupancyRepository;
    private final HotelRepository hotelRepository;
    private final MenuRepository menuRepository;
    private final ThaliRepository thaliRepository;
    private final TableRepository tableRepository;
    private final ChairRepository chairRepository;
    private final ObjectMapper objectMapper;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    @Transactional
    public OrderResponseDto createOrderWithContact(Long hotelId, List<Long> tableIds, List<Long> chairIds, List<Long> itemIds, List<String> itemTypes, List<Integer> quantities, String customerContact) {
        // Create OrderRequestDto from the parameters
        OrderRequestDto orderRequestDto = new OrderRequestDto();
        orderRequestDto.setHotelId(hotelId);
        orderRequestDto.setTableIds(tableIds);
        orderRequestDto.setChairIds(chairIds);
        orderRequestDto.setCustomerContact(customerContact);

        // Create OrderItemRequestDto list from itemIds, itemTypes, and quantities
        List<OrderItemRequestDto> items = new ArrayList<>();
        for (int i = 0; i < itemIds.size(); i++) {
            OrderItemRequestDto itemRequest = new OrderItemRequestDto();
            itemRequest.setItemId(itemIds.get(i));
            itemRequest.setItemType(ItemType.valueOf(itemTypes.get(i)));
            itemRequest.setQuantity(quantities.get(i));
            items.add(itemRequest);
        }
        orderRequestDto.setItems(items);

        // Call the existing createOrder method
        return createOrder(orderRequestDto);
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto orderRequestDto) {
        // Validate hotel exists
        Optional<Hotel> hotelOpt = hotelRepository.findById(orderRequestDto.getHotelId());
        if (!hotelOpt.isPresent()) {
            throw new RuntimeException("Hotel not found");
        }
        Hotel hotel = hotelOpt.get();

        // Validate tables and chairs exist and are available
        List<Long> tableIds = orderRequestDto.getTableIds();
        List<Long> chairIds = orderRequestDto.getChairIds();

        // Build orderTables
        List<OrderTable> orderTables = new ArrayList<>();
        if (!chairIds.isEmpty()) {
            // Partial occupancy - group chairs by table
            Map<Long, List<String>> tableIdToChairNames = new HashMap<>();
            for (Long chairId : chairIds) {
                Optional<Chairs> chairOpt = chairRepository.findById(chairId);
                if (chairOpt.isPresent()) {
                    Chairs chair = chairOpt.get();
                    Long tableId = chair.getTable().getTableId();
                    tableIdToChairNames.computeIfAbsent(tableId, k -> new ArrayList<>()).add(chair.getChairName());
                }
            }
            for (Map.Entry<Long, List<String>> entry : tableIdToChairNames.entrySet()) {
                Long tableId = entry.getKey();
                List<String> chairNames = entry.getValue();
                Optional<Tables> tableOpt = tableRepository.findById(tableId);
                if (tableOpt.isPresent()) {
                    String tableName = tableOpt.get().getTableName();
                    orderTables.add(new OrderTable(tableName, chairNames));
                }
            }
        } else {
            // Full table occupancy - all chairs from tables
            for (Long tableId : tableIds) {
                Optional<Tables> tableOpt = tableRepository.findById(tableId);
                if (tableOpt.isPresent()) {
                    Tables table = tableOpt.get();
                    List<String> chairNames = table.getChairs().stream().map(Chairs::getChairName).collect(Collectors.toList());
                    orderTables.add(new OrderTable(table.getTableName(), chairNames));
                }
            }
        }

        // Check occupancy - allow partial occupancy
        // Check TableOccupancy records to prevent conflicts
        if (!chairIds.isEmpty()) {
            // Partial occupancy - check if any requested chairs are already occupied
            for (Long chairId : chairIds) {
                if (tableOccupancyRepository.existsByHotelNameAndChairIdAndStatus(hotel.getName(), chairId, OccupancyStatus.OCCUPIED)) {
                    throw new RuntimeException("Chair with id " + chairId + " is already occupied by an ongoing order");
                }
            }

            // Also check if any of the tables are fully occupied (have occupancy record with null chairId)
            for (Long tableId : tableIds) {
                List<TableOccupancy> allOccupancies = tableOccupancyRepository.findAll();
                boolean isFullyOccupied = allOccupancies.stream()
                    .anyMatch(occ -> occ.getHotelName().equals(hotel.getName()) &&
                             occ.getTableId().equals(tableId) &&
                             occ.getChairId() == null &&
                             occ.getStatus() == OccupancyStatus.OCCUPIED);
                if (isFullyOccupied) {
                    throw new RuntimeException("Table with id " + tableId + " is already fully occupied by an ongoing order");
                }
            }
        } else {
            // Full table occupancy - check if any requested tables are already occupied
            for (Long tableId : tableIds) {
                if (tableOccupancyRepository.existsByHotelNameAndTableIdAndStatus(hotel.getName(), tableId, OccupancyStatus.OCCUPIED)) {
                    throw new RuntimeException("Table with id " + tableId + " is already occupied by an ongoing order");
                }
            }
        }

        // Create order
        Order order = new Order();
        String sequenceKey = "ORD-" + hotel.getId();
        order.setOrderReference("ORD-"+sequenceGeneratorService.getNextNumber(sequenceKey));
        order.setHotelName(hotel.getName());
        order.setHotelId(hotel.getId());
        order.setOrderTables(orderTables);
        order.setStatus(OrderStatus.ONGOING);
        order.setCustomerContact(orderRequestDto.getCustomerContact()); // Set customer contact
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        order = orderRepository.save(order);

        // Create order items - consolidate duplicate items by quantity
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : orderRequestDto.getItems()) {
            // Fetch item name and type from itemId and itemType first
            String itemName = null;
            BigDecimal itemPrice = null;

            if (itemRequest.getItemId() == null) {
                throw new RuntimeException("Item ID is required");
            }
            if (itemRequest.getItemType() == null) {
                throw new RuntimeException("Item type is required");
            }

            if (itemRequest.getItemType() == ItemType.MENU) {
                Optional<Menu> menuOpt = menuRepository.findById(itemRequest.getItemId());
                if (menuOpt.isPresent()) {
                    Menu menu = menuOpt.get();
                    itemName = menu.getMenuName();
                    itemPrice = BigDecimal.valueOf(menu.getPrice());
                } else {
                    throw new RuntimeException("Menu item not found for id " + itemRequest.getItemId());
                }
            } else if (itemRequest.getItemType() == ItemType.THALI) {
                Optional<Thali> thaliOpt = thaliRepository.findById(itemRequest.getItemId());
                if (thaliOpt.isPresent()) {
                    Thali thali = thaliOpt.get();
                    itemName = thali.getThaliName();
                    itemPrice = BigDecimal.valueOf(thali.getThaliPrice());
                } else {
                    throw new RuntimeException("Thali item not found for id " + itemRequest.getItemId());
                }
            }

            // Check if item with same itemName and itemType already exists
            OrderItem existingItem = null;
            for (OrderItem item : orderItems) {
                if (item.getItemName() != null && item.getItemName().equals(itemName) &&
                    item.getItemType() != null && item.getItemType().equals(itemRequest.getItemType())) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                // Update quantity of existing item
                existingItem.setQuantity(existingItem.getQuantity() + itemRequest.getQuantity());
                existingItem.setUpdatedAt(LocalDateTime.now());
            } else {
                // Create new item
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setItemName(itemName);
                item.setItemType(itemRequest.getItemType());
                item.setPriceAtOrder(itemPrice);
                item.setQuantity(itemRequest.getQuantity());
                item.setStatus(OrderItemStatus.ADDED);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());

                orderItems.add(item);
            }
        }
        orderItemRepository.saveAll(orderItems);

        // Create occupancy
        if (!chairIds.isEmpty()) {
            // Partial occupancy
            for (Long chairId : chairIds) {
                Optional<Chairs> chairOpt = chairRepository.findById(chairId);
                if (chairOpt.isPresent()) {
                    Chairs chair = chairOpt.get();
                    TableOccupancy occupancy = new TableOccupancy();
                    occupancy.setHotelName(hotel.getName());
                    occupancy.setTableId(chair.getTable().getTableId());
                    occupancy.setChairId(chairId);
                    occupancy.setOrder(order);
                    occupancy.setStatus(OccupancyStatus.OCCUPIED);
                    occupancy.setCreatedAt(LocalDateTime.now());
                    occupancy.setUpdatedAt(LocalDateTime.now());
                    tableOccupancyRepository.save(occupancy);
                }
            }
        } else {
            // Full table occupancy
            for (Long tableId : tableIds) {
                // Check if any chair in the table is already occupied
                List<Chairs> chairs = tableRepository.findById(tableId)
                        .map(Tables::getChairs)
                        .orElse(new ArrayList<>());
                for (Chairs chair : chairs) {
                    if (tableOccupancyRepository.existsByHotelNameAndChairIdAndStatus(hotel.getName(), chair.getChairId(), OccupancyStatus.OCCUPIED)) {
                        throw new RuntimeException("Table with id " + tableId + " has chairs that are already occupied");
                    }
                }
                TableOccupancy occupancy = new TableOccupancy();
                occupancy.setHotelName(hotel.getName());
                occupancy.setTableId(tableId);
                occupancy.setOrder(order);
                occupancy.setStatus(OccupancyStatus.OCCUPIED);
                occupancy.setCreatedAt(LocalDateTime.now());
                occupancy.setUpdatedAt(LocalDateTime.now());
                tableOccupancyRepository.save(occupancy);
            }
        }

        // Update chair and table statuses
        updateStatusesAfterOrderCreation(hotel.getName(), tableIds, chairIds);

        // Reload order with updated tables and chairs for response
        Optional<Order> updatedOrderOpt = orderRepository.findById(order.getOrderId());
        if (updatedOrderOpt.isPresent()) {
            Order updatedOrder = updatedOrderOpt.get();
            List<OrderItem> updatedOrderItems = orderItemRepository.findByOrder(updatedOrder);
            return mapToResponseDto(updatedOrder, updatedOrderItems);
        } else {
            return mapToResponseDto(order, orderItems);
        }
    }
    


    private void updateStatusesAfterOrderCreation(String hotelName, List<Long> tableIds, List<Long> chairIds) {
        if (!chairIds.isEmpty()) {
            // Partial occupancy - only update specified chairs
            for (Long chairId : chairIds) {
                chairRepository.findById(chairId).ifPresent(chair -> {
                    chair.setChairStatus(ChairStatus.OCCUPIED);
                    chairRepository.save(chair);
                });
            }

            // Update table statuses based on chair occupancy
            for (Long tableId : tableIds) {
                tableRepository.findById(tableId).ifPresent(table -> {
                    List<Chairs> chairs = table.getChairs();
                    long occupiedCount = chairs.stream()
                            .filter(chair -> chair.getChairStatus() == ChairStatus.OCCUPIED)
                            .count();
                    if (occupiedCount == 0) {
                        table.setTableStatus(TableStatus.AVAILABLE);
                    } else if (occupiedCount < chairs.size()) {
                        table.setTableStatus(TableStatus.PARTIALLY_OCCUPIED);
                    } else {
                        table.setTableStatus(TableStatus.FULLY_OCCUPIED);
                    }
                    tableRepository.save(table);
                });
            }
        } else {
            // Full table occupancy - mark all chairs as occupied and set table to FULLY_OCCUPIED
            for (Long tableId : tableIds) {
                tableRepository.findById(tableId).ifPresent(table -> {
                    // Mark all chairs in the table as occupied
                    List<Chairs> chairs = table.getChairs();
                    for (Chairs chair : chairs) {
                        chair.setChairStatus(ChairStatus.OCCUPIED);
                        chairRepository.save(chair);
                    }

                    // Set table status to FULLY_OCCUPIED
                    table.setTableStatus(TableStatus.FULLY_OCCUPIED);
                    tableRepository.save(table);
                });
            }
        }
    }

    private void updateStatusesAfterOrderCloseOrCancel(String hotelName, List<Long> tableIds, List<Long> chairIds) {
        if (!chairIds.isEmpty()) {
            // Partial occupancy - only update specified chairs
            for (Long chairId : chairIds) {
                chairRepository.findById(chairId).ifPresent(chair -> {
                    chair.setChairStatus(ChairStatus.AVAILABLE);
                    chairRepository.save(chair);
                });
            }

            // Update table statuses based on remaining chair occupancy
            for (Long tableId : tableIds) {
                tableRepository.findById(tableId).ifPresent(table -> {
                    List<Chairs> chairs = table.getChairs();
                    long occupiedCount = chairs.stream()
                            .filter(chair -> chair.getChairStatus() == ChairStatus.OCCUPIED)
                            .count();
                    if (occupiedCount == 0) {
                        table.setTableStatus(TableStatus.AVAILABLE);
                    } else if (occupiedCount < chairs.size()) {
                        table.setTableStatus(TableStatus.PARTIALLY_OCCUPIED);
                    } else {
                        table.setTableStatus(TableStatus.FULLY_OCCUPIED);
                    }
                    tableRepository.save(table);
                });
            }
        } else {
            // Full table occupancy - mark ALL chairs as available and set table to AVAILABLE
            for (Long tableId : tableIds) {
                tableRepository.findById(tableId).ifPresent(table -> {
                    // Mark all chairs in the table as available
                    List<Chairs> chairs = table.getChairs();
                    for (Chairs chair : chairs) {
                        chair.setChairStatus(ChairStatus.AVAILABLE);
                        chairRepository.save(chair);
                    }

                    // Set table status to AVAILABLE
                    table.setTableStatus(TableStatus.AVAILABLE);
                    tableRepository.save(table);
                });
            }
        }
    }

    // private BigDecimal getPrice(String itemName, ItemType itemType) {
    //     if (itemType == ItemType.MENU) {
    //         Menu menu = menuRepository.findByMenuName(itemName);
    //         return BigDecimal.valueOf(menu.getPrice());
    //     } else {
    //         Thali thali = thaliRepository.findByThaliName(itemName);
    //         return BigDecimal.valueOf(thali.getThaliPrice());
    //     }
    // }

    @Override
    @Transactional
    public OrderResponseDto addItems(Long orderId, List<OrderItemRequestDto> items) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent() || orderOpt.get().getStatus() != OrderStatus.ONGOING) {
            throw new RuntimeException("Order not found or not ongoing");
        }
        Order order = orderOpt.get();

        // Get existing items for this order to check for duplicates
        List<OrderItem> existingItems = orderItemRepository.findByOrder(order);

        List<OrderItem> itemsToSave = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : items) {
            // Fetch item name and type from itemId and itemType first
            String itemName = null;
            BigDecimal itemPrice = null;

            if (itemRequest.getItemId() == null) {
                throw new RuntimeException("Item ID is required");
            }
            if (itemRequest.getItemType() == null) {
                throw new RuntimeException("Item type is required");
            }

            if (itemRequest.getItemType() == ItemType.MENU) {
                Optional<Menu> menuOpt = menuRepository.findById(itemRequest.getItemId());
                if (menuOpt.isPresent()) {
                    Menu menu = menuOpt.get();
                    itemName = menu.getMenuName();
                    itemPrice = BigDecimal.valueOf(menu.getPrice());
                } else {
                    throw new RuntimeException("Menu item not found for id " + itemRequest.getItemId());
                }
            } else if (itemRequest.getItemType() == ItemType.THALI) {
                Optional<Thali> thaliOpt = thaliRepository.findById(itemRequest.getItemId());
                if (thaliOpt.isPresent()) {
                    Thali thali = thaliOpt.get();
                    itemName = thali.getThaliName();
                    itemPrice = BigDecimal.valueOf(thali.getThaliPrice());
                } else {
                    throw new RuntimeException("Thali item not found for id " + itemRequest.getItemId());
                }
            }

            // Check if item with same itemName and itemType already exists in the order
            OrderItem existingItem = null;
            for (OrderItem item : existingItems) {
                if (item.getItemName() != null && item.getItemName().equals(itemName) &&
                    item.getItemType() != null && item.getItemType().equals(itemRequest.getItemType())) {
                    existingItem = item;
                    break;
                }
            }

            if (existingItem != null) {
                // Update quantity of existing item
                existingItem.setQuantity(existingItem.getQuantity() + itemRequest.getQuantity());
                existingItem.setUpdatedAt(LocalDateTime.now());
                itemsToSave.add(existingItem);
            } else {
                // Create new item
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setItemName(itemName);
                item.setItemType(itemRequest.getItemType());
                item.setPriceAtOrder(itemPrice);
                item.setQuantity(itemRequest.getQuantity());
                item.setStatus(OrderItemStatus.ADDED);
                item.setCreatedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                itemsToSave.add(item);
            }
        }
        orderItemRepository.saveAll(itemsToSave);

        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        List<OrderItem> allItems = orderItemRepository.findByOrder(order);
        return mapToResponseDto(order, allItems);
    }

    @Override
    @Transactional
    public OrderResponseDto billOrder(Long orderId, com.hotel.entity.dtos.orders.BillingRequestDto billingRequest) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent() || orderOpt.get().getStatus() != OrderStatus.ONGOING) {
            throw new RuntimeException("Order not found or not ongoing");
        }
        Order order = orderOpt.get();

        // Calculate billing
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item.getStatus() != OrderItemStatus.CANCELLED) {
                total = total.add(item.getPriceAtOrder().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }

        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal discount = BigDecimal.ZERO;

        // Apply GST if selected
        if (billingRequest.getApplyGst() != null && billingRequest.getApplyGst()) {
            BigDecimal gstPercent = billingRequest.getGstPercentage() != null ? billingRequest.getGstPercentage() : BigDecimal.ZERO;
            tax = total.multiply(gstPercent).divide(BigDecimal.valueOf(100)); // dynamic GST percentage
        }

        // Apply discount if provided
        if (billingRequest.getDiscountAmount() != null) {
            discount = billingRequest.getDiscountAmount();
            if (discount.compareTo(total.add(tax)) > 0) {
                throw new RuntimeException("Discount cannot be greater than total amount including tax");
            }
        }

        BigDecimal finalAmount = total.add(tax).subtract(discount);

        BillingDto billing = new BillingDto(total, tax, discount, finalAmount);
        try {
            String json = objectMapper.writeValueAsString(billing);
            order.setBillingSnapshot(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing billing", e);
        }
        order.setStatus(OrderStatus.CLOSED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Delete occupancy records (no longer needed for closed orders)
        List<TableOccupancy> occupancies = tableOccupancyRepository.findByOrder(order);
        tableOccupancyRepository.deleteAll(occupancies);

        // Update chair and table statuses
        List<Long> tableIds = occupancies.stream()
                .map(TableOccupancy::getTableId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        List<Long> chairIds = occupancies.stream()
                .filter(occ -> occ.getChairId() != null)
                .map(TableOccupancy::getChairId)
                .collect(java.util.stream.Collectors.toList());
        updateStatusesAfterOrderCloseOrCancel(order.getHotelName(), tableIds, chairIds);

        return mapToResponseDto(order, items);
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent() || orderOpt.get().getStatus() != OrderStatus.ONGOING) {
            throw new RuntimeException("Order not found or not ongoing");
        }
        Order order = orderOpt.get();

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Delete occupancy records (no longer needed for canceled orders)
        List<TableOccupancy> occupancies = tableOccupancyRepository.findByOrder(order);
        tableOccupancyRepository.deleteAll(occupancies);

        // Update chair and table statuses
        List<Long> tableIds = occupancies.stream()
                .map(TableOccupancy::getTableId)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        List<Long> chairIds = occupancies.stream()
                .filter(occ -> occ.getChairId() != null)
                .map(TableOccupancy::getChairId)
                .collect(java.util.stream.Collectors.toList());
        updateStatusesAfterOrderCloseOrCancel(order.getHotelName(), tableIds, chairIds);

        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return mapToResponseDto(order, items);
    }

    @Override
    @Transactional
    public OrderResponseDto updateItem(Long orderId, Long orderItemId, Integer quantity) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent() || orderOpt.get().getStatus() != OrderStatus.ONGOING) {
            throw new RuntimeException("Order not found or not ongoing");
        }
        Optional<OrderItem> itemOpt = orderItemRepository.findById(orderItemId);
        if (!itemOpt.isPresent() || !itemOpt.get().getOrder().getOrderId().equals(orderId)) {
            throw new RuntimeException("Order item not found");
        }
        OrderItem item = itemOpt.get();

        if (quantity == 0) {
            // Delete the order item if quantity is zero
            orderItemRepository.delete(item);
        } else {
            // Update the quantity to the new value
            item.setQuantity(quantity);
            item.setUpdatedAt(LocalDateTime.now());
            orderItemRepository.save(item);
        }

        orderOpt.get().setUpdatedAt(LocalDateTime.now());
        orderRepository.save(orderOpt.get());

        List<OrderItem> allItems = orderItemRepository.findByOrder(orderOpt.get());
        return mapToResponseDto(orderOpt.get(), allItems);
    }

    @Override
    public List<OrderResponseDto> listOrders(String status, Long hotelId) {
        List<OrderResponseDto> response = new ArrayList<>();
        if (hotelId != null) {
            // Validate hotel exists
            Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
            if (!hotelOpt.isPresent()) {
                return response; // Return empty list if hotel not found
            }
            // Use the new repository method to find orders by hotelId and status
            OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
            List<Order> orders = orderRepository.findByHotelIdAndStatus(hotelId, orderStatus);
            for (Order order : orders) {
                List<OrderItem> items = orderItemRepository.findByOrder(order);
                response.add(mapToResponseDto(order, items));
            }
        } else {
            // If no hotelId provided, get all orders and filter by status if provided
            List<Order> orders = orderRepository.findAll();
            for (Order order : orders) {
                if (status != null && !order.getStatus().name().equals(status)) continue;
                List<OrderItem> items = orderItemRepository.findByOrder(order);
                response.add(mapToResponseDto(order, items));
            }
        }
        return response;
    }

    @Override
    public List<OrderResponseDto> getOrdersForAuditByHotelId(Long hotelId) {
        // Validate hotel exists
        Optional<Hotel> hotelOpt = hotelRepository.findById(hotelId);
        if (!hotelOpt.isPresent()) {
            throw new RuntimeException("Hotel not found");
        }

        // Use the repository method to find all orders by hotelId (no status filter for audit)
        List<Order> orders = orderRepository.findByHotelIdAndStatus(hotelId, null);
        List<OrderResponseDto> response = new ArrayList<>();

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            response.add(mapToResponseDto(order, items));
        }

        return response;
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return null;
        }
        Order order = orderOpt.get();
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return mapToResponseDto(order, items);
    }

    private OrderResponseDto mapToResponseDto(Order order, List<OrderItem> items) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getOrderId());
        dto.setOrderReference(order.getOrderReference());
        dto.setHotelName(order.getHotelName());
        dto.setHotelId(order.getHotelId());
        List<OrderTableDto> orderTableDtos = order.getOrderTables().stream()
            .map(ot -> new OrderTableDto(ot.getTableName(), ot.getChairs()))
            .collect(Collectors.toList());
        dto.setOrderTables(orderTableDtos);
        dto.setStatus(order.getStatus());
        dto.setHotelAddress(hotelRepository.findAddressById(order.getHotelId()));
        if (order.getBillingSnapshot() != null) {
            dto.setBillingSnapshot(deserializeBilling(order.getBillingSnapshot()));
        }
        dto.setCustomerContact(order.getCustomerContact()); // Set customer contact
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponseDto> itemDtos = new ArrayList<>();
        for (OrderItem item : items) {
            OrderItemResponseDto itemDto = new OrderItemResponseDto();
            itemDto.setOrderItemId(item.getOrderItemId());
            itemDto.setItemName(item.getItemName());
            itemDto.setItemType(item.getItemType());
            itemDto.setPriceAtOrder(item.getPriceAtOrder());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setStatus(item.getStatus());
            itemDto.setCreatedAt(item.getCreatedAt());
            itemDto.setUpdatedAt(item.getUpdatedAt());
            itemDtos.add(itemDto);
        }
        dto.setItems(itemDtos);
        return dto;
    }

    private BillingDto deserializeBilling(String json) {
        try {
            return objectMapper.readValue(json, BillingDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error deserializing billing", e);
        }
    }

    
}
