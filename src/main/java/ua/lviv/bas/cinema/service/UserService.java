package ua.lviv.bas.cinema.service;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.UserRepository;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {

	private static final Logger logger = LogManager.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder bCryptPasswordEncoder;

	public void save(UserRegistrationDto userDto) {
		logger.info("Attempting to register user with email: {}", userDto.getEmail());

		if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
			logger.error("Registration failed: Email {} is already registered", userDto.getEmail());

			throw new RuntimeException("Email is already registered");
		}

		if (!userDto.getPassword().equals(userDto.getPasswordConfirm())) {
			logger.error("Registration failed: Passwords do not match for email {}", userDto.getEmail());
			throw new IllegalArgumentException("Passwords do not match");
		}

		User user = UserMapper.toEntity(userDto, bCryptPasswordEncoder);

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
