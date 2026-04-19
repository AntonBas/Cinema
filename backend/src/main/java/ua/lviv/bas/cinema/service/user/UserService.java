package ua.lviv.bas.cinema.service.user;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final EmailTokenGeneratorService emailTokenGeneratorService;
	private final AuditService auditService;

	@CacheEvict(value = "users", allEntries = true)
	@Transactional
	public UserResponse register(UserRegistrationRequest request) {
		validatePasswordMatch(request.password(), request.passwordConfirm());
		validateEmailNotExists(request.email());

		var user = userMapper.toUser(request);
		user.setPassword(passwordEncoder.encode(request.password()));

		var saved = userRepository.save(user);
		log.info("User registered: {}", request.email());
		emailTokenGeneratorService.generateVerificationToken(saved);
		auditRegister(saved);

		return userMapper.toUserResponse(saved);
	}

	@CacheEvict(value = "users", allEntries = true)
	@Transactional
	public UserProfileResponse update(Long userId, UserUpdateRequest request) {
		var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
		var oldDetails = captureDetails(user);

		userMapper.updateUserFromRequest(request, user);

		if (isDateOfBirthChanged(request.dateOfBirth(), user.getDateOfBirth())) {
			revokeVerificationIfNeeded(user);
		}

		var updated = userRepository.save(user);
		log.info("User updated: {}", userId);
		auditUpdate(userId, updated.getEmail(), oldDetails, updated);

		return userMapper.toUserProfileResponse(updated);
	}

	@CacheEvict(value = "users", allEntries = true)
	@Transactional
	public void requestEmailChange(Long userId, String currentPassword, String newEmail) {
		var user = getUser(userId);
		var oldEmail = user.getEmail();

		validateCurrentPassword(user, currentPassword);
		validateNewEmail(user.getEmail(), newEmail);
		validateEmailNotExists(newEmail);

		emailTokenGeneratorService.generateEmailChangeToken(user, newEmail);
		log.info("Email change requested for user {} to {}", userId, newEmail);
		auditEmailChangeRequested(userId, oldEmail, newEmail);
	}

	@CacheEvict(value = "users", allEntries = true)
	@Transactional
	public void updatePassword(Long userId, UserPasswordUpdateRequest request) {
		var user = getUser(userId);

		validatePasswordMatch(request.newPassword(), request.passwordConfirm());
		validateCurrentPassword(user, request.currentPassword());
		validateNewPasswordDifferent(user, request.newPassword());

		user.setPassword(passwordEncoder.encode(request.newPassword()));
		userRepository.save(user);
		log.info("Password updated for user {}", userId);
		auditPasswordChanged(userId, user.getEmail());
	}

	@Cacheable(value = "users", key = "#id")
	public User getUser(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Cacheable(value = "users", key = "#email")
	public User getUser(String email) {
		return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
	}

	public boolean emailExists(String email) {
		return userRepository.existsByEmail(email);
	}

	public UserProfileResponse getProfile(Long id) {
		return userMapper.toUserProfileResponse(getUser(id));
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

	private void validateNewEmail(String currentEmail, String newEmail) {
		if (currentEmail.equalsIgnoreCase(newEmail)) {
			throw new SameEmailException();
		}
	}

	private boolean isDateOfBirthChanged(LocalDate newDate, LocalDate oldDate) {
		return newDate != null && !newDate.equals(oldDate);
	}

	private void revokeVerificationIfNeeded(User user) {
		if (user.getVerificationStatus() == VerificationStatus.VERIFIED) {
			user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);
			user.setVerifiedAt(null);
			log.info("Birth date verification revoked for user {}", user.getId());
		}
	}

	private Map<String, Object> captureDetails(User user) {
		Map<String, Object> details = new HashMap<>();
		details.put("firstName", user.getFirstName());
		details.put("lastName", user.getLastName());
		details.put("city", user.getCity());
		details.put("phoneNumber", user.getPhoneNumber());
		details.put("dateOfBirth", user.getDateOfBirth());
		return details;
	}

	private void auditRegister(User user) {
		Map<String, Object> details = new HashMap<>();
		details.put("email", user.getEmail());
		details.put("firstName", user.getFirstName());
		details.put("lastName", user.getLastName());
		auditService.logChange("User", user.getId(), user.getEmail(), AuditAction.REGISTER, null, details);
	}

	private void auditUpdate(Long userId, String email, Map<String, Object> oldDetails, User updated) {
		Map<String, Object> newDetails = captureDetails(updated);
		auditService.logChange("User", userId, email, AuditAction.UPDATED, oldDetails, newDetails);
	}

	private void auditEmailChangeRequested(Long userId, String oldEmail, String newEmail) {
		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("email", oldEmail);
		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("email", newEmail);
		auditService.logChange("User", userId, oldEmail, AuditAction.EMAIL_CHANGE_REQUESTED, oldDetails, newDetails);
	}

	private void auditPasswordChanged(Long userId, String email) {
		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("passwordChanged", true);
		auditService.logChange("User", userId, email, AuditAction.PASSWORD_CHANGED, oldDetails, null);
	}
}