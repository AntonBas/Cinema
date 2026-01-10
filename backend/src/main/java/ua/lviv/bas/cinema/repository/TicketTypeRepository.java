package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

	Optional<TicketType> findByCode(String code);

	boolean existsByCode(String code);

	List<TicketType> findByActiveTrue();

	List<TicketType> findByActiveFalse();
}