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
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
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
	@PreAuthorize("permitAll()")
	public ResponseEntity<List<PromotionResponse>> getAvailablePromotions(
			@AuthenticationPrincipal CustomUserDetails currentUser) {
		User user = currentUser != null ? currentUser.getUser() : null;
		log.info("Getting available promotions for user: {}", user != null ? user.getId() : "anonymous");
		return ResponseEntity.ok(promotionService.getAvailablePromotions(user));
	}

	@RateLimit(value = 3, duration = 1, key = "user")
	@PostMapping("/claim")
	@Operation(summary = "Claim a promotion")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PromotionResponse> claimPromotion(@Valid @RequestBody UserPromotionCreateRequest request,
			@AuthenticationPrincipal CustomUserDetails currentUser) {
		User user = currentUser.getUser();
		log.info("User ID: {} claiming promotion ID: {}", user.getId(), request.promotionId());
		return ResponseEntity.ok(promotionService.claimPromotion(request, user));
	}
}