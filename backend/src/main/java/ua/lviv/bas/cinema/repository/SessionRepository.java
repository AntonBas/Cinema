package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Repository
public interface SessionRepository
		extends JpaRepository<Session, Long>, QuerydslPredicateExecutor<Session>, JpaSpecificationExecutor<Session> {

	List<Session> findByStatusAndStartTimeBefore(CinemaSessionStatus status, LocalDateTime time);

	@Query("SELECT s FROM Session s WHERE s.status = :status AND "
			+ "(s.startTime + FUNCTION('MINUTE', s.movie.durationMinutes)) < :endTime")
	List<Session> findByStatusAndEndTimeBefore(@Param("status") CinemaSessionStatus status,
			@Param("endTime") LocalDateTime endTime);
}