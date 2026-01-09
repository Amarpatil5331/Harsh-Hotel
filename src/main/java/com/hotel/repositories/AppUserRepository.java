package com.hotel.repositories;

import com.hotel.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> getMyUserByUsername(String username);

//    Page<AppUser> findAll(Pageable pageable);

    @Query("select count(*) from AppUser u where u.role.id=:id")
    long countByRole_Id(@Param("id") Long role_id);

    List<AppUser> getAppUserByHotel_Id(Long id);
    List<AppUser> getAppUserByIsDeletedTrue();

     boolean existsByUsername(String username);
}
