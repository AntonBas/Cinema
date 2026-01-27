package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
	Optional<TicketType> findByCode(String code);

	boolean existsByCode(String code);

	List<TicketType> findByActiveTrue();

	List<TicketType> findByActiveFalse();

	List<TicketType> findByCategory(TicketTypeCategory category);

	List<TicketType> findByActiveTrueAndCategory(TicketTypeCategory category);

	List<TicketType> findByActiveFalseAndCategory(TicketTypeCategory category);

	@Query("SELECT t FROM TicketType t WHERE " + "(:active IS NULL OR t.active = :active) AND "
			+ "(:category IS NULL OR t.category = :category) AND "
			+ "(LOWER(t.code) LIKE LOWER(CONCAT('%', :search, '%')) OR "
			+ "LOWER(t.displayName) LIKE LOWER(CONCAT('%', :search, '%')))")
	List<TicketType> findByFilters(@Param("active") Boolean active, @Param("category") TicketTypeCategory category,
			@Param("search") String search);

	@Query("SELECT t FROM TicketType t WHERE t.active = true ORDER BY t.displayOrder ASC")
	List<TicketType> findByActiveTrueOrderByDisplayOrderAsc();
}