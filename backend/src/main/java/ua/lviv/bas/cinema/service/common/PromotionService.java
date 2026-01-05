package ua.lviv.bas.cinema.service.common;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.PromotionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final PromotionMapper promotionMapper;

	@Transactional
	public PromotionResponse createPromotion(PromotionCreateRequest request) {
		log.info("Creating new promotion: {}", request.getTitle());

		if (promotionRepository.existsByTitle(request.getTitle())) {
			throw new IllegalArgumentException("Promotion with title '" + request.getTitle() + "' already exists.");
		}

		Promotion promotion = promotionMapper.toEntity(request);
		promotion = promotionRepository.save(promotion);

		log.info("Promotion created with ID: {}", promotion.getId());
		return promotionMapper.toResponse(promotion);
	}

	@Transactional
	public PromotionResponse updatePromotion(Long promotionId, PromotionUpdateRequest request) {
		log.info("Updating promotion with ID: {}", promotionId);

		Promotion promotion = findByIdOrThrow(promotionId);
		promotionMapper.updateEntity(promotion, request);

		promotion = promotionRepository.save(promotion);
		return promotionMapper.toResponse(promotion);
	}

	public void deletePromotion(Long promotionId) {
		log.info("Deleting promotion with ID: {}", promotionId);

		Promotion promotion = findByIdOrThrow(promotionId);

		if (!promotion.getUserRedemptions().isEmpty()) {
			throw new IllegalStateException("Cannot delete promotion with existing user redemptions.");
		}

		promotionRepository.delete(promotion);

		log.info("Promotion with ID: {} has been deleted", promotionId);
	}

	public PromotionResponse getPromotionById(Long promotionId) {
		Promotion promotion = findByIdOrThrow(promotionId);
		return promotionMapper.toResponse(promotion);
	}

	public List<PromotionResponse> getAllPromotions() {
		List<Promotion> promotions = promotionRepository.findAll();
		return promotionMapper.toResponseList(promotions);
	}

	public List<PromotionResponse> getActivePromotions() {
		List<Promotion> promotions = promotionRepository.findAll();
		return promotionMapper.toResponseList(promotions);
	}

	public Promotion findByIdOrThrow(Long promotionId) {
		return promotionRepository.findById(promotionId)
				.orElseThrow(() -> new IllegalArgumentException("Promotion not found with id: " + promotionId));
	}

	public boolean isPromotionActive(Promotion promotion) {
		if (promotion == null) {
			return false;
		}

		LocalDateTime now = LocalDateTime.now();
		LocalDateTime start = promotion.getStartDate();
		LocalDateTime end = promotion.getEndDate();

		boolean afterStart = (start == null) || !now.isBefore(start);
		boolean beforeEnd = (end == null) || !now.isAfter(end);

		return afterStart && beforeEnd;
	}
}