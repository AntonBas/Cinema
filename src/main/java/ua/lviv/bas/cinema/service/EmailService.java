package ua.lviv.bas.cinema.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private static final Logger logger = LogManager.getLogger(EmailService.class);

	private final JavaMailSender mailSender;

	public void sendVerificationEmail(String toEmail, String token) {
		String link = "http://localhost:8080/verify-email?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Confirmation of registration");
		message.setText("Thank you for registering!\n\nTo activate your account, follow this link: " + link
				+ "\nThis link will expire in 10 minutes.");

		mailSender.send(message);
		logger.info("Verification email sent to {}", toEmail);
	}

	public void sendPasswordResetEmail(String toEmail, String token) {
		String link = "http://localhost:8080/reset-password?token=" + token;
		
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Password Reset Request");
		message.setText(
				"To reset your password, click the link: " + link + "\nThis link will expire in 10 minutes.");
		mailSender.send(message);
		logger.info("Password reset email sent to {}", toEmail);
	}
}
