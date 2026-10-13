package com.khaldoun.ecommerce.notification.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserContact {

    // Same id as in auth-service, so no @GeneratedValue
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 20)
    private String role;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
