package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

	Optional<TicketType> findByCode(String code);

	boolean existsByCode(String code);

	List<TicketType> findByActiveTrue();

	List<TicketType> findByActiveFalse();

	List<TicketType> findByActiveTrueAndCategory(String category);

	List<TicketType> findByActiveFalseAndCategory(String category);

	List<TicketType> findByCategory(String category);

	@Query("SELECT tt FROM TicketType tt WHERE " + "(:active IS NULL OR tt.active = :active) AND "
			+ "(:category IS NULL OR tt.category = :category) AND "
			+ "(LOWER(tt.code) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(tt.displayName) LIKE LOWER(CONCAT('%', :search, '%')))")
	List<TicketType> findByFilters(@Param("active") Boolean active, @Param("category") String category,
			@Param("search") String search);
}