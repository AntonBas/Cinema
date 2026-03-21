package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.domain.projection.AdminUserProjection;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.exception.domain.user.LastAdminException;
import ua.lviv.bas.cinema.exception.domain.user.SelfBlockException;
import ua.lviv.bas.cinema.exception.domain.user.SelfRoleChangeException;
import ua.lviv.bas.cinema.exception.domain.user.UserNotFoundException;
import ua.lviv.bas.cinema.mapper.UserMapper;
import ua.lviv.bas.cinema.repository.UserRepository;

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
	@InjectMocks
	private AdminUserService service;

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
	}

	@Test
	void updateUserRole_PromoteToAdmin_Success() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateUserRole(USER_ID, UserRole.ROLE_ADMIN);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_ADMIN);
		verify(userRepository).save(user);
	}

	@Test
	void updateUserRole_DemoteAdmin_Success() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(OTHER_ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);
		when(userRepository.save(adminUser)).thenReturn(adminUser);

		AdminUserListResponse adminResponse = new AdminUserListResponse(ADMIN_ID, ADMIN_EMAIL, "Admin", "User",
				UserRole.ROLE_ADMIN, true, VerificationStatus.VERIFIED, null, 0L, null);
		when(userMapper.toAdminUserListResponse(adminUser)).thenReturn(adminResponse);

		AdminUserListResponse result = service.updateUserRole(ADMIN_ID, UserRole.ROLE_USER);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(ADMIN_ID);
		assertThat(adminUser.getUserRole()).isEqualTo(UserRole.ROLE_USER);
		verify(userRepository).save(adminUser);
	}

	@Test
	void updateUserRole_SelfChange_ThrowsException() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.updateUserRole(USER_ID, UserRole.ROLE_ADMIN))
				.isInstanceOf(SelfRoleChangeException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserRole_LastAdmin_ThrowsException() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(OTHER_ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(ADMIN_ID)).thenReturn(Optional.of(adminUser));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> service.updateUserRole(ADMIN_ID, UserRole.ROLE_USER))
				.isInstanceOf(LastAdminException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserRole_UserNotFound_ThrowsException() {
		when(userRepository.findWithBonusCardById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateUserRole(999L, UserRole.ROLE_ADMIN))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void updateUserStatus_BlockUser_Success() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateUserStatus(USER_ID, false);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.isEnabled()).isFalse();
		verify(userRepository).save(user);
	}

	@Test
	void updateUserStatus_UnblockUser_Success() {
		user.setEnabled(false);

		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateUserStatus(USER_ID, true);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.isEnabled()).isTrue();
		verify(userRepository).save(user);
	}

	@Test
	void updateUserStatus_SelfBlock_ThrowsException() {
		when(securityContext.getAuthentication()).thenReturn(authentication);
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.updateUserStatus(USER_ID, false)).isInstanceOf(SelfBlockException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserStatus_UserNotFound_ThrowsException() {
		when(userRepository.findWithBonusCardById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateUserStatus(999L, true)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void updateBirthDateVerification_ToVerified_Success() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest(VerificationStatus.VERIFIED);

		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateBirthDateVerification(USER_ID, request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		assertThat(user.getVerifiedAt()).isNotNull();
		verify(userRepository).save(user);
	}

	@Test
	void updateBirthDateVerification_ToNotVerified_Success() {
		LocalDateTime verifiedTime = LocalDateTime.now().minusDays(1);
		user.setVerificationStatus(VerificationStatus.VERIFIED);
		user.setVerifiedAt(verifiedTime);

		VerificationBirthDateRequest request = new VerificationBirthDateRequest(VerificationStatus.NOT_VERIFIED);

		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateBirthDateVerification(USER_ID, request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(USER_ID);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(user.getVerifiedAt()).isNull();
		verify(userRepository).save(user);
	}

	@Test
	void updateBirthDateVerification_UserNotFound_ThrowsException() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest(VerificationStatus.VERIFIED);

		when(userRepository.findWithBonusCardById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateBirthDateVerification(999L, request))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void getUsersForAdmin_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		UserFilterRequest filter = new UserFilterRequest("test", UserRole.ROLE_USER, VerificationStatus.NOT_VERIFIED,
				true);

		AdminUserProjection projection = createAdminUserProjection();
		Page<AdminUserProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);

		when(userRepository.findAdminProjectionsWithFilters(eq("test"), eq("ROLE_USER"), eq("NOT_VERIFIED"), eq(true),
				eq(pageable))).thenReturn(projectionPage);
		when(userMapper.toAdminUserListResponse(projection)).thenReturn(response);

		Page<AdminUserListResponse> result = service.getUsersForAdmin(filter, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(response);
	}

	@Test
	void getUsersForAdmin_WithNullFilters_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		UserFilterRequest filter = new UserFilterRequest(null, null, null, null);

		AdminUserProjection projection = createAdminUserProjection();
		Page<AdminUserProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);

		when(userRepository.findAdminProjectionsWithFilters(eq(null), eq(null), eq(null), eq(null), eq(pageable)))
				.thenReturn(projectionPage);
		when(userMapper.toAdminUserListResponse(projection)).thenReturn(response);

		Page<AdminUserListResponse> result = service.getUsersForAdmin(filter, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getUsersForAdmin_EmptyResult_ReturnsEmptyPage() {
		Pageable pageable = PageRequest.of(0, 10);
		UserFilterRequest filter = new UserFilterRequest(null, null, null, null);

		Page<AdminUserProjection> emptyPage = new PageImpl<>(List.of(), pageable, 0);

		when(userRepository.findAdminProjectionsWithFilters(eq(null), eq(null), eq(null), eq(null), eq(pageable)))
				.thenReturn(emptyPage);

		Page<AdminUserListResponse> result = service.getUsersForAdmin(filter, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEmpty();
		assertThat(result.getTotalElements()).isZero();
	}

	@Test
	void getAdminCount_ReturnsCount() {
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);

		long result = service.getAdminCount();

		assertThat(result).isEqualTo(3L);
	}

	@Test
	void getAdminCount_WhenNoAdmins_ReturnsZero() {
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(0L);

		long result = service.getAdminCount();

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