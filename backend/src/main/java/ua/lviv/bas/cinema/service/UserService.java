package ua.lviv.bas.cinema.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
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
	private final EmailTokenService emailTokenService;
	private final EmailTokenGeneratorService emailTokenGeneratorService;

	@Transactional
	public UserResponse registerUser(UserRegistrationRequest request) {
		log.info("Registering user with email: {}", request.getEmail());

		if (existsByEmail(request.getEmail())) {
			throw new EmailAlreadyExistsException("Email is already registered: " + request.getEmail());
		}

		User user = userMapper.toEntity(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		User saved = userRepository.save(user);
		log.info("User registered successfully: {}", saved.getEmail());

		emailTokenGeneratorService.generateVerificationToken(saved.getEmail());
		return userMapper.toDto(saved);
	}

	@Transactional
	public UserProfileResponse updateUser(Long userId, UserUpdateRequest request) {
		User user = findById(userId);
		userMapper.updateUserFromDto(request, user);
		return userMapper.toProfileResponse(userRepository.save(user));
	}

	@Transactional
	public void requestEmailChange(Long userId, String newEmail) {
		User user = findById(userId);
		if (existsByEmail(newEmail)) {
			throw new EmailAlreadyExistsException("Email is already registered: " + newEmail);
		}
		emailTokenGeneratorService.generateEmailChangeToken(user.getEmail(), newEmail);
		log.info("Email change token generated for user {}", userId);
	}

	@Transactional
	public UserProfileResponse confirmEmailChange(String token) {
		User updated = emailTokenService.confirmEmailChange(token);
		return userMapper.toProfileResponse(updated);
	}

	@Transactional
	public void updateUserPassword(Long userId, String newPassword) {
		User user = findById(userId);
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		log.info("Password updated for user {}", userId);
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getUserProfile(Long id) {
		return userMapper.toProfileResponse(findById(id));
	}

	@Transactional(readOnly = true)
	public UserResponse getUserById(Long id) {
		return userMapper.toDto(findById(id));
	}

	@Transactional(readOnly = true)
	public User findByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
	}

	@Transactional(readOnly = true)
	public User findById(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}
}