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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
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
	@Operation(summary = "Create a new promotion", description = "Creates a new promotion in the system with the provided details")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Promotion created successfully", content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionCreateRequest request) {

		log.info("Admin creating new promotion with title: {}", request.getTitle());
		PromotionResponse response = promotionService.createPromotion(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{promotionId}")
	@Operation(summary = "Get promotion by ID", description = "Retrieves detailed information about a specific promotion by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Promotion found successfully", content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
			@ApiResponse(responseCode = "404", description = "Promotion not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PromotionResponse> getPromotion(
			@Parameter(description = "ID of the promotion to retrieve", required = true, example = "1") @PathVariable Long promotionId) {

		log.info("Admin retrieving promotion with ID: {}", promotionId);
		PromotionResponse response = promotionService.getPromotionById(promotionId);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	@Operation(summary = "Get all promotions", description = "Retrieves a list of all promotions in the system")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Promotions retrieved successfully", content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
		log.info("Admin retrieving all promotions");
		List<PromotionResponse> responses = promotionService.getAllPromotions();
		return ResponseEntity.ok(responses);
	}

	@GetMapping("/active")
	@Operation(summary = "Get active promotions", description = "Retrieves a list of currently active promotions")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Active promotions retrieved successfully", content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
		log.info("Admin retrieving active promotions");
		List<PromotionResponse> responses = promotionService.getActivePromotions();
		return ResponseEntity.ok(responses);
	}

	@PutMapping("/{promotionId}")
	@Operation(summary = "Update promotion", description = "Updates an existing promotion with the provided data")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Promotion updated successfully", content = @Content(schema = @Schema(implementation = PromotionResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Promotion not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<PromotionResponse> updatePromotion(
			@Parameter(description = "ID of the promotion to update", required = true, example = "1") @PathVariable Long promotionId,

			@Valid @RequestBody PromotionUpdateRequest request) {

		log.info("Admin updating promotion with ID: {}", promotionId);
		PromotionResponse response = promotionService.updatePromotion(promotionId, request);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{promotionId}")
	@Operation(summary = "Delete promotion", description = "Deletes a promotion from the system. This action is irreversible.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Promotion deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Promotion not found", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Cannot delete promotion (e.g., it has active redemptions)", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = @Content(schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden - admin access required", content = @Content(schema = @Schema(implementation = String.class))) })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Void> deletePromotion(
			@Parameter(description = "ID of the promotion to delete", required = true, example = "1") @PathVariable Long promotionId) {

		log.info("Admin deleting promotion with ID: {}", promotionId);
		promotionService.deletePromotion(promotionId);
		return ResponseEntity.noContent().build();
	}
}