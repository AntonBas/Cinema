package ua.lviv.bas.cinema.service.user;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.exception.domain.user.LastAdminException;
import ua.lviv.bas.cinema.exception.domain.user.SelfBlockException;
import ua.lviv.bas.cinema.exception.domain.user.SelfRoleChangeException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.user.UserMapper;
import ua.lviv.bas.cinema.repository.user.UserRepository;
import ua.lviv.bas.cinema.repository.user.projection.AdminUserProjection;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final AuditService auditService;

	@CacheEvict(value = "users", allEntries = true)
	@Transactional
	public AdminUserListResponse updateRole(Long userId, UserRole newRole) {
		var user = findById(userId);
		var oldRole = user.getUserRole();
		validateSelfOperation(user);

		if (user.getUserRole() == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_ADMIN) {
			validateLastAdmin();
		}

		user.setUserRole(newRole);
		var updated = userRepository.save(user);
		log.info("User role updated to {} for user {}", newRole, userId);
		auditRoleChange(userId, user.getEmail(), oldRole, newRole);

		return userMapper.toAdminUserListResponse(updated);
	}

	@CacheEvict(value = "users", allEntries = true)
	@Transactional
	public AdminUserListResponse updateStatus(Long userId, boolean enabled) {
		var user = findById(userId);
		var oldStatus = user.isEnabled();

		if (isCurrentUser(user) && !enabled) {
			throw new SelfBlockException();
		}

		user.setEnabled(enabled);
		var updated = userRepository.save(user);
		log.info("User status updated: enabled = {} for user {}", enabled, userId);
		auditStatusChange(userId, user.getEmail(), oldStatus, enabled);

		return userMapper.toAdminUserListResponse(updated);
	}

	@CacheEvict(value = "users", allEntries = true)
	@Transactional
	public AdminUserListResponse updateVerification(Long userId, VerificationStatus status) {
		var user = findById(userId);
		var oldStatus = user.getVerificationStatus();

		user.setVerificationStatus(status);
		user.setVerifiedAt(status == VerificationStatus.VERIFIED ? LocalDateTime.now() : null);

		var updated = userRepository.save(user);
		log.info("Verification status updated: {} for user {}", status, userId);
		auditVerificationChange(userId, user.getEmail(), oldStatus, status);

		return userMapper.toAdminUserListResponse(updated);
	}

	@Cacheable(value = "users", key = "'list-' + #query + '-' + #role + '-' + #verificationStatus + '-' + #enabled + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<AdminUserListResponse> getUsers(String query, UserRole role, VerificationStatus verificationStatus,
			Boolean enabled, Pageable pageable) {
		log.info("Getting users: query={}, role={}, verificationStatus={}, enabled={}, page={}, size={}", query, role,
				verificationStatus, enabled, pageable.getPageNumber(), pageable.getPageSize());

		String roleStr = role != null ? role.name() : null;
		String verificationStatusStr = verificationStatus != null ? verificationStatus.name() : null;

		Page<AdminUserProjection> page = userRepository.findProjectionsByFilters(query, roleStr, verificationStatusStr,
				enabled, pageable);

		return page.map(userMapper::toAdminUserListResponse);
	}

	public long getAdminCount() {
		return userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	private User findById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
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

	private void auditRoleChange(Long userId, String email, UserRole oldRole, UserRole newRole) {
		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("role", oldRole);
		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("role", newRole);
		auditService.logChange("User", userId, email, AuditAction.ROLE_CHANGED, oldDetails, newDetails);
	}

	private void auditStatusChange(Long userId, String email, boolean oldStatus, boolean newStatus) {
		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("enabled", oldStatus);
		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("enabled", newStatus);
		auditService.logChange("User", userId, email, AuditAction.STATUS_CHANGED, oldDetails, newDetails);
	}

	private void auditVerificationChange(Long userId, String email, VerificationStatus oldStatus,
			VerificationStatus newStatus) {
		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("verificationStatus", oldStatus);
		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("verificationStatus", newStatus);
		auditService.logChange("User", userId, email, AuditAction.VERIFICATION_CHANGED, oldDetails, newDetails);
	}
}