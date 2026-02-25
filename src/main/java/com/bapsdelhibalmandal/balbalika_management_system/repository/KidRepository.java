package com.bapsdelhibalmandal.balbalika_management_system.repository;

import com.bapsdelhibalmandal.balbalika_management_system.model.Kid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KidRepository extends JpaRepository<Kid,Long> {

    List<Kid> findBySabhaKshetra_KshetraId(Integer sabhaKshetraId);

    @Query("SELECT k FROM Kid k JOIN k.roles r WHERE r.roleId = :roleId")
    List<Kid> findByRoleId(@Param("roleId") Integer roleId);


}
