package ua.lviv.bas.cinema.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	long countByUserRole(UserRole userRole);

	@Query("SELECT u FROM User u WHERE "
			+ "(:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
			+ "(:role IS NULL OR u.userRole = :role) AND " + "(:enabled IS NULL OR u.enabled = :enabled)")
	Page<User> findByFilters(@Param("search") String search, @Param("role") UserRole role,
			@Param("enabled") Boolean enabled, Pageable pageable);
}
