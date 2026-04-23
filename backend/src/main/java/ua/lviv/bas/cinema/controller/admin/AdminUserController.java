package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.user.request.UserRoleUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.UserStatusUpdateRequest;
import ua.lviv.bas.cinema.dto.user.request.VerificationBirthDateRequest;
import ua.lviv.bas.cinema.dto.user.response.AdminUserListResponse;
import ua.lviv.bas.cinema.service.user.AdminUserService;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User Management", description = "Endpoints for managing users (Admin only)")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "Get users with filters")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Users retrieved successfully")})
    public PageResponse<AdminUserListResponse> getUsers(@RequestParam(required = false) String query,
                                                        @RequestParam(required = false) UserRole role,
                                                        @RequestParam(required = false) VerificationStatus verificationStatus,
                                                        @RequestParam(required = false) Boolean enabled,
                                                        @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/admin/users - search: {}, role: {}, verificationStatus: {}, enabled: {}", query, role,
                verificationStatus, enabled);
        return PageResponse.from(adminUserService.getUsers(query, role, verificationStatus, enabled, pageable));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Update user role")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public AdminUserListResponse updateRole(@PathVariable Long userId,
                                            @Valid @RequestBody UserRoleUpdateRequest request) {
        log.info("PATCH /api/admin/users/{}/role - Updating role to {}", userId, request.userRole());
        return adminUserService.updateRole(userId, request.userRole());
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update user status")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public AdminUserListResponse updateStatus(@PathVariable Long userId,
                                              @Valid @RequestBody UserStatusUpdateRequest request) {
        log.info("PATCH /api/admin/users/{}/status - Updating status to {}", userId, request.enabled());
        return adminUserService.updateStatus(userId, request.enabled());
    }

    @PatchMapping("/{userId}/verification")
    @Operation(summary = "Update verification status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verification status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public AdminUserListResponse updateVerification(@PathVariable Long userId,
                                                    @Valid @RequestBody VerificationBirthDateRequest request) {
        log.info("PATCH /api/admin/users/{}/verification - Updating verification to {}", userId,
                request.verificationStatus());
        return adminUserService.updateVerification(userId, request.verificationStatus());
    }
}