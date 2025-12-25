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

	@Query(value = "SELECT s.* FROM sessions s JOIN movies m ON s.movie_id = m.id WHERE s.status = CAST(:status AS text) AND (s.start_time + (m.duration_minutes * interval '1 minute')) < :endTime", nativeQuery = true)
	List<Session> findByStatusAndEndTimeBefore(@Param("status") String status, @Param("endTime") LocalDateTime endTime);
}