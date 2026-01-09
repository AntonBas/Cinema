package ua.lviv.bas.cinema.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

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
import ua.lviv.bas.cinema.service.notification.EmailService;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

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
	void sendVerificationEmail_ShouldSendEmail_WhenValidParameters() {
		String toEmail = "test@example.com";
		String token = "verification-token-123";

		emailService.sendVerificationEmail(toEmail, token);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertThat(sentMessage).isNotNull();
		assertThat(sentMessage.getFrom()).isEqualTo(fromEmail);
		assertThat(sentMessage.getTo()).containsExactly(toEmail);
		assertThat(sentMessage.getSubject()).isEqualTo("Confirm Your Email Address");
		assertThat(sentMessage.getText()).contains(frontendUrl + "/verify-email/" + token);
		assertThat(sentMessage.getText()).contains("10 minutes");
		assertThat(sentMessage.getText()).contains(companyName);
	}

	@Test
	void sendPasswordResetEmail_ShouldSendEmail_WhenValidParameters() {
		String toEmail = "test@example.com";
		String token = "reset-token-456";

		emailService.sendPasswordResetEmail(toEmail, token);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertThat(sentMessage).isNotNull();
		assertThat(sentMessage.getFrom()).isEqualTo(fromEmail);
		assertThat(sentMessage.getTo()).containsExactly(toEmail);
		assertThat(sentMessage.getSubject()).isEqualTo("Password Reset Request");
		assertThat(sentMessage.getText()).contains(frontendUrl + "/reset-password/" + token);
		assertThat(sentMessage.getText()).contains("10 minutes");
		assertThat(sentMessage.getText()).contains(companyName);
	}

	@Test
	void sendEmailChangeConfirmation_ShouldSendEmail_WhenValidParameters() {
		String toEmail = "new@example.com";
		String token = "email-change-token-789";

		emailService.sendEmailChangeConfirmation(toEmail, token);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertThat(sentMessage).isNotNull();
		assertThat(sentMessage.getFrom()).isEqualTo(fromEmail);
		assertThat(sentMessage.getTo()).containsExactly(toEmail);
		assertThat(sentMessage.getSubject()).isEqualTo("Confirm Your Email Change");
		assertThat(sentMessage.getText()).contains(frontendUrl + "/confirm-email-change/" + token);
		assertThat(sentMessage.getText()).contains("24 hours");
		assertThat(sentMessage.getText()).contains(companyName);
	}

	@Test
	void sendEmailChangeNotification_ShouldSendEmail_WhenValidParameters() {
		String oldEmail = "old@example.com";
		String newEmail = "new@example.com";

		emailService.sendEmailChangeNotification(oldEmail, newEmail);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertThat(sentMessage).isNotNull();
		assertThat(sentMessage.getFrom()).isEqualTo(fromEmail);
		assertThat(sentMessage.getTo()).containsExactly(oldEmail);
		assertThat(sentMessage.getSubject()).isEqualTo("Email Address Changed");
		assertThat(sentMessage.getText()).contains(oldEmail);
		assertThat(sentMessage.getText()).contains(newEmail);
		assertThat(sentMessage.getText()).contains(companyName);
	}

	@Test
	void sendTicketsEmail_ShouldSendEmail_WhenValidParameters() {
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

		assertThat(sentMessage).isNotNull();
		assertThat(sentMessage.getFrom()).isEqualTo(fromEmail);
		assertThat(sentMessage.getTo()).containsExactly(toEmail);
		assertThat(sentMessage.getSubject()).isEqualTo("Your Tickets: " + movieTitle);
		assertThat(sentMessage.getText()).contains(bookingNumber);
		assertThat(sentMessage.getText()).contains(movieTitle);
		assertThat(sentMessage.getText()).contains(sessionTime);
		assertThat(sentMessage.getText()).contains(hallName);
		assertThat(sentMessage.getText()).contains("450.00");
		assertThat(sentMessage.getText()).contains(paymentMethod);
		assertThat(sentMessage.getText()).contains(seatInfo);
		assertThat(sentMessage.getText()).contains(companyName);
		assertThat(sentMessage.getText()).contains("10-15 minutes");
	}

	@Test
	void sendPaymentFailedEmail_ShouldSendEmail_WhenValidParameters() {
		String toEmail = "customer@example.com";
		String bookingNumber = "BK-2024-002";
		String movieTitle = "Interstellar";
		String sessionTime = "2024-01-16 20:00";
		String errorMessage = "Insufficient funds";

		emailService.sendPaymentFailedEmail(toEmail, bookingNumber, movieTitle, sessionTime, errorMessage);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertThat(sentMessage).isNotNull();
		assertThat(sentMessage.getFrom()).isEqualTo(fromEmail);
		assertThat(sentMessage.getTo()).containsExactly(toEmail);
		assertThat(sentMessage.getSubject()).isEqualTo("Payment Failed: " + movieTitle);
		assertThat(sentMessage.getText()).contains(bookingNumber);
		assertThat(sentMessage.getText()).contains(movieTitle);
		assertThat(sentMessage.getText()).contains(sessionTime);
		assertThat(sentMessage.getText()).contains(errorMessage);
		assertThat(sentMessage.getText()).contains(companyName);
	}

	@Test
	void sendEmailChangeNotification_ShouldNotThrow_WhenMailSendingFails() {
		String oldEmail = "old@example.com";
		String newEmail = "new@example.com";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		assertThatCode(() -> emailService.sendEmailChangeNotification(oldEmail, newEmail)).doesNotThrowAnyException();

		verify(mailSender).send(any(SimpleMailMessage.class));
	}

	@Test
	void sendVerificationEmail_ShouldThrowException_WhenMailSendingFails() {
		String toEmail = "test@example.com";
		String token = "verification-token-123";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		assertThatThrownBy(() -> emailService.sendVerificationEmail(toEmail, token))
				.isInstanceOf(ExternalServiceException.class).hasMessageContaining("Email Service");
	}

	@Test
	void sendPasswordResetEmail_ShouldThrowException_WhenMailSendingFails() {
		String toEmail = "test@example.com";
		String token = "reset-token-456";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		assertThatThrownBy(() -> emailService.sendPasswordResetEmail(toEmail, token))
				.isInstanceOf(ExternalServiceException.class).hasMessageContaining("Email Service");
	}

	@Test
	void sendEmailChangeConfirmation_ShouldThrowException_WhenMailSendingFails() {
		String toEmail = "new@example.com";
		String token = "email-change-token-789";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		assertThatThrownBy(() -> emailService.sendEmailChangeConfirmation(toEmail, token))
				.isInstanceOf(ExternalServiceException.class).hasMessageContaining("Email Service");
	}
}