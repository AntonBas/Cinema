package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TokenType;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String> {

	Optional<EmailToken> findByToken(String token);

	Optional<EmailToken> findByUserEmailAndType(String email, TokenType type);

	List<EmailToken> findByUserEmail(String email);

	@Modifying
	@Query("DELETE FROM EmailToken et WHERE et.user = :user AND et.type = :type AND et.expiresAt < :now")
	void deleteByUserAndTypeAndExpiresAtBefore(@Param("user") User user, @Param("type") TokenType type,
			@Param("now") LocalDateTime now);

	@Modifying
	@Query("DELETE FROM EmailToken et WHERE et.expiresAt < :now")
	void deleteAllExpired(@Param("now") LocalDateTime now);

	List<EmailToken> findByUserAndType(User user, TokenType type);

	void deleteByUserAndType(User user, TokenType type);

	@Modifying
	@Query("DELETE FROM EmailToken t WHERE t.expiresAt < :now")
	int deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

	@Modifying
	@Query("DELETE FROM EmailToken t WHERE t.confirmed = true AND t.confirmedAt < :date")
	int deleteByConfirmedTrueAndConfirmedAtBefore(@Param("date") LocalDateTime date);
}