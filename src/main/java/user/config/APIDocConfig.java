package user.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.web.bind.annotation.RestController;

@RestController
@OpenAPIDefinition(info =
@Info(
        title = "User Management Service",
        version = "1.0",
        description = "Documentation for User Management Service",
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/"),
        contact = @Contact(url = "http://www.skakumanu.com", name = "Srikanth", email = "srikanth@skakumanu.com")
))
public class APIDocConfig {}
