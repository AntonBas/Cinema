package ua.lviv.bas.cinema.service.promotion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.domain.promotion.UserPromotion;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.mapper.promotion.PromotionMapper;
import ua.lviv.bas.cinema.repository.promotion.PromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.UserPromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.projection.PromotionResponseProjection;
import ua.lviv.bas.cinema.service.bonus.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "promotions")
public class PromotionService {

	private final PromotionRepository promotionRepository;
	private final UserPromotionRepository userPromotionRepository;
	private final AdminPromotionService adminPromotionService;
	private final BonusService bonusUserService;
	private final PromotionMapper promotionMapper;

	@Caching(evict = { @CacheEvict(key = "'available:' + #user.id"),
			@CacheEvict(value = "promotions", allEntries = true) })
	@Transactional
	public PromotionResponse claimPromotion(UserPromotionCreateRequest request, User user) {
		log.info("User {} claiming promotion {}", user.getEmail(), request.promotionId());

		Promotion promotion = adminPromotionService.findByIdOrThrow(request.promotionId());

		if (!isPromotionActive(promotion)) {
			throw new PromotionNotActiveException(promotion.getTitle());
		}

		if (userPromotionRepository.existsByUserAndPromotion(user, promotion)) {
			throw new AlreadyClaimedException(user.getEmail(), promotion.getTitle());
		}

		UserPromotion userPromotion = UserPromotion.builder().user(user).promotion(promotion)
				.redeemedAt(LocalDateTime.now()).pointsAwarded(promotion.getBonusPoints()).build();

		userPromotionRepository.save(userPromotion);
		bonusUserService.addPoints(user, promotion.getBonusPoints(), promotion.getTitle());

		log.info("Promotion claimed successfully. User received {} points", promotion.getBonusPoints());

		return promotionMapper.toPromotionResponse(promotion);
	}

	@Cacheable(key = "'available:' + #user.id")
	public List<PromotionResponse> getAvailablePromotions(User user) {
		log.debug("Getting available promotions for user: {}", user.getEmail());

		List<PromotionResponseProjection> activePromotions = promotionRepository.findAllActivePromotions();

		return activePromotions.stream().filter(p -> !hasUserClaimedPromotion(user, p.getId()))
				.map(promotionMapper::toPromotionResponse).collect(Collectors.toList());
	}

	private boolean isPromotionActive(Promotion promotion) {
		LocalDate now = LocalDate.now();
		LocalDate start = promotion.getStartDate();
		LocalDate end = promotion.getEndDate();

		boolean afterStart = (start == null) || !now.isBefore(start);
		boolean beforeEnd = (end == null) || !now.isAfter(end);

		return afterStart && beforeEnd;
	}

	public boolean hasUserClaimedPromotion(User user, Long promotionId) {
		return userPromotionRepository.existsByUserAndPromotionId(user, promotionId);
	}
}