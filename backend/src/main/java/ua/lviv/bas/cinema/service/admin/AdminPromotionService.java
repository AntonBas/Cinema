package ua.lviv.bas.cinema.service.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
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
		log.info("Creating new promotion: {}", request.getTitle());

		if (promotionRepository.existsByTitle(request.getTitle())) {
			throw PromotionAlreadyExistsException.forTitle(request.getTitle());
		}

		if (request.getStartDate() != null && request.getEndDate() != null
				&& request.getEndDate().isBefore(request.getStartDate())) {
			throw new PromotionDatesInvalidException(request.getStartDate(), request.getEndDate());
		}

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

	public PageResponse<PromotionResponse> getAllPromotions(Pageable pageable) {
		Page<Promotion> page = promotionRepository.findAll(pageable);
		List<PromotionResponse> responses = promotionMapper.toPromotionResponseList(page.getContent());
		return PageResponse.from(new PageImpl<>(responses, pageable, page.getTotalElements()));
	}

	public PageResponse<PromotionResponse> getActivePromotions(Pageable pageable) {
		List<PromotionResponseProjection> projections = promotionRepository.findAllPromotions(true);
		List<PromotionResponse> responses = promotionMapper.toPromotionResponseListFromProjections(projections);

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), responses.size());
		List<PromotionResponse> pageContent = responses.subList(start, end);

		return PageResponse.from(new PageImpl<>(pageContent, pageable, responses.size()));
	}

	public Promotion findByIdOrThrow(Long promotionId) {
		return promotionRepository.findById(promotionId).orElseThrow(() -> new PromotionNotFoundException(promotionId));
	}

	public boolean isPromotionActive(Promotion promotion) {
		if (promotion == null) {
			return false;
		}

		LocalDate now = LocalDate.now();
		LocalDate start = promotion.getStartDate();
		LocalDate end = promotion.getEndDate();

		boolean afterStart = (start == null) || !now.isBefore(start);
		boolean beforeEnd = (end == null) || !now.isAfter(end);

		return afterStart && beforeEnd;
	}
}