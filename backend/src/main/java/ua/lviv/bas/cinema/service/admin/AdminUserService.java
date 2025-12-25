package ua.lviv.bas.cinema.service.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
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
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
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
public class AdminUserService {

	private final UserRepository userRepository;
	private final UserQueryService userQueryService;
	private final UserMapper userMapper;

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

	@Transactional
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
	public List<User> findAllActiveAdmins() {
		return userQueryService.findAllActiveByRole(UserRole.ROLE_ADMIN);
	}

	@Transactional(readOnly = true)
	public List<User> findAllActiveUsers() {
		return userQueryService.findAllActiveUsers();
	}

	@Transactional(readOnly = true)
	public User findById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}
}