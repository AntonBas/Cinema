package ua.lviv.bas.cinema.bonus.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.bonus.dto.request.BonusRulesRequest;
import ua.lviv.bas.cinema.bonus.dto.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.bonus.dto.response.BonusRulesResponse;
import ua.lviv.bas.cinema.bonus.dto.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.bonus.service.AdminBonusService;
import ua.lviv.bas.cinema.bonus.service.BonusQueryService;
import ua.lviv.bas.cinema.common.PageResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bonus")
@RequiredArgsConstructor
@Tag(name = "Bonus Admin", description = "API for administrative management of the bonus system")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminBonusController {

    private final AdminBonusService bonusAdminService;
    private final BonusQueryService bonusQueryService;

    @GetMapping("/rules")
    @Operation(summary = "Get all bonus rules")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of bonus rules retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public List<BonusRulesResponse> getRules() {
        return bonusAdminService.getRules();
    }

    @PutMapping("/rules/{type}")
    @Operation(summary = "Update bonus rule")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bonus rule updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Bonus rule not found")
    })
    public BonusRulesResponse updateRule(@PathVariable BonusTransactionType type,
                                         @Valid @RequestBody BonusRulesRequest request) {
        return bonusAdminService.updateRule(type, request);
    }

    @PutMapping("/rules/{type}/reset")
    @Operation(summary = "Reset bonus rule to defaults")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bonus rule reset successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Bonus rule not found")
    })
    public BonusRulesResponse resetRule(@PathVariable BonusTransactionType type) {
        return bonusAdminService.resetRuleToDefaults(type);
    }

    @GetMapping("/users/{userId}/balance")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    @Operation(summary = "Get bonus balance of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Bonus card not found")
    })
    public BonusBalanceResponse getUserBalance(@PathVariable Long userId) {
        return bonusQueryService.getBalance(userId);
    }

    @GetMapping("/users/{userId}/transactions")
    @PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
    @Operation(summary = "Get bonus transactions of a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public PageResponse<BonusTransactionResponse> getUserTransactions(@PathVariable Long userId,
                                                                      @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(bonusQueryService.getTransactions(userId, pageable));
    }
}
