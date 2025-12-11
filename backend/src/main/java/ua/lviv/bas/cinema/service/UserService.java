package ua.lviv.bas.cinema.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.auth.EmailAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.auth.InvalidCurrentPasswordException;
import ua.lviv.bas.cinema.exception.domain.auth.PasswordMismatchException;
import ua.lviv.bas.cinema.exception.domain.auth.SameEmailException;
import ua.lviv.bas.cinema.exception.domain.auth.SamePasswordException;
import ua.lviv.bas.cinema.exception.domain.user.LastAdminException;
import ua.lviv.bas.cinema.exception.domain.user.SelfBlockException;
import ua.lviv.bas.cinema.exception.domain.user.SelfRoleChangeException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;
import ua.lviv.bas.cinema.service.query.UserQueryService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserQueryService userQueryService;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;
	private final EmailTokenService emailTokenService;
	private final EmailTokenGeneratorService emailTokenGeneratorService;

	@Transactional
	public UserResponse registerUser(UserRegistrationRequest request) {
		log.info("Registering user with email: {}", request.getEmail());

		if (existsByEmail(request.getEmail())) {
			throw new EmailAlreadyExistsException(request.getEmail());
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
		LocalDate oldDateOfBirth = user.getDateOfBirth();

		userMapper.updateUserFromDto(request, user);

		if (request.getDateOfBirth() != null && !request.getDateOfBirth().equals(oldDateOfBirth)) {
			if (user.getVerificationStatus() == VerificationStatus.VERIFIED) {
				user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);
				user.setVerifiedAt(null);
				log.info("Birth date verification revoked for user {} (date changed from {} to {})", userId,
						oldDateOfBirth, request.getDateOfBirth());
			}
		}

		return userMapper.toProfileResponse(userRepository.save(user));
	}

	@Transactional
	public void requestEmailChange(Long userId, String newEmail) {
		User user = findById(userId);

		if (user.getEmail().equals(newEmail)) {
			throw new SameEmailException();
		}

		if (existsByEmail(newEmail)) {
			throw new EmailAlreadyExistsException(newEmail);
		}

		emailTokenGeneratorService.generateEmailChangeToken(user.getEmail(), newEmail);
		log.info("Email change token generated for user {}", userId);
	}

	@Transactional
	public String confirmRegistration(String token) {
		return emailTokenService.confirmEmail(token);
	}

	@Transactional
	public UserProfileResponse confirmEmailChange(String token) {
		User updatedUser = emailTokenService.confirmEmailChange(token);
		return userMapper.toProfileResponse(updatedUser);
	}

	@Transactional
	public void updateUserPassword(Long userId, String currentPassword, String newPassword, String passwordConfirm) {
		User user = findById(userId);

		if (!newPassword.equals(passwordConfirm)) {
			throw new PasswordMismatchException();
		}

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new InvalidCurrentPasswordException();
		}

		if (passwordEncoder.matches(newPassword, user.getPassword())) {
			throw new SamePasswordException();
		}

		if (newPassword.length() < 6) {
			throw new IllegalArgumentException("Password must be at least 6 characters long");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		log.info("Password updated for user {}", userId);
	}

	@Transactional
	@CacheEvict(value = "users", key = "#userId")
	public void updateUserRole(Long userId, UserRole newRole) {
		User user = findById(userId);
		UserRole oldRole = user.getUserRole();

		String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		if (user.getEmail().equals(currentUserEmail)) {
			throw new SelfRoleChangeException(userId);
		}

		if (oldRole == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_ADMIN) {
			long adminCount = userRepository.countByUserRole(UserRole.ROLE_ADMIN);
			if (adminCount <= 1) {
				throw new LastAdminException();
			}
		}

		user.setUserRole(newRole);
		userRepository.save(user);
		log.info("User role updated for user {}: {} -> {}", userId, oldRole, newRole);
	}

	@Transactional
	@CacheEvict(value = "users", key = "#userId")
	public void updateUserStatus(Long userId, boolean enabled) {
		User user = findById(userId);
		boolean oldStatus = user.isEnabled();

		String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		if (user.getEmail().equals(currentUserEmail) && !enabled) {
			throw new SelfBlockException();
		}

		user.setEnabled(enabled);
		userRepository.save(user);
		log.info("User status updated for user {}: enabled = {} -> {}", userId, oldStatus, enabled);
	}

	public UserResponse updateBirthDateVerification(Long userId, VerificationBirthDateRequest request) {
		User user = findById(userId);

		VerificationStatus oldStatus = user.getVerificationStatus();
		VerificationStatus newStatus = request.getVerificationStatus();

		user.setVerificationStatus(newStatus);

		if (newStatus == VerificationStatus.VERIFIED) {
			if (user.getVerifiedAt() == null) {
				user.setVerifiedAt(LocalDateTime.now());
			}
		} else {
			user.setVerifiedAt(null);
		}
		User saved = userRepository.save(user);
		log.info("Birth date verification updated for user {}: {} -> {}", userId, oldStatus, newStatus);

		return userMapper.toDto(saved);

	}

	@Transactional(readOnly = true)
	public Page<AdminUserListResponse> findAllForAdmin(String search, UserRole role, Boolean enabled,
			Pageable pageable) {
		return userQueryService.findFilteredUsers(search, role, enabled, pageable).map(userMapper::toAdminListDto);
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
	@Cacheable(value = "users", key = "#id")
	public User findById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#email")
	public User findByEmail(String email) {
		return userQueryService.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
	}

	@Transactional(readOnly = true)
	public boolean existsByEmail(String email) {
		return userQueryService.existsByEmail(email);
	}

	@Transactional(readOnly = true)
	public boolean existsById(Long id) {
		return userRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public List<User> findAllActiveAdmins() {
		return userQueryService.findAllActiveByRole(UserRole.ROLE_ADMIN);
	}

	@Transactional(readOnly = true)
	public List<User> findAllActiveUsers() {
		return userQueryService.findAllActiveUsers();
	}
}