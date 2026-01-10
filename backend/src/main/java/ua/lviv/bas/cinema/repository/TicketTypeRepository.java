package ua.lviv.bas.cinema.repository;

import java.math.BigDecimal;
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

	List<TicketType> findByCategoryAndActiveTrue(TicketTypeCategory category);

	List<TicketType> findByPriceMultiplierBetween(BigDecimal minMultiplier, BigDecimal maxMultiplier);

	List<TicketType> findByMinAgeIsNullAndMaxAgeIsNull();

	@Query("SELECT tt FROM TicketType tt WHERE " + "(:age IS NULL AND tt.minAge IS NULL AND tt.maxAge IS NULL) OR "
			+ "(:age >= tt.minAge OR tt.minAge IS NULL) AND " + "(:age <= tt.maxAge OR tt.maxAge IS NULL)")
	List<TicketType> findAvailableForAge(@Param("age") Integer age);

	List<TicketType> findByRequiresDocumentTrue();

	List<TicketType> findByRequiresDocumentTrueAndActiveTrue();

	long countByActiveTrue();

	long countByActiveFalse();

	@Query("SELECT COUNT(tt) > 0 FROM TicketType tt WHERE tt.code = :code AND tt.id <> :id")
	boolean existsByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

	List<TicketType> findByDisplayNameContainingIgnoreCase(String displayName);

	List<TicketType> findByDisplayNameContainingIgnoreCaseAndActiveTrue(String displayName);
}