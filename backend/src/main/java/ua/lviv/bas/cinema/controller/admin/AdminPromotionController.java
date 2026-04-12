package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionListResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.service.promotion.PromotionService;

@Slf4j
@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
@Tag(name = "Admin Promotion", description = "Promotion management APIs for administrators")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminPromotionController {

	private final PromotionService promotionService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a new promotion")
	public PromotionResponse createPromotion(@Valid @RequestBody PromotionRequest request) {
		log.info("POST /api/admin/promotions - Creating new promotion: {}", request.title());
		return promotionService.createPromotion(request);
	}

	@GetMapping
	@Operation(summary = "Get all promotions")
	public PageResponse<PromotionListResponse> getPromotions(@RequestParam(required = false) String query,
			@PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
		log.info("GET /api/admin/promotions - query: '{}'", query);
		return PageResponse.from(promotionService.getPromotions(query, pageable));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update promotion")
	public PromotionResponse updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionRequest request) {
		log.info("PUT /api/admin/promotions/{} - Updating promotion", id);
		return promotionService.updatePromotion(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete promotion")
	public void deletePromotion(@PathVariable Long id) {
		log.info("DELETE /api/admin/promotions/{} - Deleting promotion", id);
		promotionService.deletePromotion(id);
	}
}