package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;

@Slf4j
@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
@Tag(name = "Admin Promotion", description = "Promotion management APIs for administrators")
@SecurityRequirement(name = "bearerAuth")
public class AdminPromotionController {

	private final AdminPromotionService promotionService;

	@PostMapping
	@Operation(summary = "Create a new promotion")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionCreateRequest request) {
		log.info("Admin creating new promotion with title: {}", request.title());
		return ResponseEntity.status(HttpStatus.CREATED).body(promotionService.createPromotion(request));
	}

	@GetMapping("/{promotionId}")
	@Operation(summary = "Get promotion by ID")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PromotionResponse> getPromotion(@PathVariable Long promotionId) {
		log.info("Admin retrieving promotion with ID: {}", promotionId);
		return ResponseEntity.ok(promotionService.getPromotionById(promotionId));
	}

	@GetMapping
	@Operation(summary = "Get all promotions (admin view)")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PageResponse<PromotionAdminResponse>> getAllPromotions(
			@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		log.info("Admin retrieving all promotions");
		return ResponseEntity.ok(promotionService.getAllPromotions(pageable));
	}

	@PutMapping("/{promotionId}")
	@Operation(summary = "Update promotion")
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PromotionResponse> updatePromotion(@PathVariable Long promotionId,
			@Valid @RequestBody PromotionUpdateRequest request) {
		log.info("Admin updating promotion with ID: {}", promotionId);
		return ResponseEntity.ok(promotionService.updatePromotion(promotionId, request));
	}

	@DeleteMapping("/{promotionId}")
	@Operation(summary = "Delete promotion")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Void> deletePromotion(@PathVariable Long promotionId) {
		log.info("Admin deleting promotion with ID: {}", promotionId);
		promotionService.deletePromotion(promotionId);
		return ResponseEntity.noContent().build();
	}
}