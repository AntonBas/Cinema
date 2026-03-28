package ua.lviv.bas.cinema.domain.projection.promotion;

import java.time.LocalDateTime;

public interface UserPromotionResponseProjection {
	Long getId();

	Long getPromotionId();

	String getPromotionTitle();

	LocalDateTime getClaimedAt();

	Integer getPointsAwarded();
}