package ua.lviv.bas.cinema.cinema.dto.hall.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Full seat layout of a cinema hall")
public record HallLayoutRequest(
        @Schema(description = "All seats the hall should have after this update", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Seats list is required")
        @Valid
        List<SeatLayoutItemRequest> seats
) {
}
