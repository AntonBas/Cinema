package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.domain.projection.AdminUserProjection;
import ua.lviv.bas.cinema.domain.specification.UserSpecification;
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
class AdminUserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private UserMapper userMapper;
	@Mock
	private UserSpecification userSpecification;
	@Mock
	private Authentication authentication;

	@InjectMocks
	private AdminUserService service;

	private final Long USER_ID = 1L;
	private final String USER_EMAIL = "user@test.com";
	private final String ADMIN_EMAIL = "admin@test.com";
	private User user;
	private AdminUserListResponse response;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.getContext().setAuthentication(authentication);

		user = User.builder().id(USER_ID).email(USER_EMAIL).userRole(UserRole.ROLE_USER).enabled(true)
				.verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		response = new AdminUserListResponse();
		response.setId(USER_ID);
	}

	@Test
	void updateUserRole_PromoteToAdmin_Success() {
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateUserRole(USER_ID, UserRole.ROLE_ADMIN);

		assertThat(result).isEqualTo(response);
		assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_ADMIN);
		verify(userRepository).save(user);
	}

	@Test
	void updateUserRole_DemoteAdmin_Success() {
		user.setUserRole(UserRole.ROLE_ADMIN);

		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateUserRole(USER_ID, UserRole.ROLE_USER);

		assertThat(result).isEqualTo(response);
		assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_USER);
	}

	@Test
	void updateUserRole_SelfChange_ThrowsException() {
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.updateUserRole(USER_ID, UserRole.ROLE_ADMIN))
				.isInstanceOf(SelfRoleChangeException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserRole_LastAdmin_ThrowsException() {
		user.setUserRole(UserRole.ROLE_ADMIN);

		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> service.updateUserRole(USER_ID, UserRole.ROLE_USER))
				.isInstanceOf(LastAdminException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateUserStatus_BlockUser_Success() {
		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateUserStatus(USER_ID, false);

		assertThat(result).isEqualTo(response);
		assertThat(user.isEnabled()).isFalse();
	}

	@Test
	void updateUserStatus_UnblockUser_Success() {
		user.setEnabled(false);

		when(authentication.getName()).thenReturn(ADMIN_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateUserStatus(USER_ID, true);

		assertThat(result).isEqualTo(response);
		assertThat(user.isEnabled()).isTrue();
	}

	@Test
	void updateUserStatus_SelfBlock_ThrowsException() {
		when(authentication.getName()).thenReturn(USER_EMAIL);
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.updateUserStatus(USER_ID, false)).isInstanceOf(SelfBlockException.class);

		verify(userRepository, never()).save(any());
	}

	@Test
	void updateBirthDateVerification_ToVerified_Success() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateBirthDateVerification(USER_ID, request);

		assertThat(result).isEqualTo(response);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		assertThat(user.getVerifiedAt()).isNotNull();
	}

	@Test
	void updateBirthDateVerification_ToNotVerified_Success() {
		LocalDateTime verifiedTime = LocalDateTime.now();
		user.setVerificationStatus(VerificationStatus.VERIFIED);
		user.setVerifiedAt(verifiedTime);

		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toAdminUserListResponse(user)).thenReturn(response);

		AdminUserListResponse result = service.updateBirthDateVerification(USER_ID, request);

		assertThat(result).isEqualTo(response);
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(user.getVerifiedAt()).isNull();
	}

	@SuppressWarnings("unchecked")
	@Test
	void getUsersForAdmin_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		UserFilterRequest filter = new UserFilterRequest();

		Specification<User> spec = (root, query, cb) -> cb.conjunction();
		when(userSpecification.buildForAdmin(filter)).thenReturn(spec);

		Page<User> userPage = new PageImpl<>(List.of(user));
		when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

		AdminUserProjection projection = new AdminUserProjection() {
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
				return LocalDateTime.now();
			}
		};

		when(userRepository.findAdminProjectionsByUserIds(List.of(USER_ID))).thenReturn(List.of(projection));
		when(userMapper.toAdminUserListResponse(projection)).thenReturn(response);

		Page<AdminUserListResponse> result = service.getUsersForAdmin(filter, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(response);
	}

	@Test
	void getAdminCount_ReturnsCount() {
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);

		long result = service.getAdminCount();

		assertThat(result).isEqualTo(3L);
	}

	@Test
	void findById_NotFound_ThrowsException() {
		when(userRepository.findWithBonusCardById(USER_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateUserRole(USER_ID, UserRole.ROLE_ADMIN))
				.isInstanceOf(UserNotFoundException.class);
	}
}