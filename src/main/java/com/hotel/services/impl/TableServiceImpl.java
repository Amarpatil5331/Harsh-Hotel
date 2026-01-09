package com.hotel.services.impl;

import com.hotel.entity.Chairs;
import com.hotel.entity.Hotel;
import com.hotel.entity.Tables;
import com.hotel.entity.enums.ChairStatus;
import com.hotel.entity.enums.TableStatus;
import com.hotel.entity.dtos.tables.ChairResponseDto;
import com.hotel.entity.dtos.tables.TableRequestDto;
import com.hotel.entity.dtos.tables.TableResponseDto;
import com.hotel.entity.dtos.tables.TableUpdateDto;
import com.hotel.repositories.ChairRepository;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.TableRepository;
import com.hotel.services.TableService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("tableService")
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {
    private final TableRepository tableRepository;
    private final ChairRepository chairRepository;
    private final HotelRepository hotelRepository;

    @Override
    public Tables addTables(TableRequestDto tableRequestDto) {
        Hotel hotel = hotelRepository.findById(tableRequestDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + tableRequestDto.getHotelId()));

        Tables table = new Tables();
        table.setTableName(tableRequestDto.getTableName());
        table.setTableStatus(TableStatus.AVAILABLE); // default status
        table.setHotel(hotel);

        Tables savedTable = tableRepository.save(table);

        List<Chairs> chairs = Optional.ofNullable(tableRequestDto.getChairs())
                .orElse(Collections.emptyList()) // fallback to empty list
                .stream()
                .map(new java.util.function.Function<com.hotel.entity.dtos.tables.ChairRequestDto, Chairs>() {
                    private int chairNumber = 1;
                    @Override
                    public Chairs apply(com.hotel.entity.dtos.tables.ChairRequestDto chairDto) {
                        Chairs newChair = new Chairs();
                        newChair.setChairStatus(chairDto.getChairStatus());
                        newChair.setTable(savedTable);
                        // Generate chair name: Chair-1, Chair-2, etc.
                        newChair.setChairName("Chair-" + chairNumber++);
                        return newChair;
                    }
                })
                .collect(Collectors.toList());

        chairRepository.saveAll(chairs);
        savedTable.setChairs(chairs);

        return savedTable;
    }



    @Override
    public List<TableResponseDto> getAllTables(Long hotelId) {
        return tableRepository.getTablesByHotel_Id(hotelId).stream()
                .map(table -> TableResponseDto.builder()
                        .tableId(table.getTableId())
                        .tableName(table.getTableName())
                        .tableStatus(table.getTableStatus())
                        .hotelId(table.getHotel().getId())
                        .chairs(
                                table.getChairs().stream()
                                        .map(chair -> ChairResponseDto.builder()
                                                .chairId(chair.getChairId())
                                                .chairName(chair.getChairName())
                                                .chairStatus(chair.getChairStatus())
                                                .build()
                                        )
                                        .collect(Collectors.toList())
                        )
                        .build()
                )
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Tables updateTable(Long tableId, TableUpdateDto updateDto) {
        // 1. Find table
        Tables table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found with ID = " + tableId));

        // 2. Update table name if provided
        if (updateDto.getTableName() != null) {
            table.setTableName(updateDto.getTableName());
        }

        // 3. Handle chairs count update
        int currentCount = table.getChairs().size();
        int newCount = updateDto.getChairCount() != null ? updateDto.getChairCount() : currentCount;

        if (newCount > currentCount) {
            // Add new chairs
            for (int i = 0; i < newCount - currentCount; i++) {
                Chairs newChair = new Chairs();
                newChair.setChairStatus(ChairStatus.AVAILABLE); // default
                newChair.setTable(table);
                // Generate chair name: Chair-1, Chair-2, etc.
                int chairNumber = table.getChairs().size() + 1;
                newChair.setChairName("Chair-" + chairNumber);
                table.getChairs().add(newChair);
            }
        } else if (newCount < currentCount) {
            // Remove extra chairs
            List<Chairs> chairs = table.getChairs();
            for (int i = currentCount - 1; i >= newCount; i--) {
                chairRepository.delete(chairs.get(i));
                chairs.remove(i);
            }
        }

        // 4. Save table
        return tableRepository.save(table);
    }

    @Override
    @Transactional
    public boolean deleteTableById(Long tableId) {
        boolean result=tableRepository.existsByTableId(tableId);
        if(!result){
            return false;
        }

        tableRepository.deleteById(tableId);
        return true;
    }


}
