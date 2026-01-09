package com.hotel.repositories;

import com.hotel.entity.Chairs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("chairRepository")
public interface ChairRepository extends JpaRepository<Chairs, Long> {
}
