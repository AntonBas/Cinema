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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;
import ua.lviv.bas.cinema.service.user.PromotionService;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "Promotion management APIs for users")
public class PromotionController {

	private final AdminPromotionService promotionService;
	private final PromotionService userPromotionService;

	@GetMapping
	@Operation(summary = "Get available promotions", description = "Get list of promotions available for the current user")
	public ResponseEntity<List<PromotionResponse>> getAvailablePromotions(@AuthenticationPrincipal User user) {
		List<PromotionResponse> allPromotions = promotionService.getAllPromotions();

		List<PromotionResponse> availablePromotions = allPromotions.stream().filter(promotion -> {
			try {
				return promotionService.isPromotionActive(promotionService.findByIdOrThrow(promotion.getId()))
						&& !userPromotionService.hasUserClaimedPromotion(user, promotion.getId());
			} catch (Exception e) {
				return false;
			}
		}).toList();

		return ResponseEntity.ok(availablePromotions);
	}

	@GetMapping("/my")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get user's claimed promotions", description = "Get list of promotions claimed by the current user")
	public ResponseEntity<List<UserPromotionResponse>> getUserPromotions(@AuthenticationPrincipal User user) {
		List<UserPromotionResponse> responses = userPromotionService.getUserPromotions(user);
		return ResponseEntity.ok(responses);
	}

	@PostMapping("/claim")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Claim a promotion", description = "Claim a promotion to receive bonus points")
	public ResponseEntity<UserPromotionResponse> claimPromotion(@Valid @RequestBody UserPromotionCreateRequest request,
			@AuthenticationPrincipal User user) {
		UserPromotionResponse response = userPromotionService.claimPromotion(request, user);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{promotionId}/status")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Check promotion status", description = "Check if promotion is available and claimable by user")
	public ResponseEntity<Boolean> checkPromotionStatus(
			@Parameter(description = "Promotion ID") @PathVariable Long promotionId,
			@AuthenticationPrincipal User user) {
		boolean isAvailable = userPromotionService.isPromotionAvailableForUser(user, promotionId);
		return ResponseEntity.ok(isAvailable);
	}
}