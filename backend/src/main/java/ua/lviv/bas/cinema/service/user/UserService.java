package ua.lviv.bas.cinema.service.user;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
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
import ua.lviv.bas.cinema.mapper.user.UserMapper;
import ua.lviv.bas.cinema.repository.user.UserRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
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
	private final AuditService auditService;

	@CacheEvict(allEntries = true)
	@Transactional
	public UserResponse registerUser(UserRegistrationRequest request) {
		validatePasswordMatch(request.password(), request.passwordConfirm());
		validateEmailNotExists(request.email());

		User user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.password()));

		User saved = userRepository.save(user);
		log.info("User registered: {}", request.email());

		emailTokenGeneratorService.generateVerificationToken(saved.getEmail());

		Map<String, Object> details = new HashMap<>();
		details.put("email", saved.getEmail());
		details.put("firstName", saved.getFirstName());
		details.put("lastName", saved.getLastName());

		auditService.logChange("User", saved.getId(), saved.getEmail(), AuditAction.REGISTER, null, details);

		return userMapper.toUserResponse(saved);
	}

	@Caching(evict = { @CacheEvict(key = "#userId"), @CacheEvict(allEntries = true) })
	@Transactional
	public UserProfileResponse updateUser(Long userId, UserUpdateRequest request) {
		User oldUser = getUserWithBonusCardById(userId);
		User user = getUserWithBonusCardById(userId);
		userMapper.updateUserFromRequest(request, user);

		if (isDateOfBirthChanged(request.dateOfBirth(), user.getDateOfBirth())) {
			revokeVerificationIfNeeded(user);
		}

		User updated = userRepository.save(user);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("firstName", oldUser.getFirstName());
		oldDetails.put("lastName", oldUser.getLastName());
		oldDetails.put("city", oldUser.getCity());
		oldDetails.put("phoneNumber", oldUser.getPhoneNumber());
		oldDetails.put("dateOfBirth", oldUser.getDateOfBirth());

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("firstName", updated.getFirstName());
		newDetails.put("lastName", updated.getLastName());
		newDetails.put("city", updated.getCity());
		newDetails.put("phoneNumber", updated.getPhoneNumber());
		newDetails.put("dateOfBirth", updated.getDateOfBirth());

		auditService.logChange("User", userId, updated.getEmail(), AuditAction.UPDATED, oldDetails, newDetails);

		return userMapper.toUserProfileResponse(updated);
	}

	@Caching(evict = { @CacheEvict(key = "#userId"), @CacheEvict(key = "#newEmail"), @CacheEvict(allEntries = true) })
	@Transactional
	public void requestEmailChange(Long userId, String currentPassword, String newEmail) {
		User user = getUserById(userId);
		String oldEmail = user.getEmail();
		validateCurrentPassword(user, currentPassword);
		validateNewEmail(user.getEmail(), newEmail);
		validateEmailNotExists(newEmail);
		emailTokenGeneratorService.generateEmailChangeToken(user.getEmail(), newEmail);

		log.info("Email change requested for user {} to {}", userId, newEmail);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("email", oldEmail);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("email", newEmail);
		newDetails.put("userId", userId);

		auditService.logChange("User", userId, oldEmail, AuditAction.EMAIL_CHANGE_REQUESTED, oldDetails, newDetails);
	}

	@Caching(evict = { @CacheEvict(key = "#userId"), @CacheEvict(allEntries = true) })
	@Transactional
	public void updateUserPassword(Long userId, UserPasswordUpdateRequest request) {
		User user = getUserById(userId);
		validatePasswordMatch(request.newPassword(), request.passwordConfirm());
		validateCurrentPassword(user, request.currentPassword());
		validateNewPasswordDifferent(user, request.newPassword());
		validatePasswordLength(request.newPassword());

		String oldPasswordHash = user.getPassword();
		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
		log.info("Password updated for user {}", userId);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("passwordHash", oldPasswordHash);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("userId", userId);
		newDetails.put("userEmail", user.getEmail());

		auditService.logChange("User", userId, user.getEmail(), AuditAction.PASSWORD_CHANGED, oldDetails, newDetails);
	}

	@Cacheable(key = "#id")
	public User getUserById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Cacheable(key = "#email")
	public User getUserByEmail(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
	}

	public User getUserWithBonusCardById(Long id) {
		return userRepository.findWithBonusCardById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	public User getUserWithTicketsById(Long id) {
		return userRepository.findWithTicketsById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	public UserProfileResponse getUserProfile(Long id) {
		return userMapper.toUserProfileResponse(getUserWithBonusCardById(id));
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