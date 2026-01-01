package ua.lviv.bas.cinema.service.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserMapper userMapper;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private Authentication authentication;

	@InjectMocks
	private AdminUserService adminUserService;

	private User testUser;
	private User adminUser;
	private User anotherAdmin;
	private final Long USER_ID = 1L;
	private final Long ADMIN_ID = 2L;
	private final Long ANOTHER_ADMIN_ID = 3L;
	private final String USER_EMAIL = "anton.bas@example.com";
	private final String ADMIN_EMAIL = "admin@example.com";
	private final String ANOTHER_ADMIN_EMAIL = "another.admin@example.com";
	private final String USER_PHONE = "+3801234567";

	@BeforeEach
	void setUp() {
		testUser = User.builder().id(USER_ID).email(USER_EMAIL).firstName("Anton").lastName("Bas")
				.phoneNumber(USER_PHONE).dateOfBirth(java.time.LocalDate.of(1990, 1, 1)).password("encodedPassword")
				.verificationStatus(VerificationStatus.NOT_VERIFIED).city("Kyiv").userRole(UserRole.ROLE_USER)
				.enabled(true).build();

		adminUser = User.builder().id(ADMIN_ID).email(ADMIN_EMAIL).firstName("Admin").lastName("User")
				.phoneNumber("+3809876543").dateOfBirth(java.time.LocalDate.of(1985, 5, 15)).password("adminPassword")
				.verificationStatus(VerificationStatus.VERIFIED).city("Lviv").userRole(UserRole.ROLE_ADMIN)
				.enabled(true).build();

		anotherAdmin = User.builder().id(ANOTHER_ADMIN_ID).email(ANOTHER_ADMIN_EMAIL).firstName("Another")
				.lastName("Admin").phoneNumber("+3805555555").dateOfBirth(java.time.LocalDate.of(1980, 3, 10))
				.password("anotherPassword").verificationStatus(VerificationStatus.VERIFIED).city("Odessa")
				.userRole(UserRole.ROLE_ADMIN).enabled(true).build();

		SecurityContextHolder.setContext(securityContext);
	}

	@Test
	void updateUserRole_Success() {
		testUser.setUserRole(UserRole.ROLE_ADMIN);

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(2L);
		when(userRepository.save(testUser)).thenReturn(testUser);

		adminUserService.updateUserRole(USER_ID, UserRole.ROLE_USER);

		assertEquals(UserRole.ROLE_USER, testUser.getUserRole());
		verify(userRepository).save(testUser);
		verify(userRepository).countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserRole_SelfRoleChange() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

		assertThrows(SelfRoleChangeException.class,
				() -> adminUserService.updateUserRole(USER_ID, UserRole.ROLE_ADMIN));
		verify(userRepository, never()).save(any());
		verify(userRepository, never()).countByUserRoleAndEnabledTrue(any());
	}

	@Test
	void updateUserRole_LastAdmin_Demotion() {
		User lastAdmin = User.builder().id(3L).email("last.admin@example.com").userRole(UserRole.ROLE_ADMIN)
				.enabled(true).build();

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(3L)).thenReturn(Optional.of(lastAdmin));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(1L);

		assertThrows(LastAdminException.class, () -> adminUserService.updateUserRole(3L, UserRole.ROLE_USER));
		verify(userRepository, never()).save(any());
		verify(userRepository).countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserRole_AdminToUser_WhenMultipleAdmins() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(ANOTHER_ADMIN_ID)).thenReturn(Optional.of(anotherAdmin));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);
		when(userRepository.save(anotherAdmin)).thenReturn(anotherAdmin);

		adminUserService.updateUserRole(ANOTHER_ADMIN_ID, UserRole.ROLE_USER);

		assertEquals(UserRole.ROLE_USER, anotherAdmin.getUserRole());
		verify(userRepository).save(anotherAdmin);
		verify(userRepository).countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserStatus_Success() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(testUser)).thenReturn(testUser);

		adminUserService.updateUserStatus(USER_ID, false);

		assertFalse(testUser.isEnabled());
		verify(userRepository).save(testUser);
	}

	@Test
	void updateUserStatus_SelfBlock() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

		assertThrows(SelfBlockException.class, () -> adminUserService.updateUserStatus(USER_ID, false));
		verify(userRepository, never()).save(any());
	}

	@Test
	void updateBirthDateVerification_Success_Verified() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(testUser)).thenReturn(testUser);
		when(userMapper.toDto(testUser)).thenReturn(new UserResponse());

		UserResponse result = adminUserService.updateBirthDateVerification(USER_ID, request);

		assertNotNull(result);
		assertEquals(VerificationStatus.VERIFIED, testUser.getVerificationStatus());
		assertNotNull(testUser.getVerifiedAt());
		verify(userRepository).save(testUser);
	}

	@Test
	void updateBirthDateVerification_Success_NotVerified() {
		testUser.setVerificationStatus(VerificationStatus.VERIFIED);
		testUser.setVerifiedAt(LocalDateTime.now());

		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(testUser)).thenReturn(testUser);
		when(userMapper.toDto(testUser)).thenReturn(new UserResponse());

		UserResponse result = adminUserService.updateBirthDateVerification(USER_ID, request);

		assertNotNull(result);
		assertEquals(VerificationStatus.NOT_VERIFIED, testUser.getVerificationStatus());
		assertNull(testUser.getVerifiedAt());
		verify(userRepository).save(testUser);
	}

	@Test
	void findAllForAdmin_Success() {
		String search = "Anton";
		UserRole role = UserRole.ROLE_USER;
		Boolean enabled = true;
		Pageable pageable = Pageable.unpaged();
		Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));

		when(userRepository.findFilteredUsers(search, role, enabled, pageable)).thenReturn(userPage);
		when(userMapper.toAdminListDto(testUser)).thenReturn(new AdminUserListResponse());

		Page<AdminUserListResponse> result = adminUserService.findAllForAdmin(search, role, enabled, pageable);

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		verify(userRepository).findFilteredUsers(search, role, enabled, pageable);
		verify(userMapper).toAdminListDto(testUser);
	}

	@Test
	void findAllActiveAdmins_Success() {
		List<User> admins = Arrays.asList(adminUser, anotherAdmin);
		when(userRepository.findByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(admins);

		List<User> result = adminUserService.findAllActiveAdmins();

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(UserRole.ROLE_ADMIN, result.get(0).getUserRole());
		verify(userRepository).findByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Test
	void findAllActiveUsers_Success() {
		List<User> users = Arrays.asList(testUser, adminUser, anotherAdmin);
		when(userRepository.findByEnabledTrue()).thenReturn(users);

		List<User> result = adminUserService.findAllActiveUsers();

		assertNotNull(result);
		assertEquals(3, result.size());
		verify(userRepository).findByEnabledTrue();
	}

	@Test
	void findById_Success() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

		User result = adminUserService.findById(USER_ID);

		assertEquals(testUser, result);
		verify(userRepository).findById(USER_ID);
	}

	@Test
	void findById_UserNotFound() {
		Long nonExistentId = 999L;
		when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> adminUserService.findById(nonExistentId));
		verify(userRepository).findById(nonExistentId);
	}

	@Test
	void updateBirthDateVerification_VerifiedAtNotOverwritten() {
		LocalDateTime existingVerifiedAt = LocalDateTime.now().minusDays(1);
		testUser.setVerificationStatus(VerificationStatus.VERIFIED);
		testUser.setVerifiedAt(existingVerifiedAt);

		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(testUser)).thenReturn(testUser);
		when(userMapper.toDto(testUser)).thenReturn(new UserResponse());

		UserResponse result = adminUserService.updateBirthDateVerification(USER_ID, request);

		assertNotNull(result);
		assertEquals(existingVerifiedAt, testUser.getVerifiedAt());
		verify(userRepository).save(testUser);
	}

	@Test
	void updateUserStatus_EnableUser() {
		testUser.setEnabled(false);

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(testUser)).thenReturn(testUser);

		adminUserService.updateUserStatus(USER_ID, true);

		assertTrue(testUser.isEnabled());
		verify(userRepository).save(testUser);
	}

	@Test
	void updateUserStatus_Success_WhenCurrentUserIsDifferent() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn("different.admin@example.com");
		when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
		when(userRepository.save(adminUser)).thenReturn(adminUser);

		adminUserService.updateUserStatus(ADMIN_ID, false);

		assertFalse(adminUser.isEnabled());
		verify(userRepository).save(adminUser);
	}

	@Test
	void countAdmins_Success() {
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);

		long result = adminUserService.countAdmins();

		assertEquals(3L, result);
		verify(userRepository).countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Test
	void findBirthdayUsersToday_Success() {
		int day = LocalDateTime.now().getDayOfMonth();
		int month = LocalDateTime.now().getMonthValue();
		List<User> birthdayUsers = Arrays.asList(testUser);

		when(userRepository.findVerifiedUsersWithBirthday(VerificationStatus.VERIFIED, day, month))
				.thenReturn(birthdayUsers);

		List<User> result = adminUserService.findBirthdayUsersToday();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals(testUser, result.get(0));
		verify(userRepository).findVerifiedUsersWithBirthday(VerificationStatus.VERIFIED, day, month);
	}

	@Test
	void updateUserRole_WhenUserIsNotAdmin() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
		when(userRepository.save(testUser)).thenReturn(testUser);

		adminUserService.updateUserRole(USER_ID, UserRole.ROLE_ADMIN);

		assertEquals(UserRole.ROLE_ADMIN, testUser.getUserRole());
		verify(userRepository).save(testUser);
		verify(userRepository, never()).countByUserRoleAndEnabledTrue(any());
	}
}