package ua.lviv.bas.cinema.exception.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    }

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController()).setControllerAdvice(new ApiErrorHandler())
                .build();
    }

    @Test
    void handleMaxUploadSizeExceededShouldReturnPayloadTooLarge() throws Exception {
        mockMvc.perform(get("/test/max-upload-size"))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message").value("Uploaded file is too large"));
    }
}
