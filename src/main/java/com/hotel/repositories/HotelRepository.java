package com.hotel.repositories;


import com.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository("hotelRepository")
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);
    Hotel findByName(String name);

    @Query("SELECT h.address FROM Hotel h WHERE h.id = :id")
    String findAddressById(Long id);
}
