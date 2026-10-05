package com.users.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataUserProfileRepository extends JpaRepository<UserProfileJpaEntity, UUID> {
}
