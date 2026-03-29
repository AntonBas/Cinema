package ua.lviv.bas.cinema.repository.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.promotion.Promotion;
import ua.lviv.bas.cinema.domain.promotion.UserPromotion;
import ua.lviv.bas.cinema.domain.user.User;

@Repository
public interface UserPromotionRepository extends JpaRepository<UserPromotion, Long> {
	boolean existsByUserAndPromotion(User user, Promotion promotion);

	@Query("SELECT COUNT(up) > 0 FROM UserPromotion up WHERE up.user = :user AND up.promotion.id = :promotionId")
	boolean existsByUserAndPromotionId(@Param("user") User user, @Param("promotionId") Long promotionId);
}