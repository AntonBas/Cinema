package ua.lviv.bas.cinema.service;

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
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.request.UserUpdateRequest;
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
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
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
		userMapper.updateUserFromDto(request, user);
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
	public void updateUserRole(Long userId, UserRole newRole) {
		User user = findById(userId);

		if (user.getUserRole() == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_ADMIN) {
			long adminCount = userRepository.countByUserRole(UserRole.ROLE_ADMIN);
			if (adminCount <= 1) {
				throw new LastAdminException();
			}
		}

		user.setUserRole(newRole);
		userRepository.save(user);
		log.info("User role updated for user {}: {} -> {}", userId, user.getUserRole(), newRole);
	}

	@Transactional
	public void updateUserStatus(Long userId, boolean enabled) {
		User user = findById(userId);

		String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		if (user.getEmail().equals(currentUserEmail) && !enabled) {
			throw new SelfBlockException();
		}

		user.setEnabled(enabled);
		userRepository.save(user);
		log.info("User status updated for user {}: enabled = {}", userId, enabled);
	}

	@Transactional(readOnly = true)
	public Page<AdminUserListResponse> findAllForAdmin(String search, UserRole role, Boolean enabled,
			Pageable pageable) {
		return userRepository.findByFilters(search, role, enabled, pageable).map(userMapper::toAdminListDto);
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