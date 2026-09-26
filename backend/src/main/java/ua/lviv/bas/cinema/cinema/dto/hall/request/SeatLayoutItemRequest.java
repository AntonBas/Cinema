package ua.lviv.bas.cinema.cinema.dto.hall.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;

@Schema(description = "Single seat position and type in a hall layout")
public record SeatLayoutItemRequest(
        @Schema(description = "Existing seat id, omit or null for a new seat", example = "42")
        Long id,

        @Schema(description = "Row number shown to customers", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Row is required")
        @Positive(message = "Row must be positive")
        Integer row,

        @Schema(description = "Seat number within the row shown to customers", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Seat number is required")
        @Positive(message = "Seat number must be positive")
        Integer number,

        @Schema(description = "Type of the seat", example = "VIP", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Seat type is required")
        SeatType seatType,

        @Schema(description = "Horizontal position in the layout canvas", example = "120", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "X coordinate is required")
        @PositiveOrZero(message = "X coordinate cannot be negative")
        Integer x,

        @Schema(description = "Vertical position in the layout canvas", example = "60", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Y coordinate is required")
        @PositiveOrZero(message = "Y coordinate cannot be negative")
        Integer y,

        @Schema(description = "Whether the seat is active (bookable)", example = "true", defaultValue = "true")
        Boolean active
) {
    public SeatLayoutItemRequest {
        if (active == null) {
            active = true;
        }
    }
}
