package ua.lviv.bas.cinema.service.promotion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import ua.lviv.bas.cinema.domain.promotion.UserPromotion;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionUpdateRequest;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionAdminResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionAlreadyExistsException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionDatesInvalidException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionHasRedemptionsException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotFoundException;
import ua.lviv.bas.cinema.mapper.promotion.PromotionMapper;
import ua.lviv.bas.cinema.repository.promotion.PromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.UserPromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionAdminProjection;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "promotions")
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final UserPromotionRepository userPromotionRepository;
	private final PromotionMapper promotionMapper;
	private final BonusService bonusService;
	private final AuditService auditService;

	@CacheEvict(allEntries = true)
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

		Map<String, Object> details = new HashMap<>();
		details.put("title", promotion.getTitle());
		details.put("bonusPoints", promotion.getBonusPoints());

		auditService.logChange("Promotion", promotion.getId(), promotion.getTitle(), AuditAction.CREATED, null,
				details);

		return promotionMapper.toPromotionResponse(promotion);
	}

	@Cacheable(key = "#promotionId")
	public PromotionResponse getPromotionById(Long promotionId) {
		Promotion promotion = findByIdOrThrow(promotionId);
		return promotionMapper.toPromotionResponse(promotion);
	}

	@Caching(evict = { @CacheEvict(key = "#promotionId"), @CacheEvict(allEntries = true) })
	@Transactional
	public PromotionResponse updatePromotion(Long promotionId, PromotionUpdateRequest request) {
		log.info("Updating promotion with ID: {}", promotionId);

		Promotion oldPromotion = findByIdOrThrow(promotionId);
		Promotion promotion = findByIdOrThrow(promotionId);
		promotionMapper.updatePromotionFromRequest(promotion, request);

		promotion = promotionRepository.save(promotion);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("title", oldPromotion.getTitle());
		oldDetails.put("bonusPoints", oldPromotion.getBonusPoints());

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("title", promotion.getTitle());
		newDetails.put("bonusPoints", promotion.getBonusPoints());

		auditService.logChange("Promotion", promotionId, oldPromotion.getTitle(), AuditAction.UPDATED, oldDetails,
				newDetails);

		return promotionMapper.toPromotionResponse(promotion);
	}

	@Caching(evict = { @CacheEvict(key = "#promotionId"), @CacheEvict(allEntries = true) })
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

		Map<String, Object> details = new HashMap<>();
		details.put("deleted", promotionTitle);

		auditService.logChange("Promotion", promotionId, promotionTitle, AuditAction.DELETED, details, null);
	}

	@Cacheable(key = "'all-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public PageResponse<PromotionAdminResponse> getAllPromotions(Pageable pageable) {
		Page<PromotionAdminProjection> page = promotionRepository.findAllAdminList(pageable);
		return PageResponse.from(page.map(promotionMapper::toPromotionAdminResponse));
	}

	@Caching(evict = { @CacheEvict(key = "'available:' + #user.id"), @CacheEvict(allEntries = true) })
	@Transactional
	public PromotionResponse claimPromotion(UserPromotionCreateRequest request, User user) {
		log.info("User {} claiming promotion {}", user.getEmail(), request.promotionId());

		Promotion promotion = findByIdOrThrow(request.promotionId());

		if (!isPromotionActive(promotion)) {
			throw new PromotionNotActiveException(promotion.getTitle());
		}

		if (userPromotionRepository.existsByUserAndPromotion(user, promotion)) {
			throw new AlreadyClaimedException(user.getEmail(), promotion.getTitle());
		}

		UserPromotion userPromotion = UserPromotion.builder().user(user).promotion(promotion)
				.redeemedAt(LocalDateTime.now()).pointsAwarded(promotion.getBonusPoints()).build();

		userPromotionRepository.save(userPromotion);
		bonusService.addPoints(user, promotion.getBonusPoints(), promotion.getTitle());

		log.info("Promotion claimed successfully. User received {} points", promotion.getBonusPoints());

		Map<String, Object> details = new HashMap<>();
		details.put("userId", user.getId());
		details.put("userEmail", user.getEmail());
		details.put("promotionId", promotion.getId());
		details.put("promotionTitle", promotion.getTitle());
		details.put("pointsAwarded", promotion.getBonusPoints());

		auditService.logChange("Promotion", promotion.getId(), promotion.getTitle(), AuditAction.CLAIMED, null,
				details);

		return promotionMapper.toPromotionResponse(promotion);
	}

	public List<PromotionResponse> getClaimedPromotions(User user) {
		log.debug("Getting claimed promotions for user: {}", user.getEmail());
		return promotionRepository.findClaimedPromotionsByUser(user).stream().map(promotionMapper::toPromotionResponse)
				.collect(Collectors.toList());
	}

	public List<PromotionResponse> getAvailablePromotions(User user) {
		log.debug("Getting available promotions for user: {}", user != null ? user.getEmail() : "anonymous");

		List<PromotionResponseProjection> activePromotions = promotionRepository.findAllActivePromotions();

		return activePromotions.stream().map(promotionMapper::toPromotionResponse).collect(Collectors.toList());
	}

	public boolean hasUserClaimedPromotion(User user, Long promotionId) {
		return userPromotionRepository.existsByUserAndPromotionId(user, promotionId);
	}

	public Promotion findByIdOrThrow(Long promotionId) {
		return promotionRepository.findById(promotionId).orElseThrow(() -> new PromotionNotFoundException(promotionId));
	}

	private boolean isPromotionActive(Promotion promotion) {
		LocalDate now = LocalDate.now();
		LocalDate start = promotion.getStartDate();
		LocalDate end = promotion.getEndDate();

		boolean afterStart = (start == null) || !now.isBefore(start);
		boolean beforeEnd = (end == null) || !now.isAfter(end);

		return afterStart && beforeEnd;
	}

	private void validateDates(LocalDate startDate, LocalDate endDate) {
		if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
			throw new PromotionDatesInvalidException(startDate, endDate);
		}
	}
}