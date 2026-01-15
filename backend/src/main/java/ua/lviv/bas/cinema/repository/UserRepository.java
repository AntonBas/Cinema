package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	List<User> findByEnabledTrue();

	List<User> findByUserRoleAndEnabledTrue(UserRole role);

	long countByUserRoleAndEnabledTrue(UserRole role);

	@Query(value = """
			SELECT u.* FROM users u
			LEFT JOIN bonus_cards bc ON u.id = bc.user_id
			WHERE (:search IS NULL OR
			       LOWER(u.email) LIKE '%' || LOWER(COALESCE(:search, '')) || '%' OR
			       LOWER(u.first_name) LIKE '%' || LOWER(COALESCE(:search, '')) || '%' OR
			       LOWER(u.last_name) LIKE '%' || LOWER(COALESCE(:search, '')) || '%')
			AND (:role IS NULL OR u.user_role = :role)
			AND (:enabled IS NULL OR u.enabled = :enabled)
			""", countQuery = """
			SELECT COUNT(*) FROM users u
			WHERE (:search IS NULL OR
			       LOWER(u.email) LIKE '%' || LOWER(COALESCE(:search, '')) || '%' OR
			       LOWER(u.first_name) LIKE '%' || LOWER(COALESCE(:search, '')) || '%' OR
			       LOWER(u.last_name) LIKE '%' || LOWER(COALESCE(:search, '')) || '%')
			AND (:role IS NULL OR u.user_role = :role)
			AND (:enabled IS NULL OR u.enabled = :enabled)
			""", nativeQuery = true)
	Page<User> findFilteredUsers(@Param("search") String search, @Param("role") String role,
			@Param("enabled") Boolean enabled, Pageable pageable);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.bonusCard WHERE " + "u.verificationStatus = :status AND "
			+ "u.enabled = true AND " + "DAY(u.dateOfBirth) = :day AND " + "MONTH(u.dateOfBirth) = :month")
	List<User> findVerifiedUsersWithBirthday(@Param("status") VerificationStatus status, @Param("day") int day,
			@Param("month") int month);
}