package ua.lviv.bas.cinema.service.notification;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	@Value("${app.company.name:Cinema}")
	private String companyName;

	@Async
	public void sendVerificationEmail(String toEmail, String token) {
		try {
			String link = frontendUrl + "/verify-email/" + token;
			String text = """
					Confirm Your Email Address

					Thank you for registering with %s!

					To activate your account, please click the link below:
					%s

					This link will expire in 10 minutes.

					If you didn't create an account, please ignore this email.

					Best regards,
					%s Team
					""".formatted(companyName, link, companyName);

			sendSimpleEmail(toEmail, "Confirm Your Email Address", text);
			log.info("Verification email sent to {}", toEmail);

		} catch (MailException e) {
			log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
			throw new ExternalServiceException("Email Service", e);
		}
	}

	@Async
	public void sendPasswordResetEmail(String toEmail, String token) {
		try {
			String link = frontendUrl + "/reset-password/" + token;
			String text = """
					Password Reset Request

					You have requested to reset your password for your %s account.

					To reset your password, click the link below:
					%s

					This link will expire in 10 minutes.

					If you didn't request a password reset, please ignore this email.

					Best regards,
					%s Team
					""".formatted(companyName, link, companyName);

			sendSimpleEmail(toEmail, "Password Reset Request", text);
			log.info("Password reset email sent to {}", toEmail);

		} catch (MailException e) {
			log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
			throw new ExternalServiceException("Email Service", e);
		}
	}

	@Async
	public void sendTicketsEmail(String toEmail, String bookingNumber, String movieTitle, String sessionTime,
			String hallName, BigDecimal amountPaid, String paymentMethod, String seatInfo) {
		try {
			String text = """
					Your Tickets - %s

					Booking Number: %s
					Movie: %s
					Time: %s
					Hall: %s
					Seats: %s
					Amount Paid: %s UAH
					Payment Method: %s

					Please present this information at the cinema entrance.
					Arrive 10-15 minutes before the session.

					Important:
					• Have your ID ready if required
					• No refunds 30 minutes before session
					• QR code will be available in your account

					Thank you for choosing %s!

					This is an automated email. Please do not reply.
					""".formatted(movieTitle, bookingNumber, movieTitle, sessionTime, hallName, seatInfo, amountPaid,
					paymentMethod, companyName);

			sendSimpleEmail(toEmail, "Your Tickets: " + movieTitle, text);
			log.info("Tickets email sent to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send tickets email to {}: {}", toEmail, e.getMessage());
		}
	}

	@Async
	public void sendPaymentFailedEmail(String toEmail, String bookingNumber, String movieTitle, String sessionTime,
			String errorMessage) {
		try {
			String text = """
					Payment Failed

					Booking: %s
					Movie: %s
					Time: %s

					Error: %s

					Please try again or contact support.

					%s Support
					""".formatted(bookingNumber, movieTitle, sessionTime, errorMessage, companyName);

			sendSimpleEmail(toEmail, "Payment Failed: " + movieTitle, text);
			log.info("Payment failed email sent to {}", toEmail);

		} catch (Exception e) {
			log.error("Failed to send payment failed email to {}: {}", toEmail, e.getMessage());
		}
	}

	@Async
	public void sendEmailChangeConfirmation(String toEmail, String token) {
		try {
			String link = frontendUrl + "/confirm-email-change/" + token;
			String text = """
					Confirm Your Email Change

					You have requested to change your %s account email address.

					To confirm this change, please click the link below:
					%s

					This link will expire in 24 hours.

					If you didn't request this change, please ignore this email.

					Best regards,
					%s Team
					""".formatted(companyName, link, companyName);

			sendSimpleEmail(toEmail, "Confirm Your Email Change", text);
			log.info("Email change confirmation sent to {}", toEmail);

		} catch (MailException e) {
			log.error("Failed to send email change confirmation to {}: {}", toEmail, e.getMessage());
			throw new ExternalServiceException("Email Service", e);
		}
	}

	@Async
	public void sendEmailChangeNotification(String oldEmail, String newEmail) {
		try {
			String text = """
					Email Address Changed

					Your %s account email address has been successfully changed:

					Old email: %s
					New email: %s

					If you didn't make this change, please contact our support team immediately.

					Best regards,
					%s Team
					""".formatted(companyName, oldEmail, newEmail, companyName);

			sendSimpleEmail(oldEmail, "Email Address Changed", text);
			log.info("Email change notification sent to {}", oldEmail);

		} catch (MailException e) {
			log.error("Failed to send email change notification to {}: {}", oldEmail, e.getMessage());
		}
	}

	private void sendSimpleEmail(String toEmail, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(toEmail);
		message.setSubject(subject);
		message.setText(text);
		mailSender.send(message);
	}
}