package ua.lviv.bas.cinema.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.lviv.bas.cinema.domain.Session;

public interface SessionRepository extends JpaRepository<Session, Long> {

	List<Session> findByHallId(Long hallId);

	List<Session> findByMovieId(Long movieId);

	List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

	List<Session> findByStartTimeAfter(LocalDateTime start);

	@Query("SELECT s FROM Session s WHERE s.hall.id = :hallId "
			+ "AND ((s.startTime < :endTime AND FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) > :startTime) "
			+ "AND (s.id != :excludeSessionId OR :excludeSessionId IS NULL))")
	List<Session> findConflictingSessions(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);
}