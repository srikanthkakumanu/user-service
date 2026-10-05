package com.users.domain.model;

import java.time.Instant;
import java.util.Objects;

import com.users.domain.exception.InvalidValueException;

/**
 * Application-side attributes of a user, owned by this service and keyed by the user's ID.
 * Nothing here is needed to authenticate or authorize, so none of it is in the token.
 */
public final class UserProfile {

	private static final int MAX_SHORT = 100;
	private static final int MAX_BIO = 1000;

	private final UserId userId;
	private String phoneNumber;
	private String jobTitle;
	private String department;
	private String locale;
	private String timeZone;
	private String bio;
	private final Instant createdAt;
	private Instant updatedAt;

	private UserProfile(UserId userId, String phoneNumber, String jobTitle, String department, String locale,
			String timeZone, String bio, Instant createdAt, Instant updatedAt) {
		this.userId = Objects.requireNonNull(userId, "userId");
		this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
		this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
		apply(phoneNumber, jobTitle, department, locale, timeZone, bio);
	}

	public static UserProfile empty(UserId userId, Instant now) {
		return new UserProfile(userId, null, null, null, null, null, null, now, now);
	}

	public static UserProfile rehydrate(UserId userId, String phoneNumber, String jobTitle, String department,
			String locale, String timeZone, String bio, Instant createdAt, Instant updatedAt) {
		return new UserProfile(userId, phoneNumber, jobTitle, department, locale, timeZone, bio, createdAt, updatedAt);
	}

	/** Replaces every attribute; a null or blank value clears that attribute. */
	public void update(String phoneNumber, String jobTitle, String department, String locale, String timeZone,
			String bio, Instant now) {
		apply(phoneNumber, jobTitle, department, locale, timeZone, bio);
		this.updatedAt = Objects.requireNonNull(now, "now");
	}

	private void apply(String phoneNumber, String jobTitle, String department, String locale, String timeZone,
			String bio) {
		String phone = text("phoneNumber", phoneNumber, 30);
		if (phone != null && !phone.matches("\\+?[0-9][0-9 ()-]{5,28}")) {
			throw new InvalidValueException("phoneNumber", "phoneNumber is not a valid phone number");
		}
		String zone = text("timeZone", timeZone, MAX_SHORT);
		if (zone != null && !java.time.ZoneId.getAvailableZoneIds().contains(zone)) {
			throw new InvalidValueException("timeZone", "timeZone is not a known time zone");
		}
		String tag = text("locale", locale, 35);
		if (tag != null && java.util.Locale.forLanguageTag(tag).getLanguage().isEmpty()) {
			throw new InvalidValueException("locale", "locale is not a valid language tag");
		}
		String title = text("jobTitle", jobTitle, MAX_SHORT);
		String unit = text("department", department, MAX_SHORT);
		String about = text("bio", bio, MAX_BIO);
		// Assign only after every value has passed, so a rejected update changes nothing.
		this.phoneNumber = phone;
		this.jobTitle = title;
		this.department = unit;
		this.locale = tag;
		this.timeZone = zone;
		this.bio = about;
	}

	private static String text(String field, String value, int maxLength) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String stripped = value.strip();
		if (stripped.length() > maxLength) {
			throw new InvalidValueException(field, field + " must be at most " + maxLength + " characters");
		}
		return stripped;
	}

	public UserId userId() {
		return userId;
	}

	public String phoneNumber() {
		return phoneNumber;
	}

	public String jobTitle() {
		return jobTitle;
	}

	public String department() {
		return department;
	}

	public String locale() {
		return locale;
	}

	public String timeZone() {
		return timeZone;
	}

	public String bio() {
		return bio;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
