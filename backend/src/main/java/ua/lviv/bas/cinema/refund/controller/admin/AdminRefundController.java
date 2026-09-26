package ua.lviv.bas.cinema.refund.controller.admin;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.common.PageResponse;
import ua.lviv.bas.cinema.refund.domain.status.RefundStatus;
import ua.lviv.bas.cinema.refund.dto.response.AdminRefundListResponse;
import ua.lviv.bas.cinema.refund.service.AdminRefundService;

@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
@Tag(name = "Admin Refund Management", description = "Endpoints for monitoring refunds")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class AdminRefundController {

    private final AdminRefundService adminRefundService;

    @GetMapping
    @Operation(summary = "Get refunds with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refunds retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort property"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public PageResponse<AdminRefundListResponse> getRefunds(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(defaultValue = "false") boolean needsAttention,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault Pageable pageable) {
        return PageResponse.from(adminRefundService.getRefunds(query, status, needsAttention, userId, dateFrom,
                dateTo, pageable));
    }
}
