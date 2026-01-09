package com.hotel.services.impl;


import com.hotel.entity.Permissions;
import com.hotel.entity.Privilege;
import com.hotel.entity.Role;
import com.hotel.entity.dtos.PermissionDto;
import com.hotel.entity.dtos.RoleDto;
import com.hotel.repositories.AppUserRepository;
import com.hotel.repositories.PermissionRepository;
import com.hotel.repositories.PrivilegeRepository;
import com.hotel.repositories.RoleRepository;
import com.hotel.services.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PrivilegeRepository privilegeRepository;
    private final AppUserRepository appUserRepository;

    @Override
    public Role createRole(RoleDto roleDto) {

        List<String> validPrivilegesList = List.of("READ", "WRITE", "UPDATE", "DELETE");

        if(roleRepository.findByRoleName(roleDto.getRoleName()).isPresent()){
            return null;
        }

        Role role = new Role();
        role.setRoleName(roleDto.getRoleName());
        role.setRoleDescription(roleDto.getRoleDescription());

        List<Permissions> permissions = new ArrayList<>();

        for (PermissionDto permissionDto : roleDto.getPermissions()) {
            Permissions permission = new Permissions();
            permission.setUserPermission(permissionDto.getUserPermission());

            Privilege privilege = new Privilege();

            for (String prg : permissionDto.getPrivileges()) {
                if (validPrivilegesList.contains(prg)) {
                    switch (prg) {
                        case "READ" -> privilege.setReadPermission("READ");
                        case "WRITE" -> privilege.setWritePermission("WRITE");
                        case "UPDATE" -> privilege.setUpdatePermission("UPDATE");
                        case "DELETE" -> privilege.setDeletePermission("DELETE");
                    }
                }
            }
            permission.setPrivilege(privilege);
            permissions.add(permission);
        }

        role.setPermissions(permissions);
        return roleRepository.save(role);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        List<RoleDto> roleDtos=new ArrayList<>();

        for(Role role:roleRepository.findAll()){
            RoleDto roleDto=new RoleDto();
            roleDto.setRoleId(role.getId());
            roleDto.setRoleName(role.getRoleName());
            roleDto.setRoleDescription(role.getRoleDescription());

            List<PermissionDto> permissionDtos=new ArrayList<>();
            for(Permissions permission:role.getPermissions()){
                PermissionDto permissionDto=new PermissionDto();
                List<String> privileges=new ArrayList<>();
                permissionDto.setUserPermission(permission.getUserPermission());
                privileges.add(permission.getPrivilege().getReadPermission());
                privileges.add(permission.getPrivilege().getWritePermission());
                privileges.add(permission.getPrivilege().getUpdatePermission());
                privileges.add(permission.getPrivilege().getDeletePermission());

                permissionDto.setPrivileges(privileges);
                permissionDtos.add(permissionDto);
            }

            roleDto.setPermissions(permissionDtos);
            roleDtos.add(roleDto);
        }

        return roleDtos;
    }

    @Override
    public List<Permissions> createPermissions(List<Permissions> permissions) {
        return permissionRepository.saveAll(permissions);
    }

    @Override
    public Privilege createPrivilege(Privilege privilege) {
        return privilegeRepository.save(privilege);
    }

    @Override
    public RoleDto getRoleByRoleName(String roleName) {
        Optional<Role> role=roleRepository.findByRoleName(roleName);
            if(role.isEmpty()){
                return null;
            }

        RoleDto roleDto=new RoleDto();

        roleDto.setRoleId(role.get().getId());
        roleDto.setRoleName(role.get().getRoleName());
        roleDto.setRoleDescription(role.get().getRoleDescription());

        List<PermissionDto> permissionDtos=new ArrayList<>();
        for(Permissions permission:role.get().getPermissions()){
            PermissionDto permissionDto=new PermissionDto();
            List<String> privileges=new ArrayList<>();
            permissionDto.setUserPermission(permission.getUserPermission());
            privileges.add(permission.getPrivilege().getReadPermission());
            privileges.add(permission.getPrivilege().getWritePermission());
            privileges.add(permission.getPrivilege().getUpdatePermission());
            privileges.add(permission.getPrivilege().getDeletePermission());

            permissionDto.setPrivileges(privileges);
            permissionDtos.add(permissionDto);
        }

        roleDto.setPermissions(permissionDtos);
        return roleDto;
    }

    @Override
    public Role updateRole(RoleDto roleDto) {

        List<String> validPrivilegesList = List.of("READ", "WRITE", "UPDATE", "DELETE");

        Optional<Role> existedRole=roleRepository.findById(roleDto.getRoleId());


        existedRole.get().setRoleName(roleDto.getRoleName());
        existedRole.get().setRoleDescription(roleDto.getRoleDescription());

        if (existedRole.get().getPermissions() != null) {
            existedRole.get().getPermissions().clear();

            List<Permissions> permissions=new ArrayList<>();
            for (PermissionDto prms:roleDto.getPermissions()){
                Permissions permission=new Permissions();
                permission.setUserPermission(prms.getUserPermission());

                Privilege privilege = new Privilege();

                for (String prg : prms.getPrivileges()) {
                    if (validPrivilegesList.contains(prg)) {
                        switch (prg) {
                            case "READ" -> privilege.setReadPermission("READ");
                            case "WRITE" -> privilege.setWritePermission("WRITE");
                            case "UPDATE" -> privilege.setUpdatePermission("UPDATE");
                            case "DELETE" -> privilege.setDeletePermission("DELETE");
                        }
                    }
                }
                permission.setPrivilege(privilege);

                permissions.add(permission);

            }
            existedRole.get().getPermissions().addAll(permissions);
        } else {
            List<Permissions> permissions=new ArrayList<>();
            for (PermissionDto prms:roleDto.getPermissions()){
                Permissions permission=new Permissions();
                permission.setUserPermission(prms.getUserPermission());

                Privilege privilege = new Privilege();

                for (String prg : prms.getPrivileges()) {
                    if (validPrivilegesList.contains(prg)) {
                        switch (prg) {
                            case "READ" -> privilege.setReadPermission("READ");
                            case "WRITE" -> privilege.setWritePermission("WRITE");
                            case "UPDATE" -> privilege.setUpdatePermission("UPDATE");
                            case "DELETE" -> privilege.setDeletePermission("DELETE");
                        }
                    }
                }
                permission.setPrivilege(privilege);

                permissions.add(permission);

            }
            existedRole.get().setPermissions(permissions);
        }

        return roleRepository.save(existedRole.get());
    }

    @Override
    public boolean deleteRole(Long roleId) {
        if(appUserRepository.countByRole_Id(roleId)>0){
            return false;
        }

        if(roleRepository.findById(roleId).isEmpty()){
            return false;
        }

        List<Permissions> permissions=permissionRepository.getPermissionsByRole_Id(roleId);
        if(!permissions.isEmpty()){
            for(Permissions permission:permissions){
                privilegeRepository.deleteById(permission.getPrivilege().getPrivilegeId());
                permissionRepository.deleteById(permission.getId());
            }
        }
        roleRepository.deleteById(roleId);
        return true;
    }

    @Override
    public Role findById(Long roleId) {
        return roleRepository.findById(roleId).get();
    }
}
