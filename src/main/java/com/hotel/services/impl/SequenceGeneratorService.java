package com.hotel.services.impl;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hotel.entity.SequenceGenerator;
import com.hotel.repositories.SequenceGeneratorRepository;

@Service
public class SequenceGeneratorService {

    @Autowired
    private SequenceGeneratorRepository repository;

    @Transactional
    public String getNextNumber(String key) {
        SequenceGenerator sequence = repository.findById(key)
                .orElse(new SequenceGenerator());
        
        if (sequence.getCurrentValue() == null) {
            sequence.setCurrentValue(0L);
        }

        Long nextValue = sequence.getCurrentValue() + 1;
        sequence.setName(key);
        sequence.setCurrentValue(nextValue);

        repository.save(sequence);

        // format to 6 digits with leading zeros
        return String.format("%06d", nextValue);
    }
}

