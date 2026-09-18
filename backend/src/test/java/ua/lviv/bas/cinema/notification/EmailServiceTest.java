package ua.lviv.bas.cinema.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class EmailServiceTest {

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    private MockRestServiceServer mockServer;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService("test-api-key", "noreply@bascinema.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(emailService, "companyName", "Cinema");

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(BREVO_URL)
                .defaultHeader("api-key", "test-api-key");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(emailService, "restClient", builder.build());
    }

    @Test
    void sendVerificationEmail_ShouldPostToBrevoWithVerificationLink() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(method(POST))
                .andExpect(header("api-key", "test-api-key"))
                .andExpect(jsonPath("$.sender.email").value("noreply@bascinema.com"))
                .andExpect(jsonPath("$.to[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.subject").value("Confirm Your Email Address"))
                .andExpect(jsonPath("$.htmlContent", containsString("http://localhost:5173/verify-email/token-123")))
                .andExpect(jsonPath("$.htmlContent", containsString("10 minutes")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendVerificationEmail("test@example.com", "token-123");

        mockServer.verify();
    }

    @Test
    void sendVerificationEmail_WhenAllAttemptsFail_ShouldThrowExternalServiceException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> emailService.sendVerificationEmail("test@example.com", "token-123"))
                .isInstanceOf(ExternalServiceException.class).hasMessageContaining("Email Service");

        mockServer.verify();
    }

    @Test
    void sendVerificationEmail_WhenTransientFailureThenSuccess_ShouldNotThrow() {
        mockServer.expect(requestTo(BREVO_URL)).andRespond(withServerError());
        mockServer.expect(requestTo(BREVO_URL)).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatCode(() -> emailService.sendVerificationEmail("test@example.com", "token-123"))
                .doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void sendPasswordResetEmail_ShouldPostToBrevoWithResetLink() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.subject").value("Password Reset Request"))
                .andExpect(jsonPath("$.htmlContent", containsString("http://localhost:5173/reset-password/token-456")))
                .andExpect(jsonPath("$.htmlContent", containsString("10 minutes")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendPasswordResetEmail("test@example.com", "token-456");

        mockServer.verify();
    }

    @Test
    void sendPasswordResetEmail_WhenAllAttemptsFail_ShouldThrowExternalServiceException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> emailService.sendPasswordResetEmail("test@example.com", "token-456"))
                .isInstanceOf(ExternalServiceException.class);

        mockServer.verify();
    }

    @Test
    void sendEmailChangeConfirmation_ShouldPostToBrevoWithConfirmationLink() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.to[0].email").value("new@example.com"))
                .andExpect(jsonPath("$.subject").value("Confirm Your Email Change"))
                .andExpect(jsonPath("$.htmlContent",
                        containsString("http://localhost:5173/confirm-email-change/token-789")))
                .andExpect(jsonPath("$.htmlContent", containsString("24 hours")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendEmailChangeConfirmation("new@example.com", "token-789");

        mockServer.verify();
    }

    @Test
    void sendEmailChangeConfirmation_WhenAllAttemptsFail_ShouldThrowExternalServiceException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatThrownBy(() -> emailService.sendEmailChangeConfirmation("new@example.com", "token-789"))
                .isInstanceOf(ExternalServiceException.class);

        mockServer.verify();
    }

    @Test
    void sendTicketsEmail_ShouldPostToBrevoWithBookingDetails() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.subject").value("Your Tickets: Inception"))
                .andExpect(jsonPath("$.htmlContent", containsString("BK-2024-001")))
                .andExpect(jsonPath("$.htmlContent", containsString("Inception")))
                .andExpect(jsonPath("$.htmlContent", containsString("2024-01-15 18:30")))
                .andExpect(jsonPath("$.htmlContent", containsString("Hall A")))
                .andExpect(jsonPath("$.htmlContent", containsString("450.00")))
                .andExpect(jsonPath("$.htmlContent", containsString("Credit Card")))
                .andExpect(jsonPath("$.htmlContent", containsString("Row 5, Seats 12-13")))
                .andExpect(jsonPath("$.htmlContent", containsString("10-15 minutes")))
                .andExpect(jsonPath("$.htmlContent", containsString("automated email")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendTicketsEmail("customer@example.com", "BK-2024-001", "Inception", "2024-01-15 18:30",
                "Hall A", new BigDecimal("450.00"), "Credit Card", "Row 5, Seats 12-13");

        mockServer.verify();
    }

    @Test
    void sendTicketsEmail_WhenAllAttemptsFail_ShouldNotThrowException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatCode(() -> emailService.sendTicketsEmail("customer@example.com", "BK-2024-001", "Inception",
                "2024-01-15 18:30", "Hall A", new BigDecimal("450.00"), "Credit Card", "Row 5, Seats 12-13"))
                .doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void sendPaymentFailedEmail_ShouldPostToBrevoWithErrorDetails() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.subject").value("Payment Failed: Interstellar"))
                .andExpect(jsonPath("$.htmlContent", containsString("BK-2024-002")))
                .andExpect(jsonPath("$.htmlContent", containsString("Interstellar")))
                .andExpect(jsonPath("$.htmlContent", containsString("Insufficient funds")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendPaymentFailedEmail("customer@example.com", "BK-2024-002", "Interstellar",
                "2024-01-16 20:00", "Insufficient funds");

        mockServer.verify();
    }

    @Test
    void sendPaymentFailedEmail_WhenAllAttemptsFail_ShouldNotThrowException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatCode(() -> emailService.sendPaymentFailedEmail("customer@example.com", "BK-2024-002",
                "Interstellar", "2024-01-16 20:00", "Insufficient funds")).doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void sendRefundEmail_ShouldPostToBrevoWithRefundDetails() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.subject").value("Refund Confirmation: The Dark Knight"))
                .andExpect(jsonPath("$.htmlContent", containsString("BK-2024-003")))
                .andExpect(jsonPath("$.htmlContent", containsString("The Dark Knight")))
                .andExpect(jsonPath("$.htmlContent", containsString("Hall B")))
                .andExpect(jsonPath("$.htmlContent", containsString("300.00")))
                .andExpect(jsonPath("$.htmlContent", containsString("Row 6, Seat 8")))
                .andExpect(jsonPath("$.htmlContent", containsString("Customer request")))
                .andExpect(jsonPath("$.htmlContent", containsString("3-5 business days")))
                .andExpect(jsonPath("$.htmlContent", containsString("automated email")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendRefundEmail("customer@example.com", "BK-2024-003", "The Dark Knight", "2024-01-17 19:00",
                "Hall B", new BigDecimal("300.00"), "Row 6, Seat 8", "Customer request");

        mockServer.verify();
    }

    @Test
    void sendRefundEmail_ShouldContainCurrentTimestamp() {
        String expectedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.htmlContent", containsString(expectedDate)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendRefundEmail("customer@example.com", "BK-2024-003", "The Dark Knight", "2024-01-17 19:00",
                "Hall B", new BigDecimal("300.00"), "Row 6, Seat 8", "Customer request");

        mockServer.verify();
    }

    @Test
    void sendRefundEmail_WhenAllAttemptsFail_ShouldNotThrowException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatCode(() -> emailService.sendRefundEmail("customer@example.com", "BK-2024-003", "The Dark Knight",
                "2024-01-17 19:00", "Hall B", new BigDecimal("300.00"), "Row 6, Seat 8", "Customer request"))
                .doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void sendEmailChangeNotification_ShouldPostToBrevoWithOldAndNewEmail() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.to[0].email").value("old@example.com"))
                .andExpect(jsonPath("$.subject").value("Email Address Changed"))
                .andExpect(jsonPath("$.htmlContent", containsString("old@example.com")))
                .andExpect(jsonPath("$.htmlContent", containsString("new@example.com")))
                .andExpect(jsonPath("$.htmlContent", containsString("contact our support team immediately")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendEmailChangeNotification("old@example.com", "new@example.com");

        mockServer.verify();
    }

    @Test
    void sendEmailChangeNotification_WhenAllAttemptsFail_ShouldNotThrowException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatCode(() -> emailService.sendEmailChangeNotification("old@example.com", "new@example.com"))
                .doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void sendPasswordChangedNotification_ShouldPostToBrevoWithWarning() {
        mockServer.expect(requestTo(BREVO_URL))
                .andExpect(jsonPath("$.to[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.subject").value("Your Password Was Changed"))
                .andExpect(jsonPath("$.htmlContent", containsString("password was just changed")))
                .andExpect(jsonPath("$.htmlContent", containsString("contact our support team immediately")))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        emailService.sendPasswordChangedNotification("test@example.com");

        mockServer.verify();
    }

    @Test
    void sendPasswordChangedNotification_WhenAllAttemptsFail_ShouldNotThrowException() {
        mockServer.expect(ExpectedCount.times(3), requestTo(BREVO_URL)).andRespond(withServerError());

        assertThatCode(() -> emailService.sendPasswordChangedNotification("test@example.com"))
                .doesNotThrowAnyException();

        mockServer.verify();
    }

    @Test
    void sendSafely_WhenActionThrows_ShouldSwallowException() {
        assertThatCode(() -> emailService.sendSafely("send test email", 1L, () -> {
            throw new RuntimeException("boom");
        })).doesNotThrowAnyException();
    }
}
