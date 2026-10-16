package ua.lviv.bas.cinema.exception.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.data.core.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import ua.lviv.bas.cinema.user.exception.ResendCooldownException;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiErrorHandlerTest {

    @RestController
    static class ThrowingController {
        @GetMapping("/test/max-upload-size")
        void trigger() {
            throw new MaxUploadSizeExceededException(10_000_000L);
        }

        @GetMapping("/test/constraint-violation")
        void triggerConstraintViolation() {
            throw new DataIntegrityViolationException("insert failed",
                    new org.hibernate.exception.ConstraintViolationException("duplicate key",
                            new java.sql.SQLException("duplicate key"), "uk_user_promotion"));
        }

        @GetMapping("/test/resend-cooldown")
        void triggerResendCooldown() {
            throw new ResendCooldownException(42);
        }

        @GetMapping("/test/optimistic-lock")
        void triggerOptimisticLock() {
            throw new ObjectOptimisticLockingFailureException("Payment", 1L);
        }

        @GetMapping("/test/uuid/{sessionId}")
        void triggerTypeMismatch(@PathVariable UUID sessionId) {
        }

        @PostMapping("/test/validated")
        void triggerValidation(@Valid @RequestBody ValidatedRequest request) {
        }

        @GetMapping("/test/invalid-sort")
        void triggerInvalidSort() {
            throw new PropertyReferenceException("unknownProperty", TypeInformation.of(Object.class), List.of());
        }
    }

    record ValidatedRequest(@Size(min = 2, max = 50, message = "City name must be between 2 and 50 characters")
                            @NotBlank(message = "City is required") String city) {
    }

    private ApiErrorHandler apiErrorHandler;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        apiErrorHandler = new ApiErrorHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController()).setControllerAdvice(apiErrorHandler)
                .build();
    }

    @Test
    void handleMaxUploadSizeExceededShouldReturnPayloadTooLarge() throws Exception {
        mockMvc.perform(get("/test/max-upload-size"))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.message").value("Uploaded file is too large"));
    }

    @Test
    void handleDataIntegrityViolationWithHibernateConstraintCauseShouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Database constraint violation"));
    }

    @Test
    void handleOptimisticLockShouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test/optimistic-lock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409))
                .andExpect(jsonPath("$.message").value("The resource was modified concurrently, please retry"));
    }

    @Test
    void unsupportedMethodShouldReturnApiErrorWithAllowHeader() throws Exception {
        mockMvc.perform(post("/test/max-upload-size"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.statusCode").value(405))
                .andExpect(jsonPath("$.path").value("/test/max-upload-size"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void debugMessageShouldBeOmittedByDefault() throws Exception {
        mockMvc.perform(get("/test/max-upload-size"))
                .andExpect(jsonPath("$.debugMessage").doesNotExist());
    }

    @Test
    void debugMessageShouldBeIncludedWhenDebugErrorsEnabled() throws Exception {
        ReflectionTestUtils.setField(apiErrorHandler, "debugErrorsEnabled", true);

        mockMvc.perform(get("/test/max-upload-size"))
                .andExpect(jsonPath("$.debugMessage").exists());
    }

    @Test
    void handleResendCooldownShouldReturnTooManyRequestsWithRetryAfterHeader() throws Exception {
        mockMvc.perform(get("/test/resend-cooldown"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "42"))
                .andExpect(jsonPath("$.message").value("Please wait before requesting another verification email"));
    }

    @Test
    void handleTypeMismatchShouldNotEchoRawValue() throws Exception {
        mockMvc.perform(get("/test/uuid/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sessionId"));
    }

    @Test
    void unknownPathShouldReturnGenericNotFound() throws Exception {
        mockMvc.perform(post("/api/auth/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @Test
    void validationShouldReportOnlyPresenceErrorForBlankField() throws Exception {
        mockMvc.perform(post("/test/validated").contentType(MediaType.APPLICATION_JSON).content("{\"city\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.subErrors.length()").value(1))
                .andExpect(jsonPath("$.subErrors[0].message").value("City is required"));
    }

    @Test
    void handlePropertyReferenceShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/invalid-sort"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sort property: unknownProperty"));
    }
}
