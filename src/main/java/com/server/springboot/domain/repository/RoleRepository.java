package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Role;
import com.server.springboot.domain.enumeration.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(AppRole roleName);

}
