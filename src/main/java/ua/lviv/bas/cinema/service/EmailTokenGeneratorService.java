package ua.lviv.bas.cinema.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.dao.EmailTokenRepository;
import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TokenType;

@Service
public class EmailTokenGeneratorService {

	@Autowired
	private EmailTokenRepository tokenRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private EmailService emailService;

	public void generateVerificationToken(String email) {
		User user = userService.findByEmail(email);
		if (user == null)
			throw new RuntimeException("There is no user with this email address.");

		String token = UUID.randomUUID().toString();
		EmailToken emailToken = new EmailToken();
		emailToken.setToken(token);
		emailToken.setUser(user);
		emailToken.setCreatedAt(LocalDateTime.now());
		emailToken.setExpiresAt(LocalDateTime.now().plusHours(1));
		emailToken.setType(TokenType.VERIFICATION);

		tokenRepository.save(emailToken);

		emailService.sendVerificationEmail(email, token);
	}
}
