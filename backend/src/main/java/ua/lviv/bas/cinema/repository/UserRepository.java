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

	long countByUserRole(UserRole userRole);

	List<User> findByEnabledTrue();

	List<User> findByUserRoleAndEnabledTrue(UserRole role);

	long countByUserRoleAndEnabledTrue(UserRole role);

	@Query("SELECT u FROM User u WHERE " + "(:search IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
			+ "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
			+ "(:role IS NULL OR u.userRole = :role) AND " + "(:enabled IS NULL OR u.enabled = :enabled)")
	Page<User> findFilteredUsers(@Param("search") String search, @Param("role") UserRole role,
			@Param("enabled") Boolean enabled, Pageable pageable);

	@Query("SELECT u FROM User u WHERE " + "u.verificationStatus = :status AND " + "u.enabled = true AND "
			+ "FUNCTION('DAY', u.dateOfBirth) = :day AND " + "FUNCTION('MONTH', u.dateOfBirth) = :month")
	List<User> findVerifiedUsersWithBirthday(@Param("status") VerificationStatus status, @Param("day") int day,
			@Param("month") int month);
}