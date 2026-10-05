package com.users.infrastructure.event;

import com.users.domain.event.DomainEvent;
import com.users.domain.port.DomainEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events inside the process and logs them. Sending them to a broker later
 * means replacing this adapter; publishers and the events themselves stay as they are.
 */
@Component
class LoggingDomainEventPublisher implements DomainEventPublisher {

	private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

	private final ApplicationEventPublisher publisher;

	LoggingDomainEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public void publish(DomainEvent event) {
		log.info("Domain event {} for user {} at {}", event.type(), event.userId(), event.occurredAt());
		publisher.publishEvent(event);
	}
}
