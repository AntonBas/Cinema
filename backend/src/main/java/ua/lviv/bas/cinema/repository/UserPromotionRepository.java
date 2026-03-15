package ua.lviv.bas.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Promotion;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.UserPromotion;

@Repository
public interface UserPromotionRepository extends JpaRepository<UserPromotion, Long> {
	boolean existsByUserAndPromotion(User user, Promotion promotion);

	@Query("SELECT COUNT(up) > 0 FROM UserPromotion up WHERE up.user = :user AND up.promotion.id = :promotionId")
	boolean existsByUserAndPromotionId(@Param("user") User user, @Param("promotionId") Long promotionId);
}