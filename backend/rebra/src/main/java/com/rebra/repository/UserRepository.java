package com.rebra.repository;

import com.rebra.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySub(String sub);
    
    boolean existsByNickname(String nickname);
}