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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
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
	private AdminUserController userController;

	private AdminUserListResponse createAdminUserListResponse(Long id, String email, String firstName, String lastName,
			UserRole role, boolean enabled) {
		return new AdminUserListResponse(id, email, firstName, lastName, role, enabled, VerificationStatus.NOT_VERIFIED,
				null, 0L, null);
	}

	@Test
	void getUsers_ShouldReturnPageOfUsers() {
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

		when(adminUserService.getUsersForAdmin(search, role, verificationStatus, enabled, pageable)).thenReturn(page);

		ResponseEntity<PageResponse<AdminUserListResponse>> response = userController.getUsers(search, role,
				verificationStatus, enabled, pageable);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		PageResponse<AdminUserListResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.content()).hasSize(2);
		assertThat(body.number()).isZero();
		assertThat(body.size()).isEqualTo(20);
		assertThat(body.totalElements()).isEqualTo(2);
		assertThat(body.totalPages()).isEqualTo(1);

		verify(adminUserService).getUsersForAdmin(search, role, verificationStatus, enabled, pageable);
	}

	@Test
	void getUsers_WithFilter_ShouldReturnFilteredUsers() {
		String search = "john";
		UserRole role = UserRole.ROLE_USER;
		VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;
		Boolean enabled = true;
		Pageable pageable = PageRequest.of(0, 20);

		AdminUserListResponse user = createAdminUserListResponse(1L, "john@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);
		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user), pageable, 1);

		when(adminUserService.getUsersForAdmin(search, role, verificationStatus, enabled, pageable)).thenReturn(page);

		ResponseEntity<PageResponse<AdminUserListResponse>> response = userController.getUsers(search, role,
				verificationStatus, enabled, pageable);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		PageResponse<AdminUserListResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.content()).hasSize(1);
		assertThat(body.content().get(0).email()).isEqualTo("john@example.com");

		verify(adminUserService).getUsersForAdmin(search, role, verificationStatus, enabled, pageable);
	}

	@Test
	void getUsers_WhenNoResults_ShouldReturnEmptyPage() {
		String search = null;
		UserRole role = null;
		VerificationStatus verificationStatus = null;
		Boolean enabled = null;
		Pageable pageable = PageRequest.of(0, 20);
		Page<AdminUserListResponse> emptyPage = Page.empty(pageable);

		when(adminUserService.getUsersForAdmin(search, role, verificationStatus, enabled, pageable))
				.thenReturn(emptyPage);

		ResponseEntity<PageResponse<AdminUserListResponse>> response = userController.getUsers(search, role,
				verificationStatus, enabled, pageable);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		PageResponse<AdminUserListResponse> body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.content()).isEmpty();
		assertThat(body.totalElements()).isZero();

		verify(adminUserService).getUsersForAdmin(search, role, verificationStatus, enabled, pageable);
	}

	@Test
	void updateUserRole_ShouldUpdateRoleSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ROLE_ADMIN);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_ADMIN, true);

		when(adminUserService.updateUserRole(1L, UserRole.ROLE_ADMIN)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserRole(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		AdminUserListResponse body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.userRole()).isEqualTo(UserRole.ROLE_ADMIN);
		assertThat(body.id()).isEqualTo(1L);

		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserRole_WithContentManagerRole_ShouldUpdateSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ROLE_CONTENT_MANAGER);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_CONTENT_MANAGER, true);

		when(adminUserService.updateUserRole(1L, UserRole.ROLE_CONTENT_MANAGER)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserRole(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		AdminUserListResponse body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.userRole()).isEqualTo(UserRole.ROLE_CONTENT_MANAGER);

		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_CONTENT_MANAGER);
	}

	@Test
	void updateUserRole_WithUserRole_ShouldUpdateSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ROLE_USER);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);

		when(adminUserService.updateUserRole(1L, UserRole.ROLE_USER)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserRole(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		AdminUserListResponse body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.userRole()).isEqualTo(UserRole.ROLE_USER);

		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_USER);
	}

	@Test
	void updateUserStatus_ShouldUpdateStatusSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, false);

		when(adminUserService.updateUserStatus(1L, false)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserStatus(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		AdminUserListResponse body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.enabled()).isFalse();
		assertThat(body.id()).isEqualTo(1L);

		verify(adminUserService).updateUserStatus(1L, false);
	}

	@Test
	void updateUserStatus_WithEnabledTrue_ShouldUpdateSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest(true);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);

		when(adminUserService.updateUserStatus(1L, true)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserStatus(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		AdminUserListResponse body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.enabled()).isTrue();

		verify(adminUserService).updateUserStatus(1L, true);
	}

	@Test
	void updateBirthDateVerification_ShouldUpdateToVerifiedSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest(VerificationStatus.VERIFIED);

		AdminUserListResponse updatedUser = new AdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true, VerificationStatus.VERIFIED, null, 0L, null);

		when(adminUserService.updateBirthDateVerification(1L, VerificationStatus.VERIFIED)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateBirthDateVerification(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		AdminUserListResponse body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.id()).isEqualTo(1L);
		assertThat(body.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);

		verify(adminUserService).updateBirthDateVerification(1L, VerificationStatus.VERIFIED);
	}

	@Test
	void updateBirthDateVerification_ShouldUpdateToNotVerifiedSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest(VerificationStatus.NOT_VERIFIED);

		AdminUserListResponse updatedUser = new AdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true, VerificationStatus.NOT_VERIFIED, null, 0L, null);

		when(adminUserService.updateBirthDateVerification(1L, VerificationStatus.NOT_VERIFIED)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateBirthDateVerification(1L, request);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		AdminUserListResponse body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body.verificationStatus()).isEqualTo(VerificationStatus.NOT_VERIFIED);

		verify(adminUserService).updateBirthDateVerification(1L, VerificationStatus.NOT_VERIFIED);
	}
}