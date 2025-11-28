package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.service.UserService;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

	@Mock
	private UserService userService;

	@InjectMocks
	private AdminUserController adminUserController;

	@Test
	void getAllUsers_ShouldReturnPageOfUsers() {
		Pageable pageable = PageRequest.of(0, 10);
		AdminUserListResponse user1 = AdminUserListResponse.builder().id(1L).email("user1@example.com").build();
		AdminUserListResponse user2 = AdminUserListResponse.builder().id(2L).email("user2@example.com").build();
		Page<AdminUserListResponse> userPage = new PageImpl<>(List.of(user1, user2));

		when(userService.findAllForAdmin(isNull(), isNull(), isNull(), eq(pageable))).thenReturn(userPage);

		Page<AdminUserListResponse> result = adminUserController.getAllUsers(null, null, null, pageable);

		assertEquals(2, result.getContent().size());
		assertEquals("user1@example.com", result.getContent().get(0).getEmail());
		assertEquals("user2@example.com", result.getContent().get(1).getEmail());
		verify(userService).findAllForAdmin(null, null, null, pageable);
	}

	@Test
	void getAllUsers_WithSearch_ShouldReturnFilteredUsers() {
		String search = "john";
		Pageable pageable = PageRequest.of(0, 10);
		AdminUserListResponse user = AdminUserListResponse.builder().id(1L).email("john@example.com").build();
		Page<AdminUserListResponse> userPage = new PageImpl<>(List.of(user));

		when(userService.findAllForAdmin(eq(search), isNull(), isNull(), eq(pageable))).thenReturn(userPage);

		Page<AdminUserListResponse> result = adminUserController.getAllUsers(search, null, null, pageable);

		assertEquals(1, result.getContent().size());
		assertEquals("john@example.com", result.getContent().get(0).getEmail());
		verify(userService).findAllForAdmin(search, null, null, pageable);
	}

	@Test
	void getAllUsers_WithRole_ShouldReturnFilteredUsers() {
		UserRole role = UserRole.ROLE_ADMIN;
		Pageable pageable = PageRequest.of(0, 10);
		AdminUserListResponse user = AdminUserListResponse.builder().id(1L).email("admin@example.com").build();
		Page<AdminUserListResponse> userPage = new PageImpl<>(List.of(user));

		when(userService.findAllForAdmin(isNull(), eq(role), isNull(), eq(pageable))).thenReturn(userPage);

		Page<AdminUserListResponse> result = adminUserController.getAllUsers(null, role, null, pageable);

		assertEquals(1, result.getContent().size());
		assertEquals("admin@example.com", result.getContent().get(0).getEmail());
		verify(userService).findAllForAdmin(null, role, null, pageable);
	}

	@Test
	void getAllUsers_WithEnabled_ShouldReturnFilteredUsers() {
		Boolean enabled = true;
		Pageable pageable = PageRequest.of(0, 10);
		AdminUserListResponse user = AdminUserListResponse.builder().id(1L).email("active@example.com").build();
		Page<AdminUserListResponse> userPage = new PageImpl<>(List.of(user));

		when(userService.findAllForAdmin(isNull(), isNull(), eq(enabled), eq(pageable))).thenReturn(userPage);

		Page<AdminUserListResponse> result = adminUserController.getAllUsers(null, null, enabled, pageable);

		assertEquals(1, result.getContent().size());
		assertEquals("active@example.com", result.getContent().get(0).getEmail());
		verify(userService).findAllForAdmin(null, null, enabled, pageable);
	}

	@Test
	void getAllUsers_WithAllFilters_ShouldReturnFilteredUsers() {
		String search = "admin";
		UserRole role = UserRole.ROLE_ADMIN;
		Boolean enabled = true;
		Pageable pageable = PageRequest.of(0, 10);
		AdminUserListResponse user = AdminUserListResponse.builder().id(1L).email("admin@example.com").build();
		Page<AdminUserListResponse> userPage = new PageImpl<>(List.of(user));

		when(userService.findAllForAdmin(eq(search), eq(role), eq(enabled), eq(pageable))).thenReturn(userPage);

		Page<AdminUserListResponse> result = adminUserController.getAllUsers(search, role, enabled, pageable);

		assertEquals(1, result.getContent().size());
		assertEquals("admin@example.com", result.getContent().get(0).getEmail());
		verify(userService).findAllForAdmin(search, role, enabled, pageable);
	}

	@Test
	void updateUserRole_ShouldUpdateRole() {
		Long userId = 1L;
		UserRoleUpdateRequest request = new UserRoleUpdateRequest(UserRole.ROLE_ADMIN);

		ResponseEntity<Void> response = adminUserController.updateUserRole(userId, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(userService).updateUserRole(userId, UserRole.ROLE_ADMIN);
	}

	@Test
	void updateUserStatus_ShouldUpdateStatus() {
		Long userId = 1L;
		UserStatusUpdateRequest request = new UserStatusUpdateRequest(true);

		ResponseEntity<Void> response = adminUserController.updateUserStatus(userId, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(userService).updateUserStatus(userId, true);
	}

	@Test
	void updateUserStatus_ShouldDisableUser() {
		Long userId = 1L;
		UserStatusUpdateRequest request = new UserStatusUpdateRequest(false);

		ResponseEntity<Void> response = adminUserController.updateUserStatus(userId, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		verify(userService).updateUserStatus(userId, false);
	}
}