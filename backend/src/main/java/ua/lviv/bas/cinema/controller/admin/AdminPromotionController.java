package ua.lviv.bas.cinema.controller.admin;

import java.util.List;

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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;

@RestController
@RequestMapping("/api/admin/promotions")
@RequiredArgsConstructor
@Tag(name = "Admin Promotion", description = "Promotion management APIs for administrators")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {

	private final AdminPromotionService promotionService;

	@PostMapping
	@Operation(summary = "Create a new promotion", description = "Creates a new promotion in the system")
	public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionCreateRequest request) {

		PromotionResponse response = promotionService.createPromotion(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{promotionId}")
	@Operation(summary = "Get promotion by ID", description = "Get detailed information about a promotion")
	public ResponseEntity<PromotionResponse> getPromotion(@PathVariable Long promotionId) {
		PromotionResponse response = promotionService.getPromotionById(promotionId);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	@Operation(summary = "Get all promotions", description = "Get list of all promotions")
	public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
		List<PromotionResponse> responses = promotionService.getAllPromotions();
		return ResponseEntity.ok(responses);
	}

	@GetMapping("/active")
	@Operation(summary = "Get active promotions", description = "Get list of currently active promotions")
	public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
		List<PromotionResponse> responses = promotionService.getActivePromotions();
		return ResponseEntity.ok(responses);
	}

	@PutMapping("/{promotionId}")
	@Operation(summary = "Update promotion", description = "Update an existing promotion by ID")
	public ResponseEntity<PromotionResponse> updatePromotion(
			@Parameter(description = "Promotion ID") @PathVariable Long promotionId,
			@Valid @RequestBody PromotionUpdateRequest request) {
		PromotionResponse response = promotionService.updatePromotion(promotionId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{promotionId}")
	@Operation(summary = "Delete promotion", description = "Delete a promotion by ID")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<Void> deletePromotion(
			@Parameter(description = "Promotion ID") @PathVariable Long promotionId) {
		promotionService.deletePromotion(promotionId);
		return ResponseEntity.noContent().build();
	}
}