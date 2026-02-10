package ua.lviv.bas.cinema.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
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
	private Authentication authentication;

	@InjectMocks
	private AdminUserService service;

	@BeforeEach
	void setUp() {
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	@Test
	void updateUserRolePromoteToAdmin() {
		User user = User.builder().id(1L).email("user@test.com").userRole(UserRole.ROLE_USER).build();

		when(authentication.getName()).thenReturn("admin@test.com");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		service.updateUserRole(1L, UserRole.ROLE_ADMIN);

		assertThat(user.getUserRole()).isEqualTo(UserRole.ROLE_ADMIN);
		verify(userRepository).save(user);
	}

	@Test
	void updateUserRoleDemoteAdminWithMultipleAdmins() {
		User admin = User.builder().id(1L).email("admin@test.com").userRole(UserRole.ROLE_ADMIN).build();

		when(authentication.getName()).thenReturn("other@test.com");
		when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);
		when(userRepository.save(admin)).thenReturn(admin);

		service.updateUserRole(1L, UserRole.ROLE_USER);

		assertThat(admin.getUserRole()).isEqualTo(UserRole.ROLE_USER);
		verify(userRepository).countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserRoleSelfChange() {
		User user = User.builder().id(1L).email("user@test.com").userRole(UserRole.ROLE_USER).build();

		when(authentication.getName()).thenReturn("user@test.com");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.updateUserRole(1L, UserRole.ROLE_ADMIN))
				.isInstanceOf(SelfRoleChangeException.class);
	}

	@Test
	void updateUserRoleLastAdmin() {
		User admin = User.builder().id(1L).email("admin@test.com").userRole(UserRole.ROLE_ADMIN).build();

		when(authentication.getName()).thenReturn("other@test.com");
		when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> service.updateUserRole(1L, UserRole.ROLE_USER)).isInstanceOf(LastAdminException.class);
	}

	@Test
	void updateUserStatus() {
		User user = User.builder().id(1L).email("user@test.com").enabled(true).build();

		when(authentication.getName()).thenReturn("admin@test.com");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		service.updateUserStatus(1L, false);

		assertThat(user.isEnabled()).isFalse();
		verify(userRepository).save(user);
	}

	@Test
	void updateUserStatusSelfBlock() {
		User user = User.builder().id(1L).email("user@test.com").enabled(true).build();

		when(authentication.getName()).thenReturn("user@test.com");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		assertThatThrownBy(() -> service.updateUserStatus(1L, false)).isInstanceOf(SelfBlockException.class);
	}

	@Test
	void updateBirthDateVerification() {
		User user = User.builder().id(1L).verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		UserResponse response = new UserResponse();

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserResponse(user)).thenReturn(response);

		UserResponse result = service.updateBirthDateVerification(1L, request);

		assertThat(result).isNotNull();
		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		assertThat(user.getVerifiedAt()).isNotNull();
	}

	@Test
	void getUserById() {
		User user = User.builder().id(1L).build();

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		User result = service.getUserById(1L);

		assertThat(result).isEqualTo(user);
	}

	@Test
	void getUserByIdNotFound() {
		when(userRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getUserById(1L)).isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void getAdminCount() {
		when(userRepository.countByUserRoleAndEnabledTrue(UserRole.ROLE_ADMIN)).thenReturn(3L);

		long result = service.getAdminCount();

		assertThat(result).isEqualTo(3L);
	}

	@Test
	void getTodayBirthdayUsers() {
		User user = User.builder().id(1L).build();
		LocalDateTime today = LocalDateTime.now();

		when(userRepository.findVerifiedUsersWithBirthday(VerificationStatus.VERIFIED, today.getDayOfMonth(),
				today.getMonthValue())).thenReturn(Collections.singletonList(user));

		var result = service.getTodayBirthdayUsers();

		assertThat(result).hasSize(1);
	}

	@Test
	void updateUserStatusEnable() {
		User user = User.builder().id(1L).email("user@test.com").enabled(false).build();

		when(authentication.getName()).thenReturn("admin@test.com");
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);

		service.updateUserStatus(1L, true);

		assertThat(user.isEnabled()).isTrue();
	}

	@Test
	void updateBirthDateVerificationToNotVerified() {
		User user = User.builder().id(1L).verificationStatus(VerificationStatus.VERIFIED)
				.verifiedAt(LocalDateTime.now()).build();

		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		UserResponse response = new UserResponse();

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserResponse(user)).thenReturn(response);

		service.updateBirthDateVerification(1L, request);

		assertThat(user.getVerificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);
		assertThat(user.getVerifiedAt()).isNull();
	}

	@Test
	void updateBirthDateVerificationSameStatus() {
		LocalDateTime originalTime = LocalDateTime.now().minusDays(1);
		User user = User.builder().id(1L).verificationStatus(VerificationStatus.VERIFIED).verifiedAt(originalTime)
				.build();

		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		UserResponse response = new UserResponse();

		when(userRepository.findById(1L)).thenReturn(Optional.of(user));
		when(userRepository.save(user)).thenReturn(user);
		when(userMapper.toUserResponse(user)).thenReturn(response);

		UserResponse result = service.updateBirthDateVerification(1L, request);

		assertThat(result).isNotNull();
	}
}