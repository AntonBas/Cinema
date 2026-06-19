package ua.lviv.bas.cinema.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.service.bonus.AdminBonusService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/bonus")
@RequiredArgsConstructor
@Tag(name = "Bonus Admin", description = "API for administrative management of the bonus system")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminBonusController {

    private final AdminBonusService bonusAdminService;

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
}