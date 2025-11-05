package ua.lviv.bas.cinema.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
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
	private final EmailTokenGeneratorService emailTokenGeneratorService;
	private final EmailTokenService emailTokenService;

	@Transactional
	public UserProfileResponse registerUser(UserRegistrationRequest userDto) {
		log.info("Attempting to register user with email: {}", userDto.getEmail());

		if (existsByEmail(userDto.getEmail())) {
			throw new EmailAlreadyExistsException("Email is already registered: " + userDto.getEmail());
		}

		String encodedPassword = passwordEncoder.encode(userDto.getPassword());
		User user = userMapper.toEntityWithPassword(userDto, encodedPassword);

		User savedUser = userRepository.save(user);

		emailTokenGeneratorService.generateVerificationToken(userDto.getEmail());

		log.info("User registered successfully with email: {}", userDto.getEmail());

		return userMapper.toProfileResponse(savedUser);
	}

	@Transactional
	public UserProfileResponse updateUser(Long userId, UserUpdateRequest updateRequest) {
		log.info("Updating user with ID: {}", userId);

		User user = findById(userId);
		userMapper.updateUserFromDto(updateRequest, user);

		User updatedUser = userRepository.save(user);
		log.info("User with ID: {} updated successfully", userId);

		return userMapper.toProfileResponse(updatedUser);
	}

	@Transactional
	public void requestEmailChange(Long userId, String newEmail) {
		log.info("Requesting email change for user ID: {} to {}", userId, newEmail);

		if (existsByEmail(newEmail)) {
			throw new EmailAlreadyExistsException("Email is already registered: " + newEmail);
		}

		User user = findById(userId);

		emailTokenGeneratorService.generateEmailChangeToken(user.getEmail(), newEmail);

		log.info("Email change requested successfully for user ID: {}", userId);
	}

	@Transactional
	public UserProfileResponse confirmEmailChange(String token) {
		log.info("Confirming email change with token: {}", token);

		User updatedUser = emailTokenService.confirmEmailChange(token);

		return userMapper.toProfileResponse(updatedUser);
	}

	@Transactional
	public void updateUserPassword(Long userId, String newPassword) {
		log.info("Updating password for user ID: {}", userId);

		User user = findById(userId);
		String encodedPassword = passwordEncoder.encode(newPassword);
		user.setPassword(encodedPassword);

		userRepository.save(user);
		log.info("Password updated for user ID: {}", userId);
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getUserProfileById(Long id) {
		User user = findById(id);
		return userMapper.toProfileResponse(user);
	}

	@Transactional(readOnly = true)
	public Optional<User> findOptionalByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Transactional(readOnly = true)
	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
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