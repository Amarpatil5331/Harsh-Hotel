package com.hotel.repositories;

import com.hotel.entity.Tables;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository("tableRepository")
public interface TableRepository extends JpaRepository<Tables, Long> {
    List<Tables> getTablesByHotel_Id(Long hotelId);

    boolean existsByTableId(Long tableId);
}
