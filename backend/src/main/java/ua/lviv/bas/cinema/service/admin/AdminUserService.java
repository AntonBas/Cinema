package ua.lviv.bas.cinema.service.admin;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.domain.user.LastAdminException;
import ua.lviv.bas.cinema.exception.domain.user.SelfBlockException;
import ua.lviv.bas.cinema.exception.domain.user.SelfRoleChangeException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

	private final UserRepository userRepository;
	private final TicketRepository ticketRepository;
	private final UserMapper userMapper;

	@Transactional
	@CacheEvict(value = "users", allEntries = true)
	public void updateUserRole(Long userId, UserRole newRole) {
		User user = findById(userId);
		UserRole oldRole = user.getUserRole();

		String currentUserEmail = getCurrentUserEmail();
		validateSelfOperation(user.getEmail(), currentUserEmail, () -> new SelfRoleChangeException(userId));

		if (oldRole == UserRole.ROLE_ADMIN && newRole != UserRole.ROLE_ADMIN) {
			validateLastAdmin();
		}

		user.setUserRole(newRole);
		userRepository.save(user);
		log.info("User role updated for user {}: {} -> {}", userId, oldRole, newRole);
	}

	@Transactional
	@CacheEvict(value = "users", allEntries = true)
	public void updateUserStatus(Long userId, boolean enabled) {
		User user = findById(userId);
		boolean oldStatus = user.isEnabled();

		String currentUserEmail = getCurrentUserEmail();
		if (user.getEmail().equals(currentUserEmail) && !enabled) {
			throw new SelfBlockException();
		}

		user.setEnabled(enabled);
		userRepository.save(user);
		log.info("User status updated for user {}: enabled = {} -> {}", userId, oldStatus, enabled);
	}

	@Transactional
	@CacheEvict(value = { "users", "verifications" }, allEntries = true)
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

		return userMapper.toUserResponse(saved);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#search + '-' + #role + '-' + #enabled + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<AdminUserListResponse> findAllForAdmin(String search, UserRole role, Boolean enabled,
			Pageable pageable) {
		String roleString = role != null ? role.name() : null;
		Page<User> usersPage = userRepository.findFilteredUsers(search, roleString, enabled, pageable);

		List<Long> userIds = usersPage.getContent().stream().map(User::getId).collect(Collectors.toList());

		Map<Long, Long> ticketsCountMap = new HashMap<>();
		if (!userIds.isEmpty()) {
			List<Object[]> counts = ticketRepository.countTicketsByUserIds(userIds);
			for (Object[] count : counts) {
				Long userId = (Long) count[0];
				Long ticketsCount = (Long) count[1];
				ticketsCountMap.put(userId, ticketsCount);
			}
		}

		return usersPage.map(user -> {
			AdminUserListResponse response = userMapper.toAdminUserListResponse(user);

			Long ticketsCount = ticketsCountMap.get(user.getId());
			response.setTicketsCount(ticketsCount != null ? ticketsCount.intValue() : 0);

			return response;
		});
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "admins", key = "'active'")
	public List<User> findAllActiveAdmins() {
		return userRepository.findByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "'active'")
	public List<User> findAllActiveUsers() {
		return userRepository.findByEnabledTrue();
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "users", key = "#id")
	public User findById(Long id) {
		return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
	}

	@Transactional(readOnly = true)
	public long countAdmins() {
		return userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Transactional(readOnly = true)
	public List<User> findBirthdayUsersToday() {
		LocalDateTime today = LocalDateTime.now();
		int day = today.getDayOfMonth();
		int month = today.getMonthValue();

		return userRepository.findVerifiedUsersWithBirthday(VerificationStatus.VERIFIED, day, month);
	}

	private String getCurrentUserEmail() {
		return SecurityContextHolder.getContext().getAuthentication().getName();
	}

	private void validateSelfOperation(String userEmail, String currentUserEmail,
			RuntimeExceptionSupplier exceptionSupplier) {
		if (userEmail.equals(currentUserEmail)) {
			throw exceptionSupplier.get();
		}
	}

	private void validateLastAdmin() {
		long adminCount = countAdmins();
		if (adminCount <= 1) {
			throw new LastAdminException();
		}
	}

	@FunctionalInterface
	private interface RuntimeExceptionSupplier {
		RuntimeException get();
	}
}