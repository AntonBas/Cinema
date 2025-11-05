package ua.lviv.bas.cinema.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

	@Transactional
	public UserResponse registerUser(UserRegistrationRequest userDto) {
		log.info("Attempting to register user with email: {}", userDto.getEmail());

		if (existsByEmail(userDto.getEmail())) {
			throw new EmailAlreadyExistsException("Email is already registered: " + userDto.getEmail());
		}

		String encodedPassword = passwordEncoder.encode(userDto.getPassword());
		User user = userMapper.toEntityWithPassword(userDto, encodedPassword);

		User savedUser = userRepository.save(user);
		log.info("User registered successfully with email: {}", userDto.getEmail());

		return userMapper.toDto(savedUser);
	}

	@Transactional(readOnly = true)
	public Optional<User> findOptionalByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional(readOnly = true)
	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email)); // ✅ Змінено
	}

	@Transactional
	public User updateUser(User user) {
		log.info("Updating user with ID: {}", user.getId());
		return userRepository.save(user);
	}

	@Transactional
	public void verifyEmail(String email) {
		log.info("Verifying email: {}", email);
		User user = findByEmail(email);
		user.setEnabled(true);
		userRepository.save(user);
		log.info("Email verified successfully for: {}", email);
	}

	@Transactional(readOnly = true)
	public User findById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.findByEmail(email).isPresent();
	}
}