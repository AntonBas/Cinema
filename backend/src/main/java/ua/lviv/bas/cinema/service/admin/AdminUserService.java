package ua.lviv.bas.cinema.service.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.domain.projection.AdminUserProjection;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.user.LastAdminException;
import ua.lviv.bas.cinema.exception.domain.user.SelfBlockException;
import ua.lviv.bas.cinema.exception.domain.user.SelfRoleChangeException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = { "users", "admins" })
public class AdminUserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;

	@CacheEvict(allEntries = true)
	@Transactional
	public void updateUserRole(Long userId, UserRole newRole) {
		User user = findById(userId);
		validateSelfOperation(user);

		if (user.getUserRole() == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_ADMIN) {
			validateLastAdmin();
		}

		user.setUserRole(newRole);
		userRepository.save(user);
		log.info("User role updated to {} for user {}", newRole, userId);
	}

	@CacheEvict(allEntries = true)
	@Transactional
	public void updateUserStatus(Long userId, boolean enabled) {
		User user = findById(userId);

		if (isCurrentUser(user) && !enabled) {
			throw new SelfBlockException();
		}

		user.setEnabled(enabled);
		userRepository.save(user);
		log.info("User status updated: enabled = {} for user {}", enabled, userId);
	}

	@CacheEvict(allEntries = true)
	@Transactional
	public UserResponse updateBirthDateVerification(Long userId, VerificationBirthDateRequest request) {
		User user = findById(userId);
		VerificationStatus newStatus = request.getVerificationStatus();

		user.setVerificationStatus(newStatus);
		user.setVerifiedAt(newStatus == VerificationStatus.VERIFIED ? LocalDateTime.now() : null);

		User saved = userRepository.save(user);
		log.info("Birth date verification updated: {} for user {}", newStatus, userId);

		return userMapper.toUserResponse(saved);
	}

	@Cacheable(key = "'list-' + #filter.hashCode() + '-' + #pageable")
	public Page<AdminUserListResponse> getUsersForAdmin(UserFilterRequest filter, Pageable pageable) {
		Page<AdminUserProjection> projections = userRepository.findAdminUsers(filter, pageable);
		return projections.map(userMapper::toAdminUserListResponse);
	}

	@Cacheable(key = "'active-admins'")
	public List<User> getActiveAdmins() {
		return userRepository.findActiveByRole(UserRole.ROLE_ADMIN);
	}

	@Cacheable(key = "'active'")
	public List<User> getActiveUsers() {
		return userRepository.findAll((root, query, cb) -> cb.isTrue(root.get("enabled")));
	}

	@Cacheable(key = "#id")
	public User getUserById(Long id) {
		return userRepository.findWithBonusCardById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	public long getAdminCount() {
		return userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	public List<User> getTodayBirthdayUsers() {
		LocalDateTime today = LocalDateTime.now();
		return userRepository.findVerifiedUsersWithBirthday(VerificationStatus.VERIFIED, today.getDayOfMonth(),
				today.getMonthValue());
	}

	private User findById(Long id) {
		return userRepository.findWithBonusCardById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	private boolean isCurrentUser(User user) {
		String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
		return user.getEmail().equals(currentEmail);
	}

	private void validateSelfOperation(User user) {
		if (isCurrentUser(user)) {
			throw new SelfRoleChangeException(user.getId());
		}
	}

	private void validateLastAdmin() {
		if (getAdminCount() <= 1) {
			throw new LastAdminException();
		}
	}
}