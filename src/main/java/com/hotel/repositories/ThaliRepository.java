package com.hotel.repositories;

import com.hotel.entity.Thali;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository("thaliRepository")
public interface ThaliRepository extends JpaRepository<Thali, Long> {
    List<Thali> findByHotel_Id(Long hotelId);
    Thali findByThaliName(String thaliName);

    @Query("SELECT COUNT(t) FROM Thali t JOIN t.menus m WHERE m.menuId = :menuId")
    long countThalisByMenuId(@Param("menuId") Long menuId);
}
