package ua.lviv.bas.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.TicketType;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

}
