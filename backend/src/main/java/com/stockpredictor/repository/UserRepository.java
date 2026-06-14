package com.stockpredictor.repository;

import com.stockpredictor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// JpaRepository gives us FREE database methods:
// save(), findById(), findAll(), delete(), count() etc.
// We just add our own custom ones below using method naming conventions
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring auto-generates the SQL: SELECT * FROM users WHERE username = ?
    Optional<User> findByUsername(String username);

    // Spring auto-generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Check if username already taken: SELECT COUNT(*) FROM users WHERE username = ?
    boolean existsByUsername(String username);

    // Check if email already registered
    boolean existsByEmail(String email);
}
