package com.smart_hire.auth.repository;

import com.smart_hire.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    java.util.Optional<User> findByUsername(String username);
}
