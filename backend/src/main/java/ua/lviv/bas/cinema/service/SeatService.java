package ua.lviv.bas.cinema.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.SeatRepository;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;

@Service
@RequiredArgsConstructor
public class SeatService {

	private static final Logger logger = LogManager.getLogger(SeatService.class);

	private final SeatRepository seatRepository;

	public List<Seat> getSeatsByHall(CinemaHall hall) {
		logger.info("Retrieving seats for CinemaHall with id: {}", hall.getId());
		return seatRepository.findByHall(hall);
	}

	public Seat createSeat(Seat seat) {
		logger.info("Creating seat: Row {}, Number {} in Hall ID {}", seat.getRowNumber(), seat.getSeatNumber(),
				seat.getHall() != null ? seat.getHall().getId() : null);
		return seatRepository.save(seat);
	}

	public Seat readSeat(Long id) {
		logger.info("Reading seat with id: {}", id);
		return seatRepository.findById(id).orElse(null);
	}

	public Seat updateSeat(Seat seat) {
		logger.info("Updating seat with id: {}", seat.getId());
		return seatRepository.save(seat);
	}

	public void deleteSeat(Long id) {
		logger.info("Deleting seat with id: {}", id);
		seatRepository.deleteById(id);
	}

}
