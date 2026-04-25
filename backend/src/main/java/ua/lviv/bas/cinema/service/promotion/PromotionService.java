package ua.lviv.bas.cinema.service.promotion;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.domain.promotion.UserPromotion;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.promotion.request.ClaimPromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.request.PromotionRequest;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionListResponse;
import ua.lviv.bas.cinema.dto.promotion.response.PromotionResponse;
import ua.lviv.bas.cinema.exception.domain.financial.promotion.*;
import ua.lviv.bas.cinema.mapper.promotion.PromotionMapper;
import ua.lviv.bas.cinema.repository.promotion.PromotionRepository;
import ua.lviv.bas.cinema.repository.promotion.UserPromotionRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final UserPromotionRepository userPromotionRepository;
    private final PromotionMapper promotionMapper;
    private final BonusService bonusService;
    private final AuditService auditService;

    @CacheEvict(value = "promotions", allEntries = true)
    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        log.info("Creating new promotion: {}", request.title());

        if (promotionRepository.existsByTitle(request.title())) {
            throw PromotionAlreadyExistsException.forTitle(request.title());
        }

        var promotion = promotionMapper.toPromotion(request);
        var saved = promotionRepository.save(promotion);

        log.info("Promotion created with ID: {}", saved.getId());
        auditCreate(saved);

        return promotionMapper.toPromotionResponse(saved);
    }

    @Cacheable(value = "promotions", key = "'list-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<PromotionListResponse> getPromotions(String query, Pageable pageable) {
        log.info("Getting promotions: query='{}', page={}, size={}", query, pageable.getPageNumber(),
                pageable.getPageSize());
        return promotionRepository.findAllAdminProjections(query, pageable)
                .map(promotionMapper::toPromotionListResponse);
    }

    public PromotionResponse getPromotion(Long id) {
        return promotionRepository.findById(id).map(promotionMapper::toPromotionResponse)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    public List<PromotionResponse> getAvailablePromotions(User user) {
        log.debug("Getting available promotions for user: {}", user != null ? user.getEmail() : "anonymous");
        return promotionRepository.findAllActivePromotions().stream().map(promotionMapper::toPromotionResponse)
                .toList();
    }

    public List<PromotionResponse> getClaimedPromotions(User user) {
        log.debug("Getting claimed promotions for user: {}", user.getEmail());
        return promotionRepository.findClaimedPromotionsByUser(user).stream().map(promotionMapper::toPromotionResponse)
                .toList();
    }

    @CacheEvict(value = "promotions", allEntries = true)
    @Transactional
    public PromotionResponse updatePromotion(Long id, PromotionRequest request) {
        log.info("Updating promotion with ID: {}", id);

        var promotion = findByIdOrThrow(id);
        String oldTitle = promotion.getTitle();

        promotionMapper.updatePromotionFromRequest(request, promotion);
        var updated = promotionRepository.save(promotion);

        log.info("Promotion updated with ID: {}", updated.getId());
        auditUpdate(id, oldTitle, updated);

        return promotionMapper.toPromotionResponse(updated);
    }

    @CacheEvict(value = "promotions", allEntries = true)
    @Transactional
    public void deletePromotion(Long id) {
        log.info("Deleting promotion with ID: {}", id);

        var promotion = findByIdOrThrow(id);

        if (!promotion.getUserRedemptions().isEmpty()) {
            int redemptionCount = promotion.getUserRedemptions().size();
            throw new PromotionHasRedemptionsException(id, redemptionCount);
        }

        String promotionTitle = promotion.getTitle();
        promotionRepository.delete(promotion);
        log.info("Promotion with ID: {} has been deleted", id);
        auditDelete(id, promotionTitle);
    }

    @CacheEvict(value = "promotions", allEntries = true)
    @Transactional
    public PromotionResponse claimPromotion(ClaimPromotionRequest request, User user) {
        log.info("User {} claiming promotion {}", user.getEmail(), request.promotionId());

        var promotion = findByIdOrThrow(request.promotionId());

        if (!isPromotionActive(promotion)) {
            throw new PromotionNotActiveException(promotion.getTitle());
        }

        if (userPromotionRepository.existsByUserAndPromotion(user, promotion)) {
            throw new AlreadyClaimedException(promotion.getTitle());
        }

        var userPromotion = UserPromotion.builder().user(user).promotion(promotion).redeemedAt(LocalDateTime.now())
                .pointsAwarded(promotion.getBonusPoints()).build();

        userPromotionRepository.save(userPromotion);
        bonusService.addPromotionPoints(user, promotion.getBonusPoints(), promotion.getTitle());

        log.info("Promotion claimed successfully. User received {} points", promotion.getBonusPoints());
        auditClaim(promotion, user);

        return promotionMapper.toPromotionResponse(promotion);
    }

    public boolean hasUserClaimedPromotion(User user, Long promotionId) {
        return userPromotionRepository.existsByUserAndPromotionId(user, promotionId);
    }

    private Promotion findByIdOrThrow(Long id) {
        return promotionRepository.findById(id).orElseThrow(() -> new PromotionNotFoundException(id));
    }

    private boolean isPromotionActive(Promotion promotion) {
        LocalDate now = LocalDate.now();
        LocalDate start = promotion.getStartDate();
        LocalDate end = promotion.getEndDate();

        boolean afterStart = (start == null) || !now.isBefore(start);
        boolean beforeEnd = (end == null) || !now.isAfter(end);

        return afterStart && beforeEnd;
    }

    private void auditCreate(Promotion promotion) {
        Map<String, Object> details = new HashMap<>();
        details.put("title", promotion.getTitle());
        details.put("bonusPoints", promotion.getBonusPoints());
        auditService.logChange("Promotion", promotion.getId(), promotion.getTitle(), AuditAction.CREATED, null,
                details);
    }

    private void auditUpdate(Long id, String oldTitle, Promotion updated) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("title", oldTitle);
        oldDetails.put("bonusPoints", updated.getBonusPoints());

        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("title", updated.getTitle());
        newDetails.put("bonusPoints", updated.getBonusPoints());

        auditService.logChange("Promotion", id, oldTitle, AuditAction.UPDATED, oldDetails, newDetails);
    }

    private void auditDelete(Long id, String title) {
        Map<String, Object> details = new HashMap<>();
        details.put("deleted", title);
        auditService.logChange("Promotion", id, title, AuditAction.DELETED, details, null);
    }

    private void auditClaim(Promotion promotion, User user) {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", user.getId());
        details.put("userEmail", user.getEmail());
        details.put("promotionId", promotion.getId());
        details.put("promotionTitle", promotion.getTitle());
        details.put("pointsAwarded", promotion.getBonusPoints());
        auditService.logChange("Promotion", promotion.getId(), promotion.getTitle(), AuditAction.CLAIMED, null,
                details);
    }
}