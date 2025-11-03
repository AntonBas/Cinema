package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.Session;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

	List<Session> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

	List<Session> findByStartTimeAfter(LocalDateTime start);

	List<Session> findByHallId(Long hallId);

	List<Session> findByMovieId(Long movieId);

	@NonNull
	Page<Session> findAll(@NonNull Pageable pageable);

	@NonNull
	@Query("SELECT s FROM Session s WHERE LOWER(s.movie.title) LIKE LOWER(CONCAT('%', :title, '%'))")
	Page<Session> findByMovieTitleContainingIgnoreCase(@Param("title") String title, @NonNull Pageable pageable);

	@NonNull
	Page<Session> findByStartTimeBetween(@NonNull LocalDateTime start, @NonNull LocalDateTime end,
			@NonNull Pageable pageable);

	@NonNull
	Page<Session> findByStartTimeAfter(@NonNull LocalDateTime start, @NonNull Pageable pageable);

	@NonNull
	Page<Session> findByHallId(@NonNull Long hallId, @NonNull Pageable pageable);

	@NonNull
	Page<Session> findByMovieId(@NonNull Long movieId, @NonNull Pageable pageable);

	@Query("SELECT s FROM Session s WHERE s.hall.id = :hallId "
			+ "AND ((s.startTime < :endTime AND FUNCTION('TIMESTAMPADD', MINUTE, s.movie.durationMinutes, s.startTime) > :startTime) "
			+ "AND (s.id != :excludeSessionId OR :excludeSessionId IS NULL))")
	List<Session> findConflictingSessions(@Param("hallId") Long hallId, @Param("startTime") LocalDateTime startTime,
			@Param("endTime") LocalDateTime endTime, @Param("excludeSessionId") Long excludeSessionId);
}