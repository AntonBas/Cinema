package ua.lviv.bas.cinema.repository.promotion.projection;

import java.time.LocalDateTime;

public interface UserPromotionResponseProjection {
	Long getId();

	Long getPromotionId();

	String getPromotionTitle();

	LocalDateTime getClaimedAt();

	Integer getPointsAwarded();
}