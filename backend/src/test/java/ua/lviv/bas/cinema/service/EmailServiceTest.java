package ua.lviv.bas.cinema.service;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
		assertEquals("Confirmation of registration", sentMessage.getSubject());
		String messageText = sentMessage.getText();
		assertNotNull(messageText);
		assertTrue(messageText.contains(frontendUrl + "/verify-email/" + token));
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
	}

	@Test
	void sendVerificationEmail_ShouldThrowException_WhenMailSendingFails() {
		String toEmail = "test@example.com";
		String token = "verification-token-123";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> emailService.sendVerificationEmail(toEmail, token));

		assertEquals("Failed to send verification email", exception.getMessage());
		assertNotNull(exception.getCause());
	}

	@Test
	void sendPasswordResetEmail_ShouldThrowException_WhenMailSendingFails() {
		String toEmail = "test@example.com";
		String token = "reset-token-456";

		MailException mailException = new MailException("SMTP error") {
			private static final long serialVersionUID = 1L;
		};

		doThrow(mailException).when(mailSender).send(any(SimpleMailMessage.class));

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> emailService.sendPasswordResetEmail(toEmail, token));

		assertEquals("Failed to send password reset email", exception.getMessage());
		assertNotNull(exception.getCause());
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
}