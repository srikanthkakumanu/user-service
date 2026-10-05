package com.users.domain.port;

import com.users.domain.event.DomainEvent;

public interface DomainEventPublisher {

	void publish(DomainEvent event);
}
