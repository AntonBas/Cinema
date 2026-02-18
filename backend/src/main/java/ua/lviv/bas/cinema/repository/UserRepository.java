package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.domain.projection.AdminUserProjection;
import ua.lviv.bas.cinema.dto.user.request.UserFilterRequest;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	@EntityGraph(attributePaths = { "bonusCard" })
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	@EntityGraph(attributePaths = { "bonusCard" })
	@Override
	Optional<User> findById(Long id);

	@Query("SELECT u FROM User u WHERE u.verificationStatus = :status AND u.enabled = true AND FUNCTION('DAY', u.dateOfBirth) = :day AND FUNCTION('MONTH', u.dateOfBirth) = :month")
	List<User> findVerifiedUsersWithBirthday(@Param("status") VerificationStatus status, @Param("day") int day,
			@Param("month") int month);

	long countByUserRoleAndEnabledTrue(UserRole role);

	@EntityGraph(attributePaths = { "bonusCard" })
	Optional<User> findWithBonusCardById(Long id);

	@Query("SELECT u FROM User u LEFT JOIN FETCH u.tickets WHERE u.id = :id")
	Optional<User> findWithTicketsById(@Param("id") Long id);

	@Query("SELECT u FROM User u WHERE u.userRole = :role AND u.enabled = true")
	List<User> findActiveByRole(@Param("role") UserRole role);

	@Query(value = """
			SELECT
			    u.id AS id,
			    u.email AS email,
			    u.firstName AS firstName,
			    u.lastName AS lastName,
			    u.userRole AS userRole,
			    COALESCE(u.enabled, false) AS enabled,
			    u.verificationStatus AS verificationStatus,
			    u.verifiedAt AS verifiedAt,
			    COALESCE(t.ticketCount, 0) AS ticketsCount,
			    u.updatedAt AS lastActivity
			FROM User u
			LEFT JOIN (
			    SELECT t.user.id as userId, COUNT(t) as ticketCount
			    FROM Ticket t
			    GROUP BY t.user.id
			) t ON t.userId = u.id
			WHERE
			    (:#{#filter.search} IS NULL OR
			     u.email ILIKE CONCAT('%', :#{#filter.search}, '%') OR
			     u.firstName ILIKE CONCAT('%', :#{#filter.search}, '%') OR
			     u.lastName ILIKE CONCAT('%', :#{#filter.search}, '%'))
			    AND (:#{#filter.role} IS NULL OR u.userRole = :#{#filter.role})
			    AND (:#{#filter.verificationStatus} IS NULL OR u.verificationStatus = :#{#filter.verificationStatus})
			    AND (:#{#filter.enabled} IS NULL OR u.enabled = :#{#filter.enabled})
			""")
	Page<AdminUserProjection> findAdminUsers(@Param("filter") UserFilterRequest filter, Pageable pageable);
}