package com.users.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.users.domain.event.DomainEvent;
import com.users.domain.model.Actor;
import com.users.domain.model.UserId;

/** Shared fixtures: fake ports, a fixed clock and the actors used across the use-case tests. */
abstract class UseCaseTest {

	static final Instant NOW = Instant.parse("2026-10-05T10:00:00Z");

	final FakeIdentityProvider identityProvider = new FakeIdentityProvider();
	final InMemoryProfiles profiles = new InMemoryProfiles();
	final List<DomainEvent> published = new ArrayList<>();
	final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

	final Actor userAdmin = new Actor(UserId.of("aaaaaaaa-0000-0000-0000-000000000001"), Set.of("USER", "USER_ADMIN"));
	final Actor platformAdmin = new Actor(UserId.of("aaaaaaaa-0000-0000-0000-000000000002"),
			Set.of("USER", "PLATFORM_ADMIN"));

	void publish(DomainEvent event) {
		published.add(event);
	}

	List<String> publishedTypes() {
		return published.stream().map(DomainEvent::type).toList();
	}
}
