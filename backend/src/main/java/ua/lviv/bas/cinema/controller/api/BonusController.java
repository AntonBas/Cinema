package ua.lviv.bas.cinema.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusCardResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.service.bonus.BonusService;

@RestController
@RequestMapping("/api/bonus")
@RequiredArgsConstructor
@Tag(name = "Bonus", description = "API for user bonus system operations")
@SecurityRequirement(name = "bearerAuth")
public class BonusController {

	private final BonusService bonusService;

	@RateLimit(value = 20, duration = 1, key = "user")
	@Operation(summary = "Get user bonus card", description = "Returns the current user's bonus card information")
	@ApiResponse(responseCode = "200", description = "Bonus card retrieved successfully")
	@ApiResponse(responseCode = "404", description = "Bonus card not found")
	@GetMapping("/my-card")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public BonusCardResponse getMyBonusCard(
			@Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId) {
		return bonusService.getCard(userId);
	}

	@RateLimit(value = 20, duration = 1, key = "user")
	@Operation(summary = "Get bonus balance", description = "Returns the current user's bonus balance with conversion details")
	@ApiResponse(responseCode = "200", description = "Balance retrieved successfully")
	@ApiResponse(responseCode = "404", description = "Bonus card not found")
	@GetMapping("/my-balance")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public BonusBalanceResponse getMyBalance(
			@Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId) {
		return bonusService.getBalance(userId);
	}

	@RateLimit(value = 30, duration = 1, key = "user")
	@Operation(summary = "Get user bonus transactions", description = "Returns the current user's bonus transaction history")
	@ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
	@GetMapping("/my-transactions")
	@ResponseStatus(HttpStatus.OK)
	@PreAuthorize("isAuthenticated()")
	public PageResponse<BonusTransactionResponse> getMyTransactions(
			@Parameter(hidden = true) @AuthenticationPrincipal(expression = "userId") Long userId,
			@PageableDefault(size = 20) Pageable pageable) {
		Page<BonusTransactionResponse> page = bonusService.getTransactions(userId, pageable);
		return PageResponse.from(page);
	}
}