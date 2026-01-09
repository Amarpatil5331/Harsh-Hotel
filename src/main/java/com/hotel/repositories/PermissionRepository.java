package com.hotel.repositories;

import com.hotel.entity.Permissions;
import com.hotel.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permissions, Long> {
    Collection<Object> getPermissionsByRole(Role savedRole);

    List<Permissions> getPermissionsByRole_Id(Long roleId);
}
