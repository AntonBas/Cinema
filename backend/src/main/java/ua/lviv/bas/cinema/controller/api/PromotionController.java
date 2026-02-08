package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.service.user.PromotionService;

@Slf4j
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "Promotion management APIs for users")
@SecurityRequirement(name = "bearerAuth")
public class PromotionController {

	private final PromotionService promotionService;

	@GetMapping
	@Operation(summary = "Get available promotions", description = "Get list of promotions available for the current user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Promotions retrieved successfully") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<List<PromotionResponse>> getAvailablePromotions(@AuthenticationPrincipal User user) {
		log.info("Getting available promotions for user ID: {}", user.getId());
		List<PromotionResponse> promotions = promotionService.getAvailablePromotions(user);
		return ResponseEntity.ok(promotions);
	}

	@GetMapping("/my")
	@Operation(summary = "Get user's claimed promotions", description = "Get list of promotions claimed by the current user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Promotions retrieved successfully") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<List<UserPromotionResponse>> getUserPromotions(@AuthenticationPrincipal User user) {
		log.info("Getting claimed promotions for user ID: {}", user.getId());
		List<UserPromotionResponse> responses = promotionService.getUserPromotions(user);
		return ResponseEntity.ok(responses);
	}

	@PostMapping("/claim")
	@Operation(summary = "Claim a promotion", description = "Claim a promotion to receive bonus points")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Promotion claimed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request"),
			@ApiResponse(responseCode = "404", description = "Promotion not found"),
			@ApiResponse(responseCode = "409", description = "Promotion already claimed or not active") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<UserPromotionResponse> claimPromotion(@Valid @RequestBody UserPromotionCreateRequest request,
			@AuthenticationPrincipal User user) {
		log.info("User ID: {} claiming promotion ID: {}", user.getId(), request.getPromotionId());
		UserPromotionResponse response = promotionService.claimPromotion(request, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{promotionId}/status")
	@Operation(summary = "Check promotion status", description = "Check if promotion is available and claimable by user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Status checked successfully"),
			@ApiResponse(responseCode = "404", description = "Promotion not found") })
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<Boolean> checkPromotionStatus(
			@Parameter(description = "Promotion ID") @PathVariable Long promotionId,
			@AuthenticationPrincipal User user) {
		log.info("Checking promotion status for promotion ID: {} and user ID: {}", promotionId, user.getId());
		boolean isAvailable = promotionService.hasUserClaimedPromotion(user, promotionId) ? false
				: promotionService.getAvailablePromotions(user).stream().anyMatch(p -> p.getId().equals(promotionId));
		return ResponseEntity.ok(isAvailable);
	}
}