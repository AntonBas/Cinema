package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId")
	long countTicketsByUserId(@Param("userId") Long userId);

	@Override
	Page<User> findAll(Specification<User> spec, Pageable pageable);

	@EntityGraph(attributePaths = { "tickets", "bonusCard" })
	@Override
	Optional<User> findById(Long id);

	@Query("SELECT u FROM User u WHERE u.verificationStatus = :status AND u.enabled = true AND DAY(u.dateOfBirth) = :day AND MONTH(u.dateOfBirth) = :month")
	List<User> findVerifiedUsersWithBirthday(@Param("status") VerificationStatus status, @Param("day") int day,
			@Param("month") int month);

	long countByUserRoleAndEnabledTrue(UserRole role);

	@EntityGraph(attributePaths = { "bonusCard" })
	Optional<User> findWithBonusCardById(Long id);

	@Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.tickets WHERE u.id = :id")
	Optional<User> findWithTicketsById(@Param("id") Long id);

	@Query("SELECT u FROM User u WHERE u.userRole = :role AND u.enabled = true")
	List<User> findActiveByRole(@Param("role") UserRole role);

	@Query("""
			SELECT
			    u.id as id,
			    u.email as email,
			    u.firstName as firstName,
			    u.lastName as lastName,
			    u.userRole as userRole,
			    u.enabled as enabled,
			    u.verificationStatus as verificationStatus,
			    u.verifiedAt as verifiedAt,
			    u.createdAt as createdAt,
			    u.updatedAt as updatedAt,
			    (SELECT COUNT(t) FROM Ticket t WHERE t.user.id = u.id) as ticketsCount,
			    u.updatedAt as lastActivity
			FROM User u
			WHERE
			    (:#{#filter.search} IS NULL OR
			     LOWER(u.email) LIKE LOWER(CONCAT('%', :#{#filter.search}, '%')) OR
			     LOWER(u.firstName) LIKE LOWER(CONCAT('%', :#{#filter.search}, '%')) OR
			     LOWER(u.lastName) LIKE LOWER(CONCAT('%', :#{#filter.search}, '%')))
			    AND (:#{#filter.role} IS NULL OR u.userRole = :#{#filter.role})
			    AND (:#{#filter.verificationStatus} IS NULL OR u.verificationStatus = :#{#filter.verificationStatus})
			    AND (:#{#filter.enabled} IS NULL OR u.enabled = :#{#filter.enabled})
			""")
	Page<AdminUserProjection> findAdminUsers(@Param("filter") UserFilterRequest filter, Pageable pageable);
}