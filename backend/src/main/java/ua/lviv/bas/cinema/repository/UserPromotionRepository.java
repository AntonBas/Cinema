package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

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

	@Query("SELECT CASE WHEN COUNT(up) > 0 THEN true ELSE false END "
			+ "FROM UserPromotion up WHERE up.user = :user AND up.promotion.id = :promotionId")
	boolean existsByUserAndPromotionId(@Param("user") User user, @Param("promotionId") Long promotionId);

	@Query("SELECT up FROM UserPromotion up JOIN FETCH up.promotion WHERE up.user = :user")
	List<UserPromotion> findByUserWithPromotion(@Param("user") User user);

	long countByPromotion(Promotion promotion);

	List<UserPromotion> findByPromotion(Promotion promotion);

	Optional<UserPromotion> findByUserAndPromotion(User user, Promotion promotion);
}