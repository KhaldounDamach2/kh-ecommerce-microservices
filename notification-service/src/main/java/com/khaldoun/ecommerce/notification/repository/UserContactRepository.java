package com.khaldoun.ecommerce.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khaldoun.ecommerce.notification.domain.UserContact;

public interface UserContactRepository extends JpaRepository<UserContact, Long> {
}
