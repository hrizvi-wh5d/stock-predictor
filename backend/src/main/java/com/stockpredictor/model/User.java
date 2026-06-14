package com.stockpredictor.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

// @Entity tells JPA to create a database TABLE called "users" for this class
// Each field below becomes a COLUMN in that table
@Entity
@Table(name = "users")
@Data               // Lombok: auto-generates getters, setters, toString
@NoArgsConstructor  // Lombok: auto-generates empty constructor
@AllArgsConstructor // Lombok: auto-generates constructor with all fields
public class User {

    // @Id = primary key, @GeneratedValue = auto-increment (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(unique=true) means no two users can have the same username
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // We NEVER store plain text passwords - always store the hashed version
    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String fullName;

    @Column(length = 10)
    private String preferredMarket = "NASDAQ"; // NASDAQ or FTSE

    // Automatically set when a user registers
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
