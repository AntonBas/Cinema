package ua.lviv.bas.cinema.service.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.exception.domain.user.LastAdminException;
import ua.lviv.bas.cinema.exception.domain.user.SelfBlockException;
import ua.lviv.bas.cinema.exception.domain.user.SelfRoleChangeException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.user.UserMapper;
import ua.lviv.bas.cinema.repository.user.UserRepository;
import ua.lviv.bas.cinema.repository.user.projection.AdminUserProjection;
import ua.lviv.bas.cinema.service.shared.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "adminUsers")
public class AdminUserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final AuditService auditService;

	@Caching(evict = { @CacheEvict(key = "'list-' + #userId"), @CacheEvict(allEntries = true) })
	@Transactional
	public AdminUserListResponse updateUserRole(Long userId, UserRole newRole) {
		User user = findById(userId);
		UserRole oldRole = user.getUserRole();
		validateSelfOperation(user);

		if (user.getUserRole() == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_ADMIN) {
			validateLastAdmin();
		}

		user.setUserRole(newRole);
		User savedUser = userRepository.save(user);
		log.info("User role updated to {} for user {}", newRole, userId);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("role", oldRole);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("role", newRole);
		newDetails.put("userId", userId);
		newDetails.put("userEmail", user.getEmail());

		auditService.logChange("User", userId, user.getEmail(), AuditAction.ROLE_CHANGED, oldDetails, newDetails);

		return userMapper.toAdminUserListResponse(savedUser);
	}

	@Caching(evict = { @CacheEvict(key = "'list-' + #userId"), @CacheEvict(allEntries = true) })
	@Transactional
	public AdminUserListResponse updateUserStatus(Long userId, boolean enabled) {
		User user = findById(userId);
		boolean oldStatus = user.isEnabled();

		if (isCurrentUser(user) && !enabled) {
			throw new SelfBlockException();
		}

		user.setEnabled(enabled);
		User savedUser = userRepository.save(user);
		log.info("User status updated: enabled = {} for user {}", enabled, userId);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("enabled", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("enabled", enabled);
		newDetails.put("userId", userId);
		newDetails.put("userEmail", user.getEmail());

		auditService.logChange("User", userId, user.getEmail(), AuditAction.STATUS_CHANGED, oldDetails, newDetails);

		return userMapper.toAdminUserListResponse(savedUser);
	}

	@Caching(evict = { @CacheEvict(key = "'list-' + #userId"), @CacheEvict(allEntries = true) })
	@Transactional
	public AdminUserListResponse updateBirthDateVerification(Long userId, VerificationBirthDateRequest request) {
		User user = findById(userId);
		VerificationStatus oldStatus = user.getVerificationStatus();
		VerificationStatus newStatus = request.verificationStatus();

		user.setVerificationStatus(newStatus);
		user.setVerifiedAt(newStatus == VerificationStatus.VERIFIED ? LocalDateTime.now() : null);

		User savedUser = userRepository.save(user);
		log.info("Birth date verification updated: {} for user {}", newStatus, userId);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("verificationStatus", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("verificationStatus", newStatus);
		newDetails.put("userId", userId);
		newDetails.put("userEmail", user.getEmail());

		auditService.logChange("User", userId, user.getEmail(), AuditAction.VERIFICATION_CHANGED, oldDetails,
				newDetails);

		return userMapper.toAdminUserListResponse(savedUser);
	}

	@Cacheable(key = "'list-' + #filter.hashCode() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<AdminUserListResponse> getUsersForAdmin(UserFilterRequest filter, Pageable pageable) {
		log.info("Fetching users for admin with filter: {}, pageable: {}", filter, pageable);

		Page<AdminUserProjection> projections = userRepository.findAdminProjectionsWithFilters(filter.search(),
				filter.role() != null ? filter.role().name() : null,
				filter.verificationStatus() != null ? filter.verificationStatus().name() : null, filter.enabled(),
				pageable);

		return projections.map(userMapper::toAdminUserListResponse);
	}

	public long getAdminCount() {
		return userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
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