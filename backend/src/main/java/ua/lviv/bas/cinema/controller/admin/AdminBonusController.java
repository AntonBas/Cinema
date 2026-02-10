package ua.lviv.bas.cinema.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.request.BonusRulesRequest;
import ua.lviv.bas.cinema.dto.bonus.response.BonusRulesResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.service.admin.AdminBonusService;

@RestController
@RequestMapping("/api/admin/bonus")
@RequiredArgsConstructor
@Tag(name = "Bonus Admin", description = "API for administrative management of the bonus system")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminBonusController {

	private final AdminBonusService bonusAdminService;

	@Operation(summary = "Get all bonus rules", description = "Returns a list of all bonus rules")
	@ApiResponse(responseCode = "200", description = "List of bonus rules retrieved successfully")
	@GetMapping("/rules")
	@ResponseStatus(HttpStatus.OK)
	public List<BonusRulesResponse> getAllBonusRules() {
		return bonusAdminService.getAllRules();
	}

	@Operation(summary = "Get bonus rule by type", description = "Returns details of a bonus rule by the specified type")
	@ApiResponse(responseCode = "200", description = "Bonus rule found")
	@ApiResponse(responseCode = "404", description = "Bonus rule not found")
	@GetMapping("/rules/{type}")
	@ResponseStatus(HttpStatus.OK)
	public BonusRulesResponse getBonusRule(@PathVariable BonusTransactionType type) {
		return bonusAdminService.getRule(type);
	}

	@Operation(summary = "Update bonus rule", description = "Updates a bonus rule by the specified type")
	@ApiResponse(responseCode = "200", description = "Bonus rule updated successfully")
	@ApiResponse(responseCode = "404", description = "Bonus rule not found")
	@PutMapping("/rules/{type}")
	@ResponseStatus(HttpStatus.OK)
	public BonusRulesResponse updateBonusRule(@PathVariable BonusTransactionType type,
			@Valid @RequestBody BonusRulesRequest request) {
		return bonusAdminService.updateRule(type, request);
	}

	@Operation(summary = "Reset bonus rule to defaults", description = "Resets a bonus rule to its default values")
	@ApiResponse(responseCode = "200", description = "Bonus rule reset successfully")
	@ApiResponse(responseCode = "404", description = "Bonus rule not found")
	@PutMapping("/rules/{type}/reset")
	@ResponseStatus(HttpStatus.OK)
	public BonusRulesResponse resetBonusRule(@PathVariable BonusTransactionType type) {
		return bonusAdminService.resetRuleToDefaults(type);
	}

	@Operation(summary = "Get user transactions", description = "Returns bonus transaction history for a specific user")
	@ApiResponse(responseCode = "200", description = "User transactions retrieved successfully")
	@GetMapping("/users/{userId}/transactions")
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<BonusTransactionResponse> getUserTransactions(@PathVariable Long userId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<BonusTransactionResponse> page = bonusAdminService.getUserTransactions(userId, pageable);
		return PageResponse.from(page);
	}

	@Operation(summary = "Get all transactions", description = "Returns all bonus transactions in the system")
	@ApiResponse(responseCode = "200", description = "All transactions retrieved successfully")
	@GetMapping("/transactions")
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<BonusTransactionResponse> getAllTransactions(@PageableDefault(size = 20) Pageable pageable) {
		Page<BonusTransactionResponse> page = bonusAdminService.getAllTransactions(pageable);
		return PageResponse.from(page);
	}

	@Operation(summary = "Get transactions by type", description = "Returns bonus transactions filtered by type")
	@ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
	@GetMapping("/transactions/type/{type}")
	@ResponseStatus(HttpStatus.OK)
	public PageResponse<BonusTransactionResponse> getTransactionsByType(@PathVariable BonusTransactionType type,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<BonusTransactionResponse> page = bonusAdminService.getTransactionsByType(type, pageable);
		return PageResponse.from(page);
	}
}