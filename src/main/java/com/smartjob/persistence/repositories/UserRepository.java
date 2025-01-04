package com.smartjob.persistence.repositories;

import com.smartjob.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.token = :token, u.modified = :modified, u.lastLogin = :lastLogin WHERE u.email = :email")
    void updateTokenUserByEmail(String email,String token, LocalDateTime modified, LocalDateTime lastLogin);
}
