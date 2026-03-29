package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.service.promotion.PromotionService;

@Slf4j
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotion", description = "Promotion management APIs for users")
@SecurityRequirement(name = "bearerAuth")
public class PromotionController {

	private final PromotionService promotionService;

	@GetMapping
	@Operation(summary = "Get available promotions")
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<List<PromotionResponse>> getAvailablePromotions(@AuthenticationPrincipal User user) {
		log.info("Getting available promotions for user ID: {}", user.getId());
		return ResponseEntity.ok(promotionService.getAvailablePromotions(user));
	}

	@PostMapping("/claim")
	@Operation(summary = "Claim a promotion")
	@PreAuthorize("hasRole('USER') or hasRole('PREMIUM_USER')")
	public ResponseEntity<PromotionResponse> claimPromotion(@Valid @RequestBody UserPromotionCreateRequest request,
			@AuthenticationPrincipal User user) {
		log.info("User ID: {} claiming promotion ID: {}", user.getId(), request.promotionId());
		return ResponseEntity.ok(promotionService.claimPromotion(request, user));
	}
}