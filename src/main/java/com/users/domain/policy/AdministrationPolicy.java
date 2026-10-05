package com.users.domain.policy;

import com.users.domain.exception.OperationNotPermittedException;
import com.users.domain.model.Actor;
import com.users.domain.model.UserId;

/**
 * Guards against privilege escalation through user administration. Holding {@code users:write}
 * is not enough to take over or remove a platform administrator.
 */
public final class AdministrationPolicy {

	/**
	 * Changing a platform administrator's account or credentials is reserved for platform
	 * administrators; otherwise a user administrator could reset that password and sign in as one.
	 */
	public void checkMayAdminister(Actor actor, boolean targetIsPlatformAdmin) {
		if (targetIsPlatformAdmin && !actor.isPlatformAdmin()) {
			throw new OperationNotPermittedException("Only a platform administrator may administer a platform administrator");
		}
	}

	/** The platform must always keep one administrator who can sign in. */
	public void checkMayRemoveAccess(UserId target, boolean targetIsLastPlatformAdmin) {
		if (targetIsLastPlatformAdmin) {
			throw new OperationNotPermittedException("The last platform administrator cannot be disabled, locked or deleted");
		}
	}

	/** Nobody locks themselves out by accident. */
	public void checkNotSelf(Actor actor, UserId target, String operation) {
		if (actor.is(target)) {
			throw new OperationNotPermittedException("You cannot " + operation + " your own account");
		}
	}
}
