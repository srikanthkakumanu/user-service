package com.users.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Persistence shape of the extended profile. Kept apart from the domain {@code UserProfile}. */
@Entity
@Table(name = "user_profile")
class UserProfileJpaEntity {

	@Id
	@Column(name = "user_id")
	private UUID userId;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "job_title")
	private String jobTitle;

	@Column(name = "department")
	private String department;

	@Column(name = "locale")
	private String locale;

	@Column(name = "time_zone")
	private String timeZone;

	@Column(name = "bio")
	private String bio;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Version
	@Column(name = "version", nullable = false)
	private long version;

	protected UserProfileJpaEntity() {
	}

	UserProfileJpaEntity(UUID userId, Instant createdAt) {
		this.userId = userId;
		this.createdAt = createdAt;
	}

	void apply(String phoneNumber, String jobTitle, String department, String locale, String timeZone, String bio,
			Instant updatedAt) {
		this.phoneNumber = phoneNumber;
		this.jobTitle = jobTitle;
		this.department = department;
		this.locale = locale;
		this.timeZone = timeZone;
		this.bio = bio;
		this.updatedAt = updatedAt;
	}

	UUID getUserId() {
		return userId;
	}

	String getPhoneNumber() {
		return phoneNumber;
	}

	String getJobTitle() {
		return jobTitle;
	}

	String getDepartment() {
		return department;
	}

	String getLocale() {
		return locale;
	}

	String getTimeZone() {
		return timeZone;
	}

	String getBio() {
		return bio;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	Instant getUpdatedAt() {
		return updatedAt;
	}
}
