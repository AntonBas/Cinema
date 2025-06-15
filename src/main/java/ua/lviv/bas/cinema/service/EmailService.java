package ua.lviv.bas.cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	public void sendVerificationEmail(String toEmail, String token) {
		String link = "http://localhost:8080/verify-email?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(toEmail);
		message.setSubject("Confirmation of registration");
		message.setText("Thank you for registering!\n\nTo activate your account, follow this link: " + link);

		mailSender.send(message);
	}
}
