package ua.lviv.bas.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${app.frontend.url:http://localhost:5173}")
	private String frontendUrl;

	@Value("${app.email.from:noreply@cinema.com}")
	private String fromEmail;

	@Async
	public void sendVerificationEmail(String toEmail, String token) {
		try {
			String link = createVerificationLink(token);
			SimpleMailMessage message = buildEmailMessage(toEmail, "Confirmation of registration",
					createVerificationEmailText(link));

			mailSender.send(message);
			log.info("Verification email sent successfully to {}", toEmail);

		} catch (MailException e) {
			log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
			throw new RuntimeException("Failed to send verification email", e);
		}
	}

	@Async
	public void sendPasswordResetEmail(String toEmail, String token) {
		try {
			String link = createPasswordResetLink(token);
			SimpleMailMessage message = buildEmailMessage(toEmail, "Password Reset Request",
					createPasswordResetEmailText(link));

			mailSender.send(message);
			log.info("Password reset email sent successfully to {}", toEmail);

		} catch (MailException e) {
			log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
			throw new RuntimeException("Failed to send password reset email", e);
		}
	}

	private SimpleMailMessage buildEmailMessage(String toEmail, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(toEmail);
		message.setSubject(subject);
		message.setText(text);
		return message;
	}

	private String createVerificationLink(String token) {
		return frontendUrl + "/verify-email/" + token;
	}

	private String createPasswordResetLink(String token) {
		return frontendUrl + "/reset-password/" + token;
	}

	private String createVerificationEmailText(String link) {
		return """
				Thank you for registering!

				To activate your account, follow this link: %s

				This link will expire in 10 minutes.

				If you didn't create an account, please ignore this email.
				""".formatted(link);
	}

	private String createPasswordResetEmailText(String link) {
		return """
				You have requested to reset your password.

				To reset your password, click the link: %s

				This link will expire in 10 minutes.

				If you didn't request a password reset, please ignore this email.
				""".formatted(link);
	}
}