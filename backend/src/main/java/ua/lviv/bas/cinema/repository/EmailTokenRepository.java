package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.EmailToken;
import ua.lviv.bas.cinema.domain.enums.TokenType;

public interface EmailTokenRepository extends JpaRepository<EmailToken, Long> {

	Optional<EmailToken> findByToken(String token);

	Optional<EmailToken> findByUserEmailAndType(String email, TokenType type);

	List<EmailToken> findByUserEmail(String email);

}