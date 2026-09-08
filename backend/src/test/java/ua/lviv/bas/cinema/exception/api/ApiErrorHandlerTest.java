package ua.lviv.bas.cinema.exception.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message").value("Uploaded file is too large"));
    }

    @Test
    void handleDataIntegrityViolationWithHibernateConstraintCauseShouldReturnConflict() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Database constraint violation"));
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
}
