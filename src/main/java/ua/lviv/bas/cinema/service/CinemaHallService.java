package ua.lviv.bas.cinema.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.CinemaHallRepository;
import ua.lviv.bas.cinema.domain.CinemaHall;

@Service
@RequiredArgsConstructor
public class CinemaHallService {

	private static final Logger logger = LogManager.getLogger(MovieService.class);

	private final CinemaHallRepository hallRepository;

	public CinemaHall createHall(CinemaHall hall) {
		logger.info("Creating CinemaHall: {}", hall.getName());
		return hallRepository.save(hall);
	}

	public CinemaHall readHall(Long id) {
		logger.info("Reading CinemaHall by id: {}", id);
		return hallRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Hall with id " + id + " not found"));
	}

	public CinemaHall updateHall(CinemaHall hall) {
		logger.info("Updating CinemaHall with id {}", hall.getId());
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
}
