package ua.lviv.bas.cinema.service.promotion;

import java.time.LocalDate;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionDatesInvalidException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionHasRedemptionsException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.mapper.promotion.PromotionMapper;
import ua.lviv.bas.cinema.repository.promotion.PromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.service.shared.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "promotions")
public class AdminPromotionService {

	private final PromotionRepository promotionRepository;
	private final PromotionMapper promotionMapper;
	private final AuditService auditService;

	@Caching(evict = { @CacheEvict(key = "'all-' + 0 + '-' + 20"), @CacheEvict(allEntries = true) })
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

		auditService.logChange("Promotion", promotion.getId(), AuditAction.CREATED, null, promotion.getTitle());

		return promotionMapper.toPromotionResponse(promotion);
	}

	@Caching(evict = { @CacheEvict(key = "#promotionId"), @CacheEvict(key = "'all-' + 0 + '-' + 20"),
			@CacheEvict(allEntries = true) })
	@Transactional
	public PromotionResponse updatePromotion(Long promotionId, PromotionUpdateRequest request) {
		log.info("Updating promotion with ID: {}", promotionId);

		Promotion oldPromotion = findByIdOrThrow(promotionId);
		Promotion promotion = findByIdOrThrow(promotionId);
		promotionMapper.updatePromotionFromRequest(promotion, request);

		promotion = promotionRepository.save(promotion);

		auditService.logChange("Promotion", promotionId, AuditAction.UPDATED, oldPromotion.getTitle(),
				promotion.getTitle());

		return promotionMapper.toPromotionResponse(promotion);
	}

	@Caching(evict = { @CacheEvict(key = "#promotionId"), @CacheEvict(key = "'all-' + 0 + '-' + 20"),
			@CacheEvict(allEntries = true) })
	@Transactional
	public void deletePromotion(Long promotionId) {
		log.info("Deleting promotion with ID: {}", promotionId);

		Promotion promotion = findByIdOrThrow(promotionId);

		if (!promotion.getUserRedemptions().isEmpty()) {
			int redemptionCount = promotion.getUserRedemptions().size();
			throw new PromotionHasRedemptionsException(promotionId, redemptionCount);
		}

		String promotionTitle = promotion.getTitle();
		promotionRepository.delete(promotion);
		log.info("Promotion with ID: {} has been deleted", promotionId);

		auditService.logChange("Promotion", promotionId, AuditAction.DELETED, promotionTitle, null);
	}

	@Cacheable(key = "#promotionId")
	public PromotionResponse getPromotionById(Long promotionId) {
		Promotion promotion = findByIdOrThrow(promotionId);
		return promotionMapper.toPromotionResponse(promotion);
	}

	@Cacheable(key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
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