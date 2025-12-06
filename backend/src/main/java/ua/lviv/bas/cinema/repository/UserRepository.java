package ua.lviv.bas.cinema.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	long countByUserRole(UserRole userRole);
}
