package com.hotel.services;

import com.hotel.entity.Tables;
import com.hotel.entity.dtos.tables.TableRequestDto;
import com.hotel.entity.dtos.tables.TableResponseDto;
import com.hotel.entity.dtos.tables.TableUpdateDto;

import java.util.List;

public interface TableService {
    Tables addTables(TableRequestDto requestDto);

    List<TableResponseDto> getAllTables(Long id);

    boolean deleteTableById(Long tableId);

    public Tables updateTable(Long tableId, TableUpdateDto updateDto);
}
