package ua.lviv.bas.cinema.service.user;

import java.time.LocalDate;

import org.springframework.cache.annotation.CacheConfig;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "users")
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final EmailTokenGeneratorService emailTokenGeneratorService;

	@CacheEvict(allEntries = true)
	@Transactional
	public UserResponse registerUser(UserRegistrationRequest request) {
		validatePasswordMatch(request.getPassword(), request.getPasswordConfirm());
		validateEmailNotExists(request.getEmail());

		User user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.getPassword()));

		User saved = userRepository.save(user);
		log.info("User registered: {}", request.getEmail());

		emailTokenGeneratorService.generateVerificationToken(saved.getEmail());
		return userMapper.toUserResponse(saved);
	}

	@CacheEvict(key = "#userId")
	@Transactional
	public UserProfileResponse updateUser(Long userId, UserUpdateRequest request) {
		User user = getUserById(userId);
		userMapper.updateUserFromRequest(request, user);

		if (isDateOfBirthChanged(request.getDateOfBirth(), user.getDateOfBirth())) {
			revokeVerificationIfNeeded(user);
		}

		User updated = userRepository.save(user);
		return userMapper.toUserProfileResponse(updated);
	}

	@Transactional
	public void requestEmailChange(Long userId, String newEmail) {
		User user = getUserById(userId);
		validateNewEmail(user.getEmail(), newEmail);
		validateEmailNotExists(newEmail);
		emailTokenGeneratorService.generateEmailChangeToken(user.getEmail(), newEmail);
		log.info("Email change requested for user {} to {}", userId, newEmail);
	}

	@CacheEvict(key = "#userId")
	@Transactional
	public void updateUserPassword(Long userId, UserPasswordUpdateRequest request) {
		User user = getUserById(userId);
		validatePasswordMatch(request.getNewPassword(), request.getPasswordConfirm());
		validateCurrentPassword(user, request.getCurrentPassword());
		validateNewPasswordDifferent(user, request.getNewPassword());
		validatePasswordLength(request.getNewPassword());

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepository.save(user);
		log.info("Password updated for user {}", userId);
	}

	@Cacheable(key = "#id")
	public User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Cacheable(key = "#email")
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
	}

	public UserProfileResponse getUserProfile(Long id) {
		return userMapper.toUserProfileResponse(getUserById(id));
	}

	public UserResponse getUserResponseById(Long id) {
		return userMapper.toUserResponse(getUserById(id));
	}

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	private void validatePasswordMatch(String password, String confirm) {
		if (!password.equals(confirm)) {
			throw new PasswordMismatchException();
		}
	}

	private void validateEmailNotExists(String email) {
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyExistsException(email);
		}
	}

	private void validateCurrentPassword(User user, String currentPassword) {
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new InvalidCurrentPasswordException();
		}
	}

	private void validateNewPasswordDifferent(User user, String newPassword) {
		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new SamePasswordException();
		}
	}

	private void validatePasswordLength(String password) {
		if (password.length() < 8) {
			throw PasswordValidationException.tooShort(8);
		}
	}

	private void validateNewEmail(String currentEmail, String newEmail) {
		if (currentEmail.equalsIgnoreCase(newEmail)) {
			throw new SameEmailException();
		}
	}

	private boolean isDateOfBirthChanged(LocalDate newDate, LocalDate oldDate) {
		return newDate != null && !newDate.equals(oldDate);
	}

	public UserResponse getUserResponseByEmail(String email) {
		User user = getUserByEmail(email);
		return userMapper.toUserResponse(user);
	}

	private void revokeVerificationIfNeeded(User user) {
		if (user.getVerificationStatus() == VerificationStatus.VERIFIED) {
			user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);
			user.setVerifiedAt(null);
			log.info("Birth date verification revoked for user {}", user.getId());
		}
	}
}