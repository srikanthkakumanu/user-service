package com.users.domain.model;

import com.users.domain.exception.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ValueObjectsTest {

	@Test
	void userIdNormalisesAUuid() {
		assertThat(UserId.of(" 7C1F0E9A-3B5D-4F6A-8B7C-9D0E1F2A3B4C ").value())
				.isEqualTo("7c1f0e9a-3b5d-4f6a-8b7c-9d0e1f2a3b4c");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "42", "not-a-uuid" })
	void userIdRejectsAnythingElse(String value) {
		assertInvalid(() -> UserId.of(value), "id");
	}

	@Test
	void usernameIsTrimmedAndLowerCased() {
		assertThat(Username.of("  Alice.Doe-1 ").value()).isEqualTo("alice.doe-1");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "ab", ".alice", "alice doe", "alice@example", "ålice",
			"a-name-that-is-far-too-long-to-be-accepted-as-a-username" })
	void usernameRejectsInvalidValues(String value) {
		assertInvalid(() -> Username.of(value), "username");
	}

	@Test
	void emailIsTrimmedAndLowerCased() {
		assertThat(Email.of(" Alice.Doe+tag@Example.COM ").value()).isEqualTo("alice.doe+tag@example.com");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { "alice", "alice@", "@example.com", "alice@example", "alice@@example.com",
			"alice@-example.com", "a lice@example.com" })
	void emailRejectsInvalidValues(String value) {
		assertInvalid(() -> Email.of(value), "email");
	}

	@Test
	void emailRejectsOverlongAddresses() {
		assertInvalid(() -> Email.of("a".repeat(250) + "@example.com"), "email");
	}

	@Test
	void personNameIsTrimmedAndJoined() {
		var name = PersonName.of(" Alice ", " Doe ");

		assertThat(name.firstName()).isEqualTo("Alice");
		assertThat(name.fullName()).isEqualTo("Alice Doe");
	}

	@Test
	void personNameRequiresBothPartsWithinLength() {
		assertInvalid(() -> PersonName.of(" ", "Doe"), "firstName");
		assertInvalid(() -> PersonName.of("Alice", null), "lastName");
		assertInvalid(() -> PersonName.of("A".repeat(101), "Doe"), "firstName");
	}

	@Test
	void passwordNeverPrintsItsValue() {
		var password = Password.of("S3cret!Passw0rd");

		assertThat(password.toString()).doesNotContain("S3cret").isEqualTo("Password[***]");
		assertThat(password.value()).isEqualTo("S3cret!Passw0rd");
	}

	@Test
	void passwordRejectsBlankAndOverlongValues() {
		assertInvalid(() -> Password.of(" "), "password");
		assertInvalid(() -> Password.of(null), "password");
		assertInvalid(() -> Password.of("x".repeat(257)), "password");
	}

	@Test
	void userSearchValidatesPaging() {
		var search = new UserSearch("  ali ", null, 2, 20);

		assertThat(search.query()).isEqualTo("ali");
		assertThat(search.offset()).isEqualTo(40);
		assertThat(new UserSearch(" ", null, 0, 1).query()).isNull();
		assertInvalid(() -> new UserSearch(null, null, -1, 20), "page");
		assertInvalid(() -> new UserSearch(null, null, 0, 0), "size");
		assertInvalid(() -> new UserSearch(null, null, 0, 101), "size");
	}

	@Test
	void pageResultMapsItems() {
		var page = new PageResult<>(java.util.List.of(1, 2), 7, 0, 2).map(String::valueOf);

		assertThat(page.items()).containsExactly("1", "2");
		assertThat(page.total()).isEqualTo(7);
	}

	private static void assertInvalid(Runnable action, String field) {
		assertThatExceptionOfType(InvalidValueException.class).isThrownBy(action::run)
				.satisfies(ex -> assertThat(ex.field()).isEqualTo(field))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("invalid-value"));
	}
}
