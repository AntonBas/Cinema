package ua.lviv.bas.cinema.repository.projection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.projection.SessionScheduleProjection;

@Repository
public interface SessionScheduleProjectionRepository
		extends JpaRepository<SessionScheduleProjection, Long>, JpaSpecificationExecutor<SessionScheduleProjection> {
}