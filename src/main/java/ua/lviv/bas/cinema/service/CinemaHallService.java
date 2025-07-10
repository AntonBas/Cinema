package ua.lviv.bas.cinema.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.CinemaHallRepository;
import ua.lviv.bas.cinema.dao.SeatRepository;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;

@Service
@RequiredArgsConstructor
public class CinemaHallService {

	private static final Logger logger = LogManager.getLogger(CinemaHallService.class);

	private final CinemaHallRepository hallRepository;
	private final SeatRepository seatRepository;

	public CinemaHall createHall(CinemaHall hall) {
		logger.info("Creating CinemaHall: {}", hall.getName());
		return hallRepository.save(hall);
	}

	public CinemaHall readHall(Long id) {
		logger.info("Reading CinemaHall by id: {}", id);
		return hallRepository.findById(id).orElse(null);
	}

	@Transactional
	public CinemaHall updateHallWithSeats(Long id, String name, int rows, int seatsPerRow) {
		logger.info("Updating CinemaHall with id {}", id);

		CinemaHall hall = hallRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + id));

		hall.getSeats().clear();
		hall.setName(name);

		List<Seat> newSeats = generateSeats(hall, rows, seatsPerRow);

		hall.getSeats().addAll(newSeats);

		return hallRepository.save(hall);
	}

	public void deleteHall(Long id) {
		logger.info("Deleting CinemaHall by id: {}", id);
		hallRepository.deleteById(id);
	}

	public List<CinemaHall> getAllHalls() {
		logger.info("Retrieving all CinemaHalls");
		return hallRepository.findAll();
	}

	public CinemaHall createHallWithSeats(String name, int rows, int seatsPerRow) {
		logger.info("Creating hall '{}' with {} rows and {} seats per row", name, rows, seatsPerRow);

		CinemaHall hall = hallRepository.save(new CinemaHall(name));

		List<Seat> seats = generateSeats(hall, rows, seatsPerRow);
		seatRepository.saveAll(seats);

		return hall;
	}

	private List<Seat> generateSeats(CinemaHall hall, int rows, int seatsPerRow) {
		List<Seat> seats = new ArrayList<>();
		for (int row = 1; row <= rows; row++) {
			for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
				Seat seat = new Seat();
				seat.setHall(hall);
				seat.setRowNumber(row);
				seat.setSeatNumber(seatNum);
				seats.add(seat);
			}
		}
		return seats;
	}

}