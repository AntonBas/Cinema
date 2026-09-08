package ua.lviv.bas.cinema.booking.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.booking.dto.request.BookingCreateRequest.SeatSelectionRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingCreateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestHasNoViolations() {
        var request = new BookingCreateRequest(1L, List.of(new SeatSelectionRequest(1L, 1L)), 0);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void cascadesValidationIntoEachSeatSelection() {
        var request = new BookingCreateRequest(1L, List.of(new SeatSelectionRequest(null, null)), 0);

        var violations = validator.validate(request);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("seats[0].seatId", "seats[0].ticketTypeId");
    }
}
