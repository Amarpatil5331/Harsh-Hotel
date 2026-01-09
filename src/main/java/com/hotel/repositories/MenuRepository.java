package com.hotel.repositories;

import com.hotel.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository("menuRepository")
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByHotel_Id(Long hotelId);
    Menu findByMenuName(String menuName);
}
