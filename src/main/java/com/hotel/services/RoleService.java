package com.hotel.services;



import com.hotel.entity.Permissions;
import com.hotel.entity.Privilege;
import com.hotel.entity.Role;
import com.hotel.entity.dtos.RoleDto;

import java.util.List;
public interface RoleService {
    Role createRole(RoleDto roleDto);
    List<RoleDto> getAllRoles();
    List<Permissions> createPermissions(List<Permissions> permissions);
    Privilege createPrivilege(Privilege privilege);

    RoleDto getRoleByRoleName(String roleName);

    Role updateRole(RoleDto roleDto) ;

    boolean deleteRole(Long roleId);

    Role findById(Long roleId);
}
