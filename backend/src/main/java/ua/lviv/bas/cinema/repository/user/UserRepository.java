package ua.lviv.bas.cinema.repository.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.repository.user.projection.AdminUserProjection;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	@EntityGraph(attributePaths = { "bonusCard" })
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	@EntityGraph(attributePaths = { "bonusCard" })
	@Override
	Optional<User> findById(Long id);

	@Query("""
			SELECT u FROM User u
			WHERE u.verificationStatus = :status
			  AND u.enabled = true
			  AND EXTRACT(DAY FROM u.dateOfBirth) = :day
			  AND EXTRACT(MONTH FROM u.dateOfBirth) = :month
			""")
	List<User> findVerifiedUsersWithBirthday(@Param("status") VerificationStatus status, @Param("day") int day,
			@Param("month") int month);

	long countByUserRoleAndEnabledTrue(UserRole role);

	@EntityGraph(attributePaths = { "bonusCard" })
	Optional<User> findWithBonusCardById(Long id);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.tickets WHERE u.id = :id")
	Optional<User> findWithTicketsById(@Param("id") Long id);

	@Query(value = """
			SELECT
			    u.id,
			    u.email,
			    u.first_name as firstName,
			    u.last_name as lastName,
			    u.user_role as userRole,
			    u.enabled,
			    u.verification_status as verificationStatus,
			    u.verified_at as verifiedAt,
			    COALESCE(t.ticketCount, 0) as ticketsCount,
			    u.last_modified_date as lastActivity
			FROM users u
			LEFT JOIN (
			    SELECT t.user_id, COUNT(t.id) as ticketCount
			    FROM tickets t
			    GROUP BY t.user_id
			) t ON t.user_id = u.id
			WHERE (:search IS NULL OR
			       u.email ILIKE CONCAT('%', CAST(:search AS text), '%') OR
			       u.first_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR
			       u.last_name ILIKE CONCAT('%', CAST(:search AS text), '%'))
			  AND (:role IS NULL OR u.user_role = CAST(:role AS text))
			  AND (:verificationStatus IS NULL OR u.verification_status = CAST(:verificationStatus AS text))
			  AND (:enabled IS NULL OR u.enabled = :enabled)
			""", countQuery = """
			SELECT COUNT(*)
			FROM users u
			WHERE (:search IS NULL OR
			       u.email ILIKE CONCAT('%', CAST(:search AS text), '%') OR
			       u.first_name ILIKE CONCAT('%', CAST(:search AS text), '%') OR
			       u.last_name ILIKE CONCAT('%', CAST(:search AS text), '%'))
			  AND (:role IS NULL OR u.user_role = CAST(:role AS text))
			  AND (:verificationStatus IS NULL OR u.verification_status = CAST(:verificationStatus AS text))
			  AND (:enabled IS NULL OR u.enabled = :enabled)
			""", nativeQuery = true)
	Page<AdminUserProjection> findProjectionsByFilters(@Param("search") String search, @Param("role") String role,
			@Param("verificationStatus") String verificationStatus, @Param("enabled") Boolean enabled,
			Pageable pageable);
}