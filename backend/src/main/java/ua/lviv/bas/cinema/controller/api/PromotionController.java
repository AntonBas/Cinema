package ua.lviv.bas.cinema.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.promotion.request.ClaimPromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.service.promotion.PromotionService;

import java.util.List;

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
    public List<PromotionResponse> getAvailablePromotions(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser != null ? currentUser.getUser() : null;
        log.info("GET /api/promotions - user: {}", user != null ? user.getId() : "anonymous");
        return promotionService.getAvailablePromotions(user);
    }

    @RateLimit(value = 3, duration = 1, key = "user")
    @PostMapping("/claim")
    @Operation(summary = "Claim a promotion")
    @PreAuthorize("isAuthenticated()")
    public PromotionResponse claimPromotion(@Valid @RequestBody ClaimPromotionRequest request,
                                            @AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser.getUser();
        log.info("POST /api/promotions/claim - user: {}, promotionId: {}", user.getId(), request.promotionId());
        return promotionService.claimPromotion(request, user);
    }

    @GetMapping("/claimed")
    @Operation(summary = "Get claimed promotions")
    @PreAuthorize("isAuthenticated()")
    public List<PromotionResponse> getClaimedPromotions(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = currentUser.getUser();
        log.info("GET /api/promotions/claimed - user: {}", user.getId());
        return promotionService.getClaimedPromotions(user);
    }
}