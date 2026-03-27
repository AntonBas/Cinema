package ua.lviv.bas.cinema.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.domain.projection.TicketTypeAdminProjection;
import ua.lviv.bas.cinema.domain.projection.TicketTypeUserProjection;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

	boolean existsByDisplayName(String displayName);

	@Query("SELECT t FROM TicketType t WHERE t.active = true ORDER BY CASE t.category WHEN 'STANDARD' THEN 0 ELSE 1 END, t.displayName ASC")
	List<TicketType> findByActiveTrue();

	@Query("SELECT t.id as id, t.displayName as displayName, t.priceMultiplier as priceMultiplier, "
			+ "t.minAge as minAge, t.maxAge as maxAge, t.requiresDocument as requiresDocument, "
			+ "t.documentType as documentType, t.active as active, t.category as category " + "FROM TicketType t WHERE "
			+ "(:active IS NULL OR t.active = :active) AND " + "(:category IS NULL OR t.category = :category) AND "
			+ "(:search IS NULL OR :search = '' OR LOWER(t.displayName) LIKE LOWER(CONCAT('%', :search, '%'))) "
			+ "ORDER BY CASE t.category WHEN 'STANDARD' THEN 0 ELSE 1 END, t.displayName ASC")
	Page<TicketTypeAdminProjection> findAdminProjections(@Param("active") Boolean active,
			@Param("category") TicketTypeCategory category, @Param("search") String search, Pageable pageable);

	@Query("SELECT t.id as id, t.displayName as displayName, t.priceMultiplier as priceMultiplier, "
			+ "t.requiresDocument as requiresDocument, t.documentType as documentType, " + "t.category as category "
			+ "FROM TicketType t WHERE t.active = true "
			+ "ORDER BY CASE t.category WHEN 'STANDARD' THEN 0 ELSE 1 END, t.displayName ASC")
	List<TicketTypeUserProjection> findUserProjections();

	@Query("SELECT COUNT(t) > 0 FROM TicketType t WHERE LOWER(t.displayName) = LOWER(:displayName) AND t.id != :id")
	boolean existsByDisplayNameAndIdNot(@Param("displayName") String displayName, @Param("id") Long id);
}