package user.config.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class UserAuthEvents {

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent ase) {
        log.info("User {} login successful", ase.getAuthentication().getName());
        // We can perform some actions by sending emails or SMSs
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent afe) {
        log.error("User {} login failed", afe.getAuthentication().getName());
    }
}
