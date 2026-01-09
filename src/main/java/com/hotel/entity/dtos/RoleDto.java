package com.hotel.entity.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {

    private Long roleId;
    private String roleName;
    private String roleDescription;
    private List<PermissionDto> permissions;
}
