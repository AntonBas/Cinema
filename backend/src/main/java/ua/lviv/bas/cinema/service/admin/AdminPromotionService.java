package ua.lviv.bas.cinema.service.admin;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionDatesInvalidException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionHasRedemptionsException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.PromotionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPromotionService {

	private final PromotionRepository promotionRepository;
	private final PromotionMapper promotionMapper;

	@Transactional
	public PromotionResponse createPromotion(PromotionCreateRequest request) {
		log.info("Creating new promotion: {}", request.title());

		if (promotionRepository.existsByTitle(request.title())) {
			throw PromotionAlreadyExistsException.forTitle(request.title());
		}

		validateDates(request.startDate(), request.endDate());

		Promotion promotion = promotionMapper.toPromotion(request);
		promotion = promotionRepository.save(promotion);

		log.info("Promotion created with ID: {}", promotion.getId());
		return promotionMapper.toPromotionResponse(promotion);
	}

	@Transactional
	public PromotionResponse updatePromotion(Long promotionId, PromotionUpdateRequest request) {
		log.info("Updating promotion with ID: {}", promotionId);

		Promotion promotion = findByIdOrThrow(promotionId);
		promotionMapper.updatePromotionFromRequest(promotion, request);

		promotion = promotionRepository.save(promotion);
		return promotionMapper.toPromotionResponse(promotion);
	}

	@Transactional
	public void deletePromotion(Long promotionId) {
		log.info("Deleting promotion with ID: {}", promotionId);

		Promotion promotion = findByIdOrThrow(promotionId);

		if (!promotion.getUserRedemptions().isEmpty()) {
			int redemptionCount = promotion.getUserRedemptions().size();
			throw new PromotionHasRedemptionsException(promotionId, redemptionCount);
		}

		promotionRepository.delete(promotion);
		log.info("Promotion with ID: {} has been deleted", promotionId);
	}

	public PromotionResponse getPromotionById(Long promotionId) {
		Promotion promotion = findByIdOrThrow(promotionId);
		return promotionMapper.toPromotionResponse(promotion);
	}

	public PageResponse<PromotionAdminResponse> getAllPromotions(Pageable pageable) {
		Page<PromotionAdminProjection> page = promotionRepository.findAllAdminList(pageable);
		return PageResponse.from(page.map(promotionMapper::toPromotionAdminResponse));
	}

	public Promotion findByIdOrThrow(Long promotionId) {
		return promotionRepository.findById(promotionId).orElseThrow(() -> new PromotionNotFoundException(promotionId));
	}

	private void validateDates(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
			throw new PromotionDatesInvalidException(startDate, endDate);
		}
	}
}