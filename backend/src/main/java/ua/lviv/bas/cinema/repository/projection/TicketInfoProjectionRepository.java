package ua.lviv.bas.cinema.repository.projection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.projection.TicketInfoProjection;

@Repository
public interface TicketInfoProjectionRepository
		extends JpaRepository<TicketInfoProjection, Long>, JpaSpecificationExecutor<TicketInfoProjection> {
	Page<TicketInfoProjection> findAll(Specification<TicketInfoProjection> spec, Pageable pageable);
}