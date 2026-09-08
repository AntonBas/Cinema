package ua.lviv.bas.cinema.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ua.lviv.bas.cinema.user.domain.EmailToken;
import ua.lviv.bas.cinema.user.domain.TokenType;
import ua.lviv.bas.cinema.user.domain.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String> {

    Optional<EmailToken> findByToken(String token);

    void deleteByUserAndType(User user, TokenType type);

    @Modifying
    @Query("DELETE FROM EmailToken t WHERE t.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM EmailToken t WHERE t.confirmed = true AND t.confirmedAt < :date")
    int deleteByConfirmedTrueAndConfirmedAtBefore(@Param("date") LocalDateTime date);
}