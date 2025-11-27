package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

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

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(emailService, "frontendUrl", frontendUrl);
		ReflectionTestUtils.setField(emailService, "fromEmail", fromEmail);
	}

	@Test
	void sendVerificationEmail_ShouldSendEmail_WhenValidParameters() {
		String toEmail = "test@example.com";
		String token = "verification-token-123";

		emailService.sendVerificationEmail(toEmail, token);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertNotNull(sentMessage);
		assertEquals(fromEmail, sentMessage.getFrom());
		assertArrayEquals(new String[] { toEmail }, sentMessage.getTo());
		assertEquals("Confirm Your Email Address", sentMessage.getSubject());
		String messageText = sentMessage.getText();
		assertNotNull(messageText);
		assertTrue(messageText.contains(frontendUrl + "/verify-email/" + token));
		assertTrue(messageText.contains("This link will expire in 10 minutes"));
	}

	@Test
	void sendPasswordResetEmail_ShouldSendEmail_WhenValidParameters() {
		String toEmail = "test@example.com";
		String token = "reset-token-456";

		emailService.sendPasswordResetEmail(toEmail, token);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertNotNull(sentMessage);
		assertEquals(fromEmail, sentMessage.getFrom());
		assertArrayEquals(new String[] { toEmail }, sentMessage.getTo());
		assertEquals("Password Reset Request", sentMessage.getSubject());
		String messageText = sentMessage.getText();
		assertNotNull(messageText);
		assertTrue(messageText.contains(frontendUrl + "/reset-password/" + token));
		assertTrue(messageText.contains("This link will expire in 10 minutes"));
	}

	@Test
	void sendEmailChangeConfirmation_ShouldSendEmail_WhenValidParameters() {
		String toEmail = "new@example.com";
		String token = "email-change-token-789";

		emailService.sendEmailChangeConfirmation(toEmail, token);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertNotNull(sentMessage);
		assertEquals(fromEmail, sentMessage.getFrom());
		assertArrayEquals(new String[] { toEmail }, sentMessage.getTo());
		assertEquals("Confirm Your Email Change", sentMessage.getSubject());
		String messageText = sentMessage.getText();
		assertNotNull(messageText);
		assertTrue(messageText.contains(frontendUrl + "/confirm-email-change/" + token));
		assertTrue(messageText.contains("This link will expire in 24 hours"));
	}

	@Test
	void sendEmailChangeNotification_ShouldSendEmail_WhenValidParameters() {
		String oldEmail = "old@example.com";
		String newEmail = "new@example.com";

		emailService.sendEmailChangeNotification(oldEmail, newEmail);

		verify(mailSender).send(messageCaptor.capture());
		SimpleMailMessage sentMessage = messageCaptor.getValue();

		assertNotNull(sentMessage);
		assertEquals(fromEmail, sentMessage.getFrom());
		assertArrayEquals(new String[] { oldEmail }, sentMessage.getTo());
		assertEquals("Email Address Changed", sentMessage.getSubject());
		String messageText = sentMessage.getText();
		assertNotNull(messageText);
		assertTrue(messageText.contains(oldEmail));
		assertTrue(messageText.contains(newEmail));
	}

	@Test
	void sendEmailChangeNotification_ShouldNotThrow_WhenMailSendingFails() {
		String oldEmail = "old@example.com";
		String newEmail = "new@example.com";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		assertDoesNotThrow(() -> emailService.sendEmailChangeNotification(oldEmail, newEmail));

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

		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> emailService.sendVerificationEmail(toEmail, token));

		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Email Service"));
	}

	@Test
	void sendPasswordResetEmail_ShouldThrowException_WhenMailSendingFails() {
		String toEmail = "test@example.com";
		String token = "reset-token-456";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> emailService.sendPasswordResetEmail(toEmail, token));

		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Email Service"));
	}

	@Test
	void sendEmailChangeConfirmation_ShouldThrowException_WhenMailSendingFails() {
		String toEmail = "new@example.com";
		String token = "email-change-token-789";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> emailService.sendEmailChangeConfirmation(toEmail, token));

		assertNotNull(exception);
		assertTrue(exception.getMessage().contains("Email Service"));
	}

	@Test
	void createLinks_ShouldGenerateCorrectUrls() {
		String token = "test-token-123";

		String verificationLink = ReflectionTestUtils.invokeMethod(emailService, "createVerificationLink", token);
		String passwordResetLink = ReflectionTestUtils.invokeMethod(emailService, "createPasswordResetLink", token);
		String emailChangeLink = ReflectionTestUtils.invokeMethod(emailService, "createEmailChangeLink", token);

		assertEquals(frontendUrl + "/verify-email/" + token, verificationLink);
		assertEquals(frontendUrl + "/reset-password/" + token, passwordResetLink);
		assertEquals(frontendUrl + "/confirm-email-change/" + token, emailChangeLink);
	}

	@Test
	void emailContent_ShouldContainCorrectInformation() {
		String link = "http://test.com/confirm";

		String verificationText = ReflectionTestUtils.invokeMethod(emailService, "createVerificationEmailText", link);
		String passwordResetText = ReflectionTestUtils.invokeMethod(emailService, "createPasswordResetEmailText", link);
		String emailChangeText = ReflectionTestUtils.invokeMethod(emailService, "createEmailChangeConfirmationText",
				link);
		String emailNotificationText = ReflectionTestUtils.invokeMethod(emailService,
				"createEmailChangeNotificationText", "old@test.com", "new@test.com");

		assertNotNull(verificationText);
		assertTrue(verificationText.contains(link));
		assertTrue(verificationText.contains("10 minutes"));

		assertNotNull(passwordResetText);
		assertTrue(passwordResetText.contains(link));
		assertTrue(passwordResetText.contains("10 minutes"));

		assertNotNull(emailChangeText);
		assertTrue(emailChangeText.contains(link));
		assertTrue(emailChangeText.contains("24 hours"));

		assertNotNull(emailNotificationText);
		assertTrue(emailNotificationText.contains("old@test.com"));
		assertTrue(emailNotificationText.contains("new@test.com"));
	}

	@Test
	void buildEmailMessage_ShouldBuildCorrectMessage() {
		String toEmail = "recipient@example.com";
		String subject = "Test Subject";
		String text = "Test email content";

		SimpleMailMessage message = ReflectionTestUtils.invokeMethod(emailService, "buildEmailMessage", toEmail,
				subject, text);

		assertNotNull(message);
		assertEquals(fromEmail, message.getFrom());
		assertArrayEquals(new String[] { toEmail }, message.getTo());
		assertEquals(subject, message.getSubject());
		assertEquals(text, message.getText());
	}

	private void assertArrayEquals(String[] expected, String[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], actual[i]);
		}
	}
}