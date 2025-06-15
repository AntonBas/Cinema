package ua.lviv.bas.cinema.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.EmailToken;

public interface EmailTokenRepository extends JpaRepository<EmailToken, String> {

	Optional<EmailToken> findByUserEmail(String email);

	Optional<EmailToken> findByToken(String token);
//	void deleteByUserEmail(String email);
}
