package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.service.admin.AdminUserService;

@ExtendWith(MockitoExtension.class)
public class AdminUserControllerTest {

	@Mock
	private AdminUserService adminUserService;

	@InjectMocks
	private AdminUserController userController;

	private AdminUserListResponse createAdminUserListResponse(Long id, String email, String firstName, String lastName,
			UserRole role, boolean enabled) {
		return AdminUserListResponse.builder().id(id).email(email).firstName(firstName).lastName(lastName)
				.userRole(role).enabled(enabled).verificationStatus(VerificationStatus.NOT_VERIFIED).build();
	}

	@Test
	void getUsers_ShouldReturnPageOfUsers() {
		UserFilterRequest filter = new UserFilterRequest();
		Pageable pageable = PageRequest.of(0, 20);

		AdminUserListResponse user1 = createAdminUserListResponse(1L, "user1@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);
		AdminUserListResponse user2 = createAdminUserListResponse(2L, "user2@example.com", "Jane", "Smith",
				UserRole.ROLE_ADMIN, true);

		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

		when(adminUserService.getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class))).thenReturn(page);

		ResponseEntity<PageResponse<AdminUserListResponse>> response = userController.getUsers(filter, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<AdminUserListResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		assertEquals(0, body.getNumber());
		assertEquals(20, body.getSize());
		assertEquals(2, body.getTotalElements());
		assertEquals(1, body.getTotalPages());

		verify(adminUserService).getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class));
	}

	@Test
	void getUsers_WithFilter_ShouldReturnFilteredUsers() {
		UserFilterRequest filter = new UserFilterRequest();
		filter.setSearch("john");
		filter.setRole(UserRole.ROLE_USER);
		filter.setVerificationStatus(VerificationStatus.NOT_VERIFIED);
		filter.setEnabled(true);

		Pageable pageable = PageRequest.of(0, 20);

		AdminUserListResponse user = createAdminUserListResponse(1L, "john@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);
		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user), pageable, 1);

		when(adminUserService.getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class))).thenReturn(page);

		ResponseEntity<PageResponse<AdminUserListResponse>> response = userController.getUsers(filter, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<AdminUserListResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals("john@example.com", body.getContent().get(0).getEmail());

		verify(adminUserService).getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class));
	}

	@Test
	void getUsers_WhenNoResults_ShouldReturnEmptyPage() {
		UserFilterRequest filter = new UserFilterRequest();
		Pageable pageable = PageRequest.of(0, 20);
		Page<AdminUserListResponse> emptyPage = Page.empty(pageable);

		when(adminUserService.getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class)))
				.thenReturn(emptyPage);

		ResponseEntity<PageResponse<AdminUserListResponse>> response = userController.getUsers(filter, pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<AdminUserListResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(0, body.getContent().size());
		assertEquals(0, body.getTotalElements());

		verify(adminUserService).getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class));
	}

	@Test
	void updateUserRole_ShouldUpdateRoleSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest();
		request.setUserRole(UserRole.ROLE_ADMIN);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_ADMIN, true);

		when(adminUserService.updateUserRole(1L, UserRole.ROLE_ADMIN)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserRole(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AdminUserListResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(UserRole.ROLE_ADMIN, body.getUserRole());
		assertEquals(1L, body.getId());

		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserRole_WithContentManagerRole_ShouldUpdateSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest();
		request.setUserRole(UserRole.ROLE_CONTENT_MANAGER);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_CONTENT_MANAGER, true);

		when(adminUserService.updateUserRole(1L, UserRole.ROLE_CONTENT_MANAGER)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserRole(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AdminUserListResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(UserRole.ROLE_CONTENT_MANAGER, body.getUserRole());

		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_CONTENT_MANAGER);
	}

	@Test
	void updateUserRole_WithUserRole_ShouldUpdateSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest();
		request.setUserRole(UserRole.ROLE_USER);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);

		when(adminUserService.updateUserRole(1L, UserRole.ROLE_USER)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserRole(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AdminUserListResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(UserRole.ROLE_USER, body.getUserRole());

		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_USER);
	}

	@Test
	void updateUserStatus_ShouldUpdateStatusSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest();
		request.setEnabled(false);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, false);

		when(adminUserService.updateUserStatus(1L, false)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserStatus(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AdminUserListResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(false, body.isEnabled());
		assertEquals(1L, body.getId());

		verify(adminUserService).updateUserStatus(1L, false);
	}

	@Test
	void updateUserStatus_WithEnabledTrue_ShouldUpdateSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest();
		request.setEnabled(true);

		AdminUserListResponse updatedUser = createAdminUserListResponse(1L, "user@example.com", "John", "Doe",
				UserRole.ROLE_USER, true);

		when(adminUserService.updateUserStatus(1L, true)).thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateUserStatus(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AdminUserListResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(true, body.isEnabled());

		verify(adminUserService).updateUserStatus(1L, true);
	}

	@Test
	void updateBirthDateVerification_ShouldUpdateToVerifiedSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		AdminUserListResponse updatedUser = AdminUserListResponse.builder().id(1L).email("user@example.com")
				.firstName("John").lastName("Doe").userRole(UserRole.ROLE_USER).enabled(true)
				.verificationStatus(VerificationStatus.VERIFIED).build();

		when(adminUserService.updateBirthDateVerification(eq(1L), any(VerificationBirthDateRequest.class)))
				.thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateBirthDateVerification(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AdminUserListResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(1L, body.getId());
		assertEquals(VerificationStatus.VERIFIED, body.getVerificationStatus());

		verify(adminUserService).updateBirthDateVerification(eq(1L), any(VerificationBirthDateRequest.class));
	}

	@Test
	void updateBirthDateVerification_ShouldUpdateToNotVerifiedSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		AdminUserListResponse updatedUser = AdminUserListResponse.builder().id(1L).email("user@example.com")
				.firstName("John").lastName("Doe").userRole(UserRole.ROLE_USER).enabled(true)
				.verificationStatus(VerificationStatus.NOT_VERIFIED).build();

		when(adminUserService.updateBirthDateVerification(eq(1L), any(VerificationBirthDateRequest.class)))
				.thenReturn(updatedUser);

		ResponseEntity<AdminUserListResponse> response = userController.updateBirthDateVerification(1L, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		AdminUserListResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(VerificationStatus.NOT_VERIFIED, body.getVerificationStatus());

		verify(adminUserService).updateBirthDateVerification(eq(1L), any(VerificationBirthDateRequest.class));
	}
}