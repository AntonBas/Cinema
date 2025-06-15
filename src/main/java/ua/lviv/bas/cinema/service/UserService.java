package ua.lviv.bas.cinema.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ua.lviv.bas.cinema.dao.UserRepository;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;

@Service
public class UserService {

	private static final Logger logger = LogManager.getLogger(UserService.class);

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder bCryptPasswordEncoder;

	public void save(UserRegistrationDto userDto) {
		logger.info("Attempting to register user with email: {}", userDto.getEmail());

		if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
			logger.error("Registration failde: Email {} is already registered", userDto.getEmail());

			throw new RuntimeException("Email is already registered");
		}

		User user = new User();
		user.setEmail(userDto.getEmail());
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setDateOfBirth(userDto.getDateOfBirth());
		user.setCity(userDto.getCity());
		user.setPhoneNumber(userDto.getPhoneNumber());
		user.setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
		user.setPasswordConfirm(bCryptPasswordEncoder.encode(userDto.getPasswordConfirm()));
		user.setUserRole(UserRole.ROLE_USER);

		userRepository.save(user);
		logger.info("User registered successfully with email: {}", userDto.getEmail());
	}

	public Optional<User> findOptionalByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
	}

	public User updateUser(User user) {
		return userRepository.save(user);
	}
	
	public void verifyEmail(String email) {
		System.out.println("Email is confirmed" + email);
	}
}
