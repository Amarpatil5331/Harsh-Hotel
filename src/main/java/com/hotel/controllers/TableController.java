package com.hotel.controllers;

import com.hotel.entity.Tables;
import com.hotel.entity.dtos.tables.ChairResponseDto;
import com.hotel.entity.dtos.tables.TableRequestDto;
import com.hotel.entity.dtos.tables.TableResponseDto;
import com.hotel.entity.dtos.tables.TableUpdateDto;
import com.hotel.services.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/table")
@CrossOrigin("*")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @PostMapping("/create-table")
    public ResponseEntity<?> addTable(@RequestBody TableRequestDto requestDto) {
        Tables table = tableService.addTables(requestDto);
        if (table == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        List<ChairResponseDto> chairResponseDtos = table.getChairs().stream()
                .map(chair -> ChairResponseDto.builder()
                        .chairId(chair.getChairId())
                        .chairStatus(chair.getChairStatus())
                        .chairName(chair.getChairName())
                        .build()
                )
                .collect(Collectors.toList());

        TableResponseDto tableResponseDto = TableResponseDto.builder()
                .tableId(table.getTableId())
                .tableName(table.getTableName())
                .tableStatus(table.getTableStatus())
                .hotelId(table.getHotel().getId())
                .chairs(chairResponseDtos)
                .build();

        return ResponseEntity.ok(tableResponseDto);
    }



    @GetMapping("/get-all-tables")
    public ResponseEntity<?> getAllTables(@RequestParam Long hotelId){
        List<TableResponseDto> tableResponseDtos=tableService.getAllTables(hotelId);
        return !tableResponseDtos.isEmpty()?
                ResponseEntity.ok(tableResponseDtos):
                ResponseEntity.noContent().build();
    }
    @PutMapping("/update-table")
    public ResponseEntity<?> updateTable(@RequestParam Long tableId,
                                         @RequestBody TableUpdateDto updateDto) {
        Tables updatedTable = tableService.updateTable(tableId, updateDto);

        List<ChairResponseDto> chairResponseDtos = updatedTable.getChairs().stream()
                .map(chair -> ChairResponseDto.builder()
                        .chairId(chair.getChairId())
                        .chairName(chair.getChairName())
                        .chairStatus(chair.getChairStatus())
                        .build())
                .collect(Collectors.toList());

        TableResponseDto responseDto = TableResponseDto.builder()
                .tableId(updatedTable.getTableId())
                .tableName(updatedTable.getTableName())
                .tableStatus(updatedTable.getTableStatus())
                .hotelId(updatedTable.getHotel().getId())
                .chairs(chairResponseDtos)
                .build();

        return ResponseEntity.ok(responseDto);
    }


    @DeleteMapping("/delete-table")
    public ResponseEntity<?> deleteTableById(@RequestParam Long tableId){
        boolean result= tableService.deleteTableById(tableId);
        return result?
                ResponseEntity.ok(true):
                ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
