package com.hotel.repositories;

import com.hotel.entity.TableOccupancy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableOccupancyRepository extends JpaRepository<TableOccupancy, Long> {
    boolean existsByHotelNameAndTableIdAndStatus(String hotelName, Long tableId, com.hotel.entity.enums.OccupancyStatus status);
    boolean existsByHotelNameAndChairIdAndStatus(String hotelName, Long chairId, com.hotel.entity.enums.OccupancyStatus status);
    java.util.List<TableOccupancy> findByOrder(com.hotel.entity.Order order);
}
