package ua.lviv.bas.cinema.config.api;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Cinema API", version = "1.0.0", description = "REST API for cinema management system", contact = @Contact(name = "Developer", email = "dev@example.com"), license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")), servers = {
        @Server(description = "Local Server", url = "http://localhost:8080")}, security = @SecurityRequirement(name = "jwtCookie"))
@SecurityScheme(name = "jwtCookie", description = "JWT set as an httpOnly cookie by POST /api/auth/login — log in via Swagger UI's Try it out on that endpoint first, the cookie is then sent automatically", type = SecuritySchemeType.APIKEY, paramName = "jwt", in = SecuritySchemeIn.COOKIE)
public class OpenApiConfig {
}