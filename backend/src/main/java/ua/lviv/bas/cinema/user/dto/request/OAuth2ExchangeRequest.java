package ua.lviv.bas.cinema.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "OAuth2 one-time exchange code request")
public record OAuth2ExchangeRequest(
        @Schema(description = "One-time code issued after a successful OAuth2 login", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Code is required")
        String code
) {
}
