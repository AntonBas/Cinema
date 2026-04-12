package ua.lviv.bas.cinema.repository.ticket;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeProjection;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

	boolean existsByDisplayName(String displayName);

	boolean existsByDisplayNameAndIdNot(String displayName, Long id);

	@Query("SELECT t FROM TicketType t WHERE t.active = true ORDER BY CASE t.category WHEN 'STANDARD' THEN 0 ELSE 1 END, t.displayName ASC")
	List<TicketType> findByActiveTrue();

	@Query("""
			SELECT t.id as id,
			       t.displayName as displayName,
			       t.priceMultiplier as priceMultiplier,
			       t.minAge as minAge,
			       t.maxAge as maxAge,
			       t.requiresDocument as requiresDocument,
			       t.documentType as documentType,
			       t.active as active,
			       t.category as category
			FROM TicketType t
			WHERE (:active IS NULL OR t.active = :active)
			  AND (:category IS NULL OR t.category = :category)
			  AND (:query IS NULL OR :query = '' OR LOWER(t.displayName) LIKE LOWER(CONCAT('%', :query, '%')))
			ORDER BY CASE t.category WHEN 'STANDARD' THEN 0 ELSE 1 END, t.displayName ASC
			""")
	Page<TicketTypeProjection> findProjectionsByFilters(@Param("active") Boolean active,
			@Param("category") TicketTypeCategory category, @Param("query") String query, Pageable pageable);
}