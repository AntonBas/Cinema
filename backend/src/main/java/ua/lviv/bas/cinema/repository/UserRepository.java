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

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query("SELECT COUNT(t) FROM Ticket t WHERE t.user.id = :userId")
	long countTicketsByUserId(@Param("userId") Long userId);

	@EntityGraph(attributePaths = { "tickets", "bonusCard" })
	@Override
	Page<User> findAll(Specification<User> spec, Pageable pageable);

	@EntityGraph(attributePaths = { "tickets", "bonusCard" })
	@Override
	Optional<User> findById(Long id);

	@Query("SELECT u FROM User u WHERE " + "u.verificationStatus = :status AND " + "u.enabled = true AND "
			+ "DAY(u.dateOfBirth) = :day AND " + "MONTH(u.dateOfBirth) = :month")
	List<User> findVerifiedUsersWithBirthday(@Param("status") VerificationStatus status, @Param("day") int day,
			@Param("month") int month);

	long countByUserRoleAndEnabledTrue(UserRole role);
}