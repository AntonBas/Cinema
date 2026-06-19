package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update user enabled/disabled status")
public record UserStatusUpdateRequest(
        @Schema(description = "Account enabled status (true = active, false = disabled)", example = "true")
        boolean enabled
) {
}