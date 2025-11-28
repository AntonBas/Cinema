package ua.lviv.bas.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	long countByUserRole(UserRole userRole);
}
