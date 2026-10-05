package com.users.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import com.users.domain.model.UserId;
import com.users.domain.model.UserProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

/** The profile repository against real Postgres, with the schema created by Flyway and validated by Hibernate. */
@Testcontainers
@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaUserProfileRepository.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaUserProfileRepositoryIT {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

	private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
	private static final Instant LATER = Instant.parse("2026-02-01T00:00:00Z");

	@Autowired
	private JpaUserProfileRepository repository;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	void flywayCreatedTheSchema() {
		Integer migrations = jdbc.queryForObject("select count(*) from flyway_schema_history where success", Integer.class);

		assertThat(migrations).isEqualTo(1);
	}

	@Test
	void savesAndLoadsAProfile() {
		UserId id = newId();
		UserProfile profile = UserProfile.empty(id, CREATED);
		profile.update("+44 20 7946 0958", "Engineer", "Platform", "en-GB", "Europe/London", "Hello", LATER);

		repository.save(profile);

		UserProfile loaded = repository.findByUserId(id).orElseThrow();
		assertThat(loaded.phoneNumber()).isEqualTo("+44 20 7946 0958");
		assertThat(loaded.jobTitle()).isEqualTo("Engineer");
		assertThat(loaded.department()).isEqualTo("Platform");
		assertThat(loaded.locale()).isEqualTo("en-GB");
		assertThat(loaded.timeZone()).isEqualTo("Europe/London");
		assertThat(loaded.bio()).isEqualTo("Hello");
		assertThat(loaded.createdAt()).isEqualTo(CREATED);
		assertThat(loaded.updatedAt()).isEqualTo(LATER);
	}

	@Test
	void savingAgainUpdatesTheSameRowAndKeepsCreationTime() {
		UserId id = newId();
		repository.save(UserProfile.empty(id, CREATED));
		UserProfile profile = repository.findByUserId(id).orElseThrow();
		profile.update(null, "Lead", null, null, null, null, LATER);

		repository.save(profile);

		Integer rows = jdbc.queryForObject("select count(*) from user_profile where user_id = ?::uuid", Integer.class,
				id.value());
		UserProfile loaded = repository.findByUserId(id).orElseThrow();
		assertThat(rows).isEqualTo(1);
		assertThat(loaded.jobTitle()).isEqualTo("Lead");
		assertThat(loaded.createdAt()).isEqualTo(CREATED);
		assertThat(loaded.updatedAt()).isEqualTo(LATER);
	}

	@Test
	void anUnknownProfileIsAbsent() {
		assertThat(repository.findByUserId(newId())).isEmpty();
	}

	@Test
	void deletesAProfileAndToleratesDeletingAgain() {
		UserId id = newId();
		repository.save(UserProfile.empty(id, CREATED));

		repository.deleteByUserId(id);
		repository.deleteByUserId(id);

		assertThat(repository.findByUserId(id)).isEmpty();
	}

	private static UserId newId() {
		return UserId.of(UUID.randomUUID().toString());
	}
}
