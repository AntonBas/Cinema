package ua.lviv.bas.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

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
			SimpleMailMessage message = buildEmailMessage(toEmail, "Confirm Your Email Address",
					createVerificationEmailText(link));

			mailSender.send(message);
			log.info("Verification email sent successfully to {}", toEmail);

		} catch (MailException e) {
			log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
			throw new ExternalServiceException("Email Service", e);
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
			throw new ExternalServiceException("Email Service", e);
		}
	}

	@Async
	public void sendEmailChangeConfirmation(String toEmail, String token) {
		try {
			String link = createEmailChangeLink(token);
			SimpleMailMessage message = buildEmailMessage(toEmail, "Confirm Your Email Change",
					createEmailChangeConfirmationText(link));

			mailSender.send(message);
			log.info("Email change confirmation sent successfully to {}", toEmail);

		} catch (MailException e) {
			log.error("Failed to send email change confirmation to {}: {}", toEmail, e.getMessage());
			throw new ExternalServiceException("Email Service", e);
		}
	}

	@Async
	public void sendEmailChangeNotification(String oldEmail, String newEmail) {
		try {
			SimpleMailMessage message = buildEmailMessage(oldEmail, "Email Address Changed",
					createEmailChangeNotificationText(oldEmail, newEmail));

			mailSender.send(message);
			log.info("Email change notification sent successfully to {}", oldEmail);

		} catch (MailException e) {
			log.error("Failed to send email change notification to {}: {}", oldEmail, e.getMessage());
			// Не кидаємо exception для notification, щоб не блокувати основний потік
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

	private String createEmailChangeLink(String token) {
		return frontendUrl + "/confirm-email-change/" + token;
	}

	private String createPasswordResetLink(String token) {
		return frontendUrl + "/reset-password/" + token;
	}

	private String createVerificationEmailText(String link) {
		return """
				Confirm Your Email Address

				Thank you for registering with Cinema!

				To activate your account, please click the link below:
				%s

				This link will expire in 10 minutes.

				If you didn't create an account, please ignore this email.

				Best regards,
				Cinema Team
				""".formatted(link);
	}

	private String createEmailChangeConfirmationText(String link) {
		return """
				Confirm Your Email Change

				You have requested to change your Cinema account email address.

				To confirm this change, please click the link below:
				%s

				This link will expire in 24 hours.

				If you didn't request this change, please ignore this email - your email address will remain unchanged.

				Best regards,
				Cinema Team
				""".formatted(link);
	}

	private String createEmailChangeNotificationText(String oldEmail, String newEmail) {
		return """
				Email Address Changed

				Your Cinema account email address has been successfully changed:

				Old email: %s
				New email: %s

				If you didn't make this change, please contact our support team immediately.

				Best regards,
				Cinema Team
				""".formatted(oldEmail, newEmail);
	}

	private String createPasswordResetEmailText(String link) {
		return """
				Password Reset Request

				You have requested to reset your password for your Cinema account.

				To reset your password, click the link below:
				%s

				This link will expire in 10 minutes.

				If you didn't request a password reset, please ignore this email.

				Best regards,
				Cinema Team
				""".formatted(link);
	}
}