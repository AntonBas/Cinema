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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.service.admin.AdminUserService;

@ExtendWith(MockitoExtension.class)
public class AdminUserControllerTest {

	@Mock
	private AdminUserService adminUserService;

	@InjectMocks
	private AdminUserController userController;

	@Test
	void getUsers_ShouldReturnPageOfUsers() {
		UserFilterRequest filter = new UserFilterRequest();
		Pageable pageable = PageRequest.of(0, 20);

		AdminUserListResponse user1 = AdminUserListResponse.builder().id(1L).email("user1@example.com")
				.firstName("John").lastName("Doe").userRole(UserRole.ROLE_USER).enabled(true).build();

		AdminUserListResponse user2 = AdminUserListResponse.builder().id(2L).email("user2@example.com")
				.firstName("Jane").lastName("Smith").userRole(UserRole.ROLE_ADMIN).enabled(true).build();

		Page<AdminUserListResponse> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

		when(adminUserService.getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class))).thenReturn(page);

		ResponseEntity<PageResponse<AdminUserListResponse>> response = userController.getUsers(filter, pageable);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());

		PageResponse<AdminUserListResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		verify(adminUserService).getUsersForAdmin(any(UserFilterRequest.class), any(Pageable.class));
	}

	@Test
	void updateUserRole_ShouldUpdateRoleSuccessfully() {
		UserRoleUpdateRequest request = new UserRoleUpdateRequest();
		request.setUserRole(UserRole.ROLE_ADMIN);

		ResponseEntity<Void> response = userController.updateUserRole(1L, request);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());
		verify(adminUserService).updateUserRole(1L, UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserStatus_ShouldUpdateStatusSuccessfully() {
		UserStatusUpdateRequest request = new UserStatusUpdateRequest();
		request.setEnabled(false);

		ResponseEntity<Void> response = userController.updateUserStatus(1L, request);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());
		verify(adminUserService).updateUserStatus(1L, false);
	}

	@Test
	void updateBirthDateVerification_ShouldUpdateVerificationSuccessfully() {
		VerificationBirthDateRequest request = new VerificationBirthDateRequest();
		request.setVerificationStatus(VerificationStatus.VERIFIED);

		UserResponse userResponse = UserResponse.builder().id(1L).email("user@example.com").firstName("John")
				.lastName("Doe").phoneNumber("+123456789").dateOfBirth(LocalDate.of(1990, 1, 1))
				.userRole(UserRole.ROLE_USER).enabled(true).build();

		when(adminUserService.updateBirthDateVerification(eq(1L), any(VerificationBirthDateRequest.class)))
				.thenReturn(userResponse);

		ResponseEntity<UserResponse> response = userController.updateBirthDateVerification(1L, request);

		assertNotNull(response);
		assertEquals(200, response.getStatusCode().value());

		UserResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(1L, responseBody.getId());
		verify(adminUserService).updateBirthDateVerification(eq(1L), any(VerificationBirthDateRequest.class));
	}
}