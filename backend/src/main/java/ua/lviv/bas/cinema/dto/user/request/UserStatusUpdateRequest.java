package ua.lviv.bas.cinema.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating user account status")
public class UserStatusUpdateRequest {

	@Schema(description = "Account enabled status (true = active, false = disabled)", example = "true")
	private boolean enabled;
}