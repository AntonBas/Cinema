package ua.lviv.bas.cinema.user.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;

import static org.assertj.core.api.Assertions.assertThat;

class VerificationBirthDateRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestHasNoViolations() {
        var request = new VerificationBirthDateRequest(VerificationStatus.VERIFIED);

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsNullVerificationStatus() {
        var request = new VerificationBirthDateRequest(null);

        assertThat(validator.validate(request)).isNotEmpty();
    }
}
