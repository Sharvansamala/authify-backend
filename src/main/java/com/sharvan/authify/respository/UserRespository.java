package com.sharvan.authify.respository;

import com.sharvan.authify.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRespository extends JpaRepository<UserEntity,Long> {
    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);
}
