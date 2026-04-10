package ua.lviv.bas.cinema.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

@ExtendWith(MockitoExtension.class)
public class AdminUserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private UserMapper userMapper;
	@Mock
	private Authentication authentication;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private AuditService auditService;
	@InjectMocks
	private AdminUserService adminUserService;

	private final Long USER_ID = 1L;
	private final Long ADMIN_ID = 2L;
	private final String USER_EMAIL = "user@test.com";
	private final String ADMIN_EMAIL = "admin@test.com";
	private final String OTHER_ADMIN_EMAIL = "other.admin@test.com";

	private User user;
	private User adminUser;
	private AdminUserListResponse response;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.setContext(securityContext);

		user = User.builder().id(USER_ID).email(USER_EMAIL).userRole(UserRole.ROLE_USER).enabled(true)
				.verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		adminUser = User.builder().id(ADMIN_ID).email(ADMIN_EMAIL).userRole(UserRole.ROLE_ADMIN).enabled(true)
				.verificationStatus(VerificationStatus.VERIFIED).build();

		response = new AdminUserListResponse(USER_ID, USER_EMAIL, "John", "Doe", UserRole.ROLE_USER, true,
				VerificationStatus.NOT_VERIFIED, null, 0L, null);

		lenient().doNothing().when(auditService).logChange(any(), any(), any(), any(), any(), any());
	}

	@Test
	void updateRolePromoteToAdminShouldSucceed() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = adminUserService.updateRole(USER_ID, UserRole.ROLE_ADMIN);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_ADMIN);
		verify(userRepository).save(user);
	}

	@Test
	void updateRoleDemoteAdminShouldSucceed() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(OTHER_ADMIN_EMAIL);
		when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);
		when(userRepository.save(adminUser)).thenReturn(adminUser);

		AdminUserListResponse adminResponse = new AdminUserListResponse(ADMIN_ID, ADMIN_EMAIL, "Admin", "User",
				UserRole.ROLE_ADMIN, true, VerificationStatus.VERIFIED, null, 0L, null);
		when(userMapper.toAdminUserListResponse(adminUser)).thenReturn(adminResponse);

		AdminUserListResponse result = adminUserService.updateRole(ADMIN_ID, UserRole.ROLE_USER);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(ADMIN_ID);
		assertThat(adminUser.getUserRole()).isEqualTo(UserRole.ROLE_USER);
		verify(userRepository).save(adminUser);
	}

	@Test
	void updateRoleSelfChangeShouldThrowException() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> adminUserService.updateRole(USER_ID, UserRole.ROLE_ADMIN))
				.isInstanceOf(SelfRoleChangeException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateRoleLastAdminShouldThrowException() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(OTHER_ADMIN_EMAIL);
		when(userRepository.findById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> adminUserService.updateRole(ADMIN_ID, UserRole.ROLE_USER))
				.isInstanceOf(LastAdminException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateRoleUserNotFoundShouldThrowException() {
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminUserService.updateRole(999L, UserRole.ROLE_ADMIN))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void updateStatusBlockUserShouldSucceed() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = adminUserService.updateStatus(USER_ID, false);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.isEnabled()).isFalse();
		verify(userRepository).save(user);
	}

	@Test
	void updateStatusUnblockUserShouldSucceed() {
		user.setEnabled(false);

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = adminUserService.updateStatus(USER_ID, true);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.isEnabled()).isTrue();
		verify(userRepository).save(user);
	}

	@Test
	void updateStatusSelfBlockShouldThrowException() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> adminUserService.updateStatus(USER_ID, false)).isInstanceOf(SelfBlockException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateStatusUserNotFoundShouldThrowException() {
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminUserService.updateStatus(999L, true)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void updateVerificationToVerifiedShouldSucceed() {
		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = adminUserService.updateVerification(USER_ID, VerificationStatus.VERIFIED);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		assertThat(user.getVerifiedAt()).isNotNull();
		verify(userRepository).save(user);
	}

	@Test
	void updateVerificationToNotVerifiedShouldSucceed() {
		LocalDateTime verifiedTime = LocalDateTime.now().minusDays(1);
		user.setVerificationStatus(VerificationStatus.VERIFIED);
		user.setVerifiedAt(verifiedTime);

		when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = adminUserService.updateVerification(USER_ID, VerificationStatus.NOT_VERIFIED);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(user.getVerifiedAt()).isNull();
		verify(userRepository).save(user);
	}

	@Test
	void updateVerificationUserNotFoundShouldThrowException() {
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> adminUserService.updateVerification(999L, VerificationStatus.VERIFIED))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void getUsersShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		String search = "test";
		UserRole role = UserRole.ROLE_USER;
		VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;
		Boolean enabled = true;

		AdminUserProjection projection = createAdminUserProjection();
		Page<AdminUserProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);

		when(userRepository.findProjectionsByFilters(eq(search), eq("ROLE_USER"), eq("NOT_VERIFIED"), eq(true),
				eq(pageable))).thenReturn(projectionPage);
		when(userMapper.toAdminUserListResponse(projection)).thenReturn(response);

		Page<AdminUserListResponse> result = adminUserService.getUsers(search, role, verificationStatus, enabled,
				pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(response);
	}

	@Test
	void getUsersWithNullFiltersShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);

		AdminUserProjection projection = createAdminUserProjection();
		Page<AdminUserProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);

		when(userRepository.findProjectionsByFilters(eq(null), eq(null), eq(null), eq(null), eq(pageable)))
				.thenReturn(projectionPage);
		when(userMapper.toAdminUserListResponse(projection)).thenReturn(response);

		Page<AdminUserListResponse> result = adminUserService.getUsers(null, null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getUsersEmptyResultShouldReturnEmptyPage() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<AdminUserProjection> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(userRepository.findProjectionsByFilters(eq(null), eq(null), eq(null), eq(null), eq(pageable)))
				.thenReturn(emptyPage);

		Page<AdminUserListResponse> result = adminUserService.getUsers(null, null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
	}

	@Test
	void getAdminCountShouldReturnCount() {
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);

		long result = adminUserService.getAdminCount();

		assertThat(result).isEqualTo(3L);
	}

	@Test
	void getAdminCountWhenNoAdminsShouldReturnZero() {
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(0L);

		long result = adminUserService.getAdminCount();

		assertThat(result).isZero();
	}

	private AdminUserProjection createAdminUserProjection() {
		return new AdminUserProjection() {
			@Override
			public Long getId() {
				return USER_ID;
			}

			@Override
			public String getEmail() {
				return USER_EMAIL;
			}

			@Override
			public String getFirstName() {
				return "John";
			}

			@Override
			public String getLastName() {
				return "Doe";
			}

			@Override
			public UserRole getUserRole() {
				return UserRole.ROLE_USER;
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public VerificationStatus getVerificationStatus() {
				return VerificationStatus.NOT_VERIFIED;
			}

			@Override
			public LocalDateTime getVerifiedAt() {
				return null;
			}

			@Override
			public Long getTicketsCount() {
				return 5L;
			}

			@Override
			public LocalDateTime getLastActivity() {
				return LocalDateTime.now().minusDays(1);
			}
		};
	}
}