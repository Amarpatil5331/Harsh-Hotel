package com.hotel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hotel.entity.SequenceGenerator;

public interface SequenceGeneratorRepository extends JpaRepository<SequenceGenerator, String> {
}

