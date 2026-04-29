package ua.lviv.bas.cinema.service.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> messageCaptor;

    private final String frontendUrl = "http://localhost:5173";
    private final String fromEmail = "noreply@cinema.com";
    private final String companyName = "Cinema";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "frontendUrl", frontendUrl);
        ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
        ReflectionTestUtils.setField(emailService, "companyName", companyName);
    }

    @Test
    void sendVerificationEmail_ShouldSendCriticalEmailWithCorrectContent() {
        String toEmail = "test@example.com";
        String token = "verification-token-123";

        emailService.sendVerificationEmail(toEmail, token);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getFrom()).isEqualTo(fromEmail);
        assertThat(sentMessage.getTo()).containsExactly(toEmail);
        assertThat(sentMessage.getSubject()).isEqualTo("Confirm Your Email Address");
        assertThat(sentMessage.getText()).contains(frontendUrl + "/verify-email/" + token).contains("10 minutes")
                .contains(companyName);
    }

    @Test
    void sendVerificationEmail_WhenMailException_ShouldThrowExternalServiceException() {
        String toEmail = "test@example.com";
        String token = "verification-token-123";
        doThrow(new MailException("SMTP error") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendVerificationEmail(toEmail, token))
                .isInstanceOf(ExternalServiceException.class).hasMessageContaining("Email Service");
    }

    @Test
    void sendPasswordResetEmail_ShouldSendCriticalEmailWithCorrectContent() {
        String toEmail = "test@example.com";
        String token = "reset-token-456";

        emailService.sendPasswordResetEmail(toEmail, token);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getSubject()).isEqualTo("Password Reset Request");
        assertThat(sentMessage.getText()).contains(frontendUrl + "/reset-password/" + token).contains("10 minutes");
    }

    @Test
    void sendPasswordResetEmail_WhenMailException_ShouldThrowExternalServiceException() {
        String toEmail = "test@example.com";
        String token = "reset-token-456";
        doThrow(new MailException("SMTP error") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendPasswordResetEmail(toEmail, token))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void sendEmailChangeConfirmation_ShouldSendCriticalEmailWithCorrectContent() {
        String toEmail = "new@example.com";
        String token = "email-change-token-789";

        emailService.sendEmailChangeConfirmation(toEmail, token);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getSubject()).isEqualTo("Confirm Your Email Change");
        assertThat(sentMessage.getText()).contains(frontendUrl + "/confirm-email-change/" + token).contains("24 hours");
    }

    @Test
    void sendEmailChangeConfirmation_WhenMailException_ShouldThrowExternalServiceException() {
        String toEmail = "new@example.com";
        String token = "email-change-token-789";
        doThrow(new MailException("SMTP error") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailService.sendEmailChangeConfirmation(toEmail, token))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void sendTicketsEmail_ShouldSendNonCriticalEmailWithCorrectContent() {
        String toEmail = "customer@example.com";
        String bookingNumber = "BK-2024-001";
        String movieTitle = "Inception";
        String sessionTime = "2024-01-15 18:30";
        String hallName = "Hall A";
        BigDecimal amountPaid = new BigDecimal("450.00");
        String paymentMethod = "Credit Card";
        String seatInfo = "Row 5, Seats 12-13";

        emailService.sendTicketsEmail(toEmail, bookingNumber, movieTitle, sessionTime, hallName, amountPaid,
                paymentMethod, seatInfo);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getSubject()).isEqualTo("Your Tickets: " + movieTitle);
        assertThat(sentMessage.getText()).contains(bookingNumber).contains(movieTitle).contains(sessionTime)
                .contains(hallName).contains("450.00").contains(paymentMethod).contains(seatInfo)
                .contains("10-15 minutes").contains("This is an automated email");
    }

    @Test
    void sendTicketsEmail_WhenMailException_ShouldNotThrowException() {
        String toEmail = "customer@example.com";
        String bookingNumber = "BK-2024-001";
        String movieTitle = "Inception";
        String sessionTime = "2024-01-15 18:30";
        String hallName = "Hall A";
        BigDecimal amountPaid = new BigDecimal("450.00");
        String paymentMethod = "Credit Card";
        String seatInfo = "Row 5, Seats 12-13";

        doThrow(new MailException("SMTP error") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendTicketsEmail(toEmail, bookingNumber, movieTitle, sessionTime, hallName,
                amountPaid, paymentMethod, seatInfo)).doesNotThrowAnyException();
    }

    @Test
    void sendPaymentFailedEmail_ShouldSendNonCriticalEmailWithCorrectContent() {
        String toEmail = "customer@example.com";
        String bookingNumber = "BK-2024-002";
        String movieTitle = "Interstellar";
        String sessionTime = "2024-01-16 20:00";
        String errorMessage = "Insufficient funds";

        emailService.sendPaymentFailedEmail(toEmail, bookingNumber, movieTitle, sessionTime, errorMessage);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getSubject()).isEqualTo("Payment Failed: " + movieTitle);
        assertThat(sentMessage.getText()).contains(bookingNumber).contains(movieTitle).contains(sessionTime)
                .contains(errorMessage);
    }

    @Test
    void sendPaymentFailedEmail_WhenMailException_ShouldNotThrowException() {
        String toEmail = "customer@example.com";
        String bookingNumber = "BK-2024-002";
        String movieTitle = "Interstellar";
        String sessionTime = "2024-01-16 20:00";
        String errorMessage = "Insufficient funds";

        doThrow(new MailException("SMTP error") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendPaymentFailedEmail(toEmail, bookingNumber, movieTitle, sessionTime,
                errorMessage)).doesNotThrowAnyException();
    }

    @Test
    void sendRefundEmail_ShouldSendNonCriticalEmailWithCorrectContent() {
        String toEmail = "customer@example.com";
        String bookingNumber = "BK-2024-003";
        String movieTitle = "The Dark Knight";
        String sessionTime = "2024-01-17 19:00";
        String hallName = "Hall B";
        BigDecimal refundAmount = new BigDecimal("300.00");
        String seatInfo = "Row 6, Seat 8";
        String refundReason = "Customer request";

        emailService.sendRefundEmail(toEmail, bookingNumber, movieTitle, sessionTime, hallName, refundAmount, seatInfo,
                refundReason);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getSubject()).isEqualTo("Refund Confirmation: " + movieTitle);
        assertThat(sentMessage.getText()).contains(bookingNumber).contains(movieTitle).contains(sessionTime)
                .contains(hallName).contains("300.00").contains(seatInfo).contains(refundReason)
                .contains("3-5 business days").contains("This is an automated email");
    }

    @Test
    void sendRefundEmail_WhenMailException_ShouldNotThrowException() {
        String toEmail = "customer@example.com";
        String bookingNumber = "BK-2024-003";
        String movieTitle = "The Dark Knight";
        String sessionTime = "2024-01-17 19:00";
        String hallName = "Hall B";
        BigDecimal refundAmount = new BigDecimal("300.00");
        String seatInfo = "Row 6, Seat 8";
        String refundReason = "Customer request";

        doThrow(new MailException("SMTP error") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendRefundEmail(toEmail, bookingNumber, movieTitle, sessionTime, hallName,
                refundAmount, seatInfo, refundReason)).doesNotThrowAnyException();
    }

    @Test
    void sendEmailChangeNotification_ShouldSendNonCriticalEmailWithCorrectContent() {
        String oldEmail = "old@example.com";
        String newEmail = "new@example.com";

        emailService.sendEmailChangeNotification(oldEmail, newEmail);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getSubject()).isEqualTo("Email Address Changed");
        assertThat(sentMessage.getText()).contains(oldEmail).contains(newEmail)
                .contains("contact our support team immediately");
    }

    @Test
    void sendEmailChangeNotification_WhenMailException_ShouldNotThrowException() {
        String oldEmail = "old@example.com";
        String newEmail = "new@example.com";

        doThrow(new MailException("SMTP error") {
        }).when(mailSender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> emailService.sendEmailChangeNotification(oldEmail, newEmail)).doesNotThrowAnyException();
    }

    @Test
    void sendRefundEmail_ShouldContainCurrentTimestamp() {
        String toEmail = "customer@example.com";
        String bookingNumber = "BK-2024-003";
        String movieTitle = "The Dark Knight";
        String sessionTime = "2024-01-17 19:00";
        String hallName = "Hall B";
        BigDecimal refundAmount = new BigDecimal("300.00");
        String seatInfo = "Row 6, Seat 8";
        String refundReason = "Customer request";

        String expectedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        emailService.sendRefundEmail(toEmail, bookingNumber, movieTitle, sessionTime, hallName, refundAmount, seatInfo,
                refundReason);

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getText()).contains(expectedDate);
    }
}