package ua.lviv.bas.cinema.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.service.UserService;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

	private final UserService userService;

	@GetMapping
	public Page<AdminUserListResponse> getAllUsers(Pageable pageable) {
		return userService.findAllForAdmin(pageable);
	}

	@PatchMapping("/{userId}/role")
	public ResponseEntity<Void> updateUserRole(@PathVariable Long userId, @RequestBody UserRoleUpdateRequest request) {
		userService.updateUserRole(userId, request.getUserRole());
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{userId}/status")
	public ResponseEntity<Void> updateUserStatus(@PathVariable Long userId,
			@RequestBody UserStatusUpdateRequest request) {
		userService.updateUserStatus(userId, request.isEnabled());
		return ResponseEntity.ok().build();
	}
}
