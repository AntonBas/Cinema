package ua.lviv.bas.cinema.service.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;
import ua.lviv.bas.cinema.domain.projection.UserPromotionResponseProjection;
import ua.lviv.bas.cinema.dto.promotion.request.UserPromotionCreateRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.dto.promotion.response.UserPromotionResponse;
import ua.lviv.bas.cinema.exception.domain.promotion.AlreadyClaimedException;
import ua.lviv.bas.cinema.exception.domain.promotion.PromotionNotActiveException;
import ua.lviv.bas.cinema.mapper.PromotionMapper;
import ua.lviv.bas.cinema.repository.UserPromotionRepository;
import ua.lviv.bas.cinema.service.admin.AdminPromotionService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

	private final UserPromotionRepository userPromotionRepository;
	private final AdminPromotionService promotionService;
	private final BonusService bonusUserService;
	private final PromotionMapper promotionMapper;

	@Transactional
	public UserPromotionResponse claimPromotion(UserPromotionCreateRequest request, User user) {
		log.info("User {} claiming promotion {}", user.getEmail(), request.getPromotionId());

		Promotion promotion = promotionService.findByIdOrThrow(request.getPromotionId());

		if (!promotionService.isPromotionActive(promotion)) {
			throw new PromotionNotActiveException(promotion.getTitle());
		}

		if (userPromotionRepository.existsByUserAndPromotion(user, promotion)) {
			throw new AlreadyClaimedException(user.getEmail(), promotion.getTitle());
		}

		UserPromotion userPromotion = UserPromotion.builder().user(user).promotion(promotion)
				.redeemedAt(LocalDateTime.now()).pointsAwarded(promotion.getBonusPoints()).build();

		userPromotion = userPromotionRepository.save(userPromotion);
		Integer newBalance = bonusUserService.addPoints(user, promotion.getBonusPoints());

		log.info("Promotion claimed successfully. User received {} points", promotion.getBonusPoints());

		UserPromotionResponse response = promotionMapper.toUserPromotionResponse(userPromotion);
		response.setNewBalance(newBalance);
		return response;
	}

	public List<PromotionResponse> getAvailablePromotions(User user) {
		log.debug("Getting available promotions for user: {}", user.getEmail());
		List<PromotionResponse> activePromotions = promotionService.getActivePromotions();

		return activePromotions.stream().filter(promotion -> !hasUserClaimedPromotion(user, promotion.getId()))
				.collect(Collectors.toList());
	}

	public List<UserPromotionResponse> getUserPromotions(User user) {
		List<UserPromotionResponseProjection> projections = userPromotionRepository
				.findUserPromotionResponsesByUser(user);

		BonusCard bonusCard = user.getBonusCard();
		Integer currentBalance = bonusCard != null ? bonusCard.getPointsBalance() : 0;

		List<UserPromotionResponse> responses = promotionMapper.toUserPromotionResponseListFromProjections(projections);
		responses.forEach(r -> r.setNewBalance(currentBalance));
		return responses;
	}

	public boolean hasUserClaimedPromotion(User user, Long promotionId) {
		return userPromotionRepository.existsByUserAndPromotionId(user, promotionId);
	}
}