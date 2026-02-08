package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDateTime;

public interface UserPromotionResponseProjection {
	Long getId();

	Long getPromotionId();

	String getPromotionTitle();

	LocalDateTime getClaimedAt();

	Integer getPointsAwarded();
}