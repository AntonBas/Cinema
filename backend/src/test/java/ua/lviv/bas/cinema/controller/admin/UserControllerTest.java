package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;

import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.service.admin.AdminUserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private AdminUserService adminUserService;

	@InjectMocks
	private UserController userController;

	@Test
	void getAllUsers_ShouldReturnPageOfUsers() {
		AdminUserListResponse user1 = AdminUserListResponse.builder().id(1L).email("user1@example.com")
				.firstName("John").lastName("Doe").userRole(UserRole.ROLE_USER).enabled(true).build();

		AdminUserListResponse user2 = AdminUserListResponse.builder().id(2L).email("user2@example.com")
				.firstName("Jane").lastName("Smith").userRole(UserRole.ROLE_ADMIN).enabled(true).build();

		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 20), 2);

		when(adminUserService.findAllForAdmin(any(), any(), any(), any())).thenReturn(page);

		Page<AdminUserListResponse> result = userController.getAllUsers("test", UserRole.ROLE_USER, true,
				PageRequest.of(0, 20));

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		verify(adminUserService).findAllForAdmin("test", UserRole.ROLE_USER, true, PageRequest.of(0, 20));
	}

	@Test
	void getAllUsers_WithNullFilters_ShouldReturnPageOfUsers() {
		AdminUserListResponse user = AdminUserListResponse.builder().id(1L).email("user@example.com").firstName("John")
				.lastName("Doe").userRole(UserRole.ROLE_USER).enabled(true).build();

		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);

		when(adminUserService.findAllForAdmin(any(), any(), any(), any())).thenReturn(page);

		Page<AdminUserListResponse> result = userController.getAllUsers(null, null, null, PageRequest.of(0, 20));

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(adminUserService).findAllForAdmin(null, null, null, PageRequest.of(0, 20));
	}

	@Test
	void updateUserRole_ShouldUpdateRoleSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest();
		request.setUserRole(UserRole.ROLE_ADMIN);

		var response = userController.updateUserRole(1L, request);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserStatus_ShouldUpdateStatusSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest();
		request.setEnabled(false);

		var response = userController.updateUserStatus(1L, request);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());
		verify(adminUserService).updateUserStatus(1L, false);
	}

	@Test
	void updateBirthDateVerification_ShouldUpdateVerificationSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		UserResponse userResponse = UserResponse.builder().id(1L).email("user@example.com").firstName("John")
				.lastName("Doe").phoneNumber("+123456789").dateOfBirth(LocalDate.of(1990, 1, 1))
				.userRole(UserRole.ROLE_USER).enabled(true).build();

		when(adminUserService.updateBirthDateVerification(eq(1L), any())).thenReturn(userResponse);

		var response = userController.updateBirthDateVerification(1L, request);

		assertNotNull(response);
		assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

		UserResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());
		verify(adminUserService).updateBirthDateVerification(1L, request);
	}
}