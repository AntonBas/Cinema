package ua.lviv.bas.cinema.service.user;

import java.time.LocalDate;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserPasswordUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidCurrentPasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordMismatchException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordValidationException;
import ua.lviv.bas.cinema.exception.domain.auth.SameEmailException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.notification.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.notification.EmailTokenService;

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
	@CacheEvict(value = "users", allEntries = true)
	public UserResponse registerUser(UserRegistrationRequest request) {
		log.info("Registering user with email: {}", request.getEmail());

		if (!request.getPassword().equals(request.getPasswordConfirm())) {
			throw new PasswordMismatchException();
		}

		if (userRepository.existsByEmail(request.getEmail())) {
			throw new EmailAlreadyExistsException(request.getEmail());
		}

		User user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		User saved = userRepository.save(user);
		log.info("User registered successfully: {}", saved.getEmail());

		emailTokenGeneratorService.generateVerificationToken(saved.getEmail());
		return userMapper.toUserResponse(saved);
	}

	@Transactional
	@CacheEvict(value = "users", key = "#userId")
	public UserProfileResponse updateUser(Long userId, UserUpdateRequest request) {
		User user = getById(userId);
		LocalDate oldDateOfBirth = user.getDateOfBirth();

		userMapper.updateUserFromRequest(request, user);

		if (request.getDateOfBirth() != null && !request.getDateOfBirth().equals(oldDateOfBirth)) {
			if (user.getVerificationStatus() == VerificationStatus.VERIFIED) {
				user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);
				user.setVerifiedAt(null);
				log.info("Birth date verification revoked for user {} (date changed from {} to {})", userId,
						oldDateOfBirth, request.getDateOfBirth());
			}
		}

		return userMapper.toUserProfileResponse(userRepository.save(user));
	}

	@Transactional
	public void requestEmailChange(Long userId, String newEmail) {
		User user = getById(userId);

		if (user.getEmail().equals(newEmail)) {
			throw new SameEmailException();
		}

		if (userRepository.existsByEmail(newEmail)) {
			throw new EmailAlreadyExistsException(newEmail);
		}

		emailTokenGeneratorService.generateEmailChangeToken(user.getEmail(), newEmail);
		log.info("Email change token generated for user {}", userId);
	}

	@Transactional
	@CacheEvict(value = "users", key = "#userId")
	public void updateUserPassword(Long userId, UserPasswordUpdateRequest request) {
		User user = getById(userId);

		if (!request.getNewPassword().equals(request.getPasswordConfirm())) {
			throw new PasswordMismatchException();
		}

		if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
			throw new InvalidCurrentPasswordException();
		}

		if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
			throw new SamePasswordException();
		}

		if (request.getNewPassword().length() < 8) {
			throw PasswordValidationException.tooShort(8);
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		log.info("Password updated for user {}", userId);
	}

	@Transactional
	public String confirmRegistration(String token) {
		return emailTokenService.confirmEmail(token);
	}

	@Transactional
	public UserProfileResponse confirmEmailChange(String token) {
		User updatedUser = emailTokenService.confirmEmailChange(token);
		return userMapper.toUserProfileResponse(updatedUser);
	}

	@Transactional(readOnly = true)
	public UserProfileResponse getUserProfile(Long id) {
		return userMapper.toUserProfileResponse(getById(id));
	}

	@Transactional(readOnly = true)
	public UserResponse getUserById(Long id) {
		return userMapper.toUserResponse(getById(id));
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#id")
	public User getById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#email")
	public User getByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public boolean existsById(Long id) {
		return userRepository.existsById(id);
	}
}