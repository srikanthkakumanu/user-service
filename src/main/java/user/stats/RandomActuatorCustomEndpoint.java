package user.stats;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Service;

/**
 * An example of custom endpoint is created to add it in actuator.
 */
@Service
@Endpoint(id = "random")
public class RandomActuatorCustomEndpoint {

    @ReadOperation
    public Integer random() {
        return (int) (Math.random() * 100);
    }
}
