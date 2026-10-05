package com.users.domain.policy;

import java.util.Set;

import com.users.domain.exception.OperationNotPermittedException;
import com.users.domain.model.Actor;
import com.users.domain.model.UserId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AdministrationPolicyTest {

	private static final UserId ALICE = UserId.of("7c1f0e9a-3b5d-4f6a-8b7c-9d0e1f2a3b4c");
	private static final UserId BOB = UserId.of("11111111-2222-3333-4444-555555555555");

	private final AdministrationPolicy policy = new AdministrationPolicy();
	private final Actor userAdmin = new Actor(ALICE, Set.of("USER", "USER_ADMIN"));
	private final Actor platformAdmin = new Actor(ALICE, Set.of("USER", "PLATFORM_ADMIN"));

	@Test
	void aUserAdminCannotAdministerAPlatformAdmin() {
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> policy.checkMayAdminister(userAdmin, true))
				.satisfies(ex -> assertThat(ex.code()).isEqualTo("operation-not-permitted"));
	}

	@Test
	void aUserAdminMayAdministerOrdinaryUsersAndAPlatformAdminMayAdministerAnyone() {
		assertThatCode(() -> policy.checkMayAdminister(userAdmin, false)).doesNotThrowAnyException();
		assertThatCode(() -> policy.checkMayAdminister(platformAdmin, true)).doesNotThrowAnyException();
	}

	@Test
	void theLastPlatformAdminCannotLoseAccess() {
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> policy.checkMayRemoveAccess(BOB, true));
		assertThatCode(() -> policy.checkMayRemoveAccess(BOB, false)).doesNotThrowAnyException();
	}

	@Test
	void nobodyRemovesTheirOwnAccess() {
		assertThatExceptionOfType(OperationNotPermittedException.class)
				.isThrownBy(() -> policy.checkNotSelf(platformAdmin, ALICE, "delete"))
				.withMessageContaining("delete");
		assertThatCode(() -> policy.checkNotSelf(platformAdmin, BOB, "delete")).doesNotThrowAnyException();
	}
}
