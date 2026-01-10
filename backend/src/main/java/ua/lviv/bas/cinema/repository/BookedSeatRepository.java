package ua.lviv.bas.cinema.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;

@Repository
public interface BookedSeatRepository extends JpaRepository<BookedSeat, Long> {

	boolean existsBySessionIdAndSeatIdAndStatusIn(Long sessionId, Long seatId, List<BookedSeatStatus> statuses);

	List<BookedSeat> findBySessionId(Long sessionId);

	List<BookedSeat> findBySessionIdAndStatusIn(Long sessionId, List<BookedSeatStatus> statuses);

	long countBySessionIdAndStatusIn(Long sessionId, List<BookedSeatStatus> of);
}