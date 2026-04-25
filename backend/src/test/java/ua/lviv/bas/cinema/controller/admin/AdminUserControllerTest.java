package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.service.user.AdminUserService;

@ExtendWith(MockitoExtension.class)
public class AdminUserControllerTest {

	@Mock
	private AdminUserService adminUserService;

	@InjectMocks
	private AdminUserController adminUserController;

	private AdminUserListResponse createAdminUserListResponse(Long id, String email, String firstName, String lastName,
			UserRole role, boolean enabled) {
		return new AdminUserListResponse(id, email, firstName, lastName, role, enabled, VerificationStatus.NOT_VERIFIED,
				null, 0L, null);
	}

	@Test
	void getUsersShouldReturnPageOfUsers() {
		String search = null;
		UserRole role = null;
		VerificationStatus verificationStatus = null;
		Boolean enabled = null;
		Pageable pageable = PageRequest.of(0, 20);

		AdminUserListResponse user1 = createAdminUserListResponse(1L, "user1@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);
		AdminUserListResponse user2 = createAdminUserListResponse(2L, "user2@example.com", "Jane", "Smith",
				UserRole.ROLE_ADMIN, true);

		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

		when(adminUserService.getUsers(search, role, verificationStatus, enabled, pageable)).thenReturn(page);

		PageResponse<AdminUserListResponse> result = adminUserController.getUsers(search, role, verificationStatus,
				enabled, pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(2);
		assertThat(result.number()).isZero();
		assertThat(result.size()).isEqualTo(20);
		assertThat(result.totalElements()).isEqualTo(2);
		assertThat(result.totalPages()).isEqualTo(1);

		verify(adminUserService).getUsers(search, role, verificationStatus, enabled, pageable);
	}

	@Test
	void getUsersWithFilterShouldReturnFilteredUsers() {
		String search = "john";
		UserRole role = UserRole.ROLE_USER;
		VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;
		Boolean enabled = true;
		Pageable pageable = PageRequest.of(0, 20);

		AdminUserListResponse user = createAdminUserListResponse(1L, "john@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);
		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user), pageable, 1);

		when(adminUserService.getUsers(search, role, verificationStatus, enabled, pageable)).thenReturn(page);

		PageResponse<AdminUserListResponse> result = adminUserController.getUsers(search, role, verificationStatus,
				enabled, pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).email()).isEqualTo("john@example.com");

		verify(adminUserService).getUsers(search, role, verificationStatus, enabled, pageable);
	}

	@Test
	void getUsersWhenNoResultsShouldReturnEmptyPage() {
		String search = null;
		UserRole role = null;
		VerificationStatus verificationStatus = null;
		Boolean enabled = null;
		Pageable pageable = PageRequest.of(0, 20);
		Page<AdminUserListResponse> emptyPage = Page.empty(pageable);

		when(adminUserService.getUsers(search, role, verificationStatus, enabled, pageable)).thenReturn(emptyPage);

		PageResponse<AdminUserListResponse> result = adminUserController.getUsers(search, role, verificationStatus,
				enabled, pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEmpty();
		assertThat(result.totalElements()).isZero();

		verify(adminUserService).getUsers(search, role, verificationStatus, enabled, pageable);
	}

	@Test
	void updateRoleShouldUpdateRoleSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ROLE_ADMIN);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_ADMIN, true);

		when(adminUserService.updateRole(1L, UserRole.ROLE_ADMIN)).thenReturn(updatedUser);

		AdminUserListResponse result = adminUserController.updateRole(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.userRole()).isEqualTo(UserRole.ROLE_ADMIN);
		assertThat(result.id()).isEqualTo(1L);

		verify(adminUserService).updateRole(1L, UserRole.ROLE_ADMIN);
	}

	@Test
	void updateRoleWithContentManagerRoleShouldUpdateSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ROLE_CONTENT_MANAGER);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_CONTENT_MANAGER, true);

		when(adminUserService.updateRole(1L, UserRole.ROLE_CONTENT_MANAGER)).thenReturn(updatedUser);

		AdminUserListResponse result = adminUserController.updateRole(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.userRole()).isEqualTo(UserRole.ROLE_CONTENT_MANAGER);

		verify(adminUserService).updateRole(1L, UserRole.ROLE_CONTENT_MANAGER);
	}

	@Test
	void updateRoleWithUserRoleShouldUpdateSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ROLE_USER);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);

		when(adminUserService.updateRole(1L, UserRole.ROLE_USER)).thenReturn(updatedUser);

		AdminUserListResponse result = adminUserController.updateRole(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.userRole()).isEqualTo(UserRole.ROLE_USER);

		verify(adminUserService).updateRole(1L, UserRole.ROLE_USER);
	}

	@Test
	void updateStatusShouldUpdateStatusSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, false);

		when(adminUserService.updateStatus(1L, false)).thenReturn(updatedUser);

		AdminUserListResponse result = adminUserController.updateStatus(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.enabled()).isFalse();
		assertThat(result.id()).isEqualTo(1L);

		verify(adminUserService).updateStatus(1L, false);
	}

	@Test
	void updateStatusWithEnabledTrueShouldUpdateSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest(true);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);

		when(adminUserService.updateStatus(1L, true)).thenReturn(updatedUser);

		AdminUserListResponse result = adminUserController.updateStatus(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.enabled()).isTrue();

		verify(adminUserService).updateStatus(1L, true);
	}

	@Test
	void updateVerificationShouldUpdateToVerifiedSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest(VerificationStatus.VERIFIED);

		AdminUserListResponse updatedUser = new AdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true, VerificationStatus.VERIFIED, null, 0L, null);

		when(adminUserService.updateVerification(1L, VerificationStatus.VERIFIED)).thenReturn(updatedUser);

		AdminUserListResponse result = adminUserController.updateVerification(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(1L);
		assertThat(result.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

		verify(adminUserService).updateVerification(1L, VerificationStatus.VERIFIED);
	}

	@Test
	void updateVerificationShouldUpdateToNotVerifiedSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest(VerificationStatus.NOT_VERIFIED);

		AdminUserListResponse updatedUser = new AdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true, VerificationStatus.NOT_VERIFIED, null, 0L, null);

		when(adminUserService.updateVerification(1L, VerificationStatus.NOT_VERIFIED)).thenReturn(updatedUser);

		AdminUserListResponse result = adminUserController.updateVerification(1L, request);

		assertThat(result).isNotNull();
		assertThat(result.verificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);

		verify(adminUserService).updateVerification(1L, VerificationStatus.NOT_VERIFIED);
	}
}