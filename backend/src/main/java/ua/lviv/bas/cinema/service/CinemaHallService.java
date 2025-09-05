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
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.CinemaHallCreateDto;
import ua.lviv.bas.cinema.dto.CinemaHallResponseDto;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;

@Service
@RequiredArgsConstructor
public class CinemaHallService {

	private static final Logger logger = LogManager.getLogger(CinemaHallService.class);

	private final CinemaHallRepository hallRepository;
	private final SeatRepository seatRepository;
	private final CinemaHallMapper cinemaHallMapper;

	@Transactional
	public CinemaHallResponseDto createHall(CinemaHallCreateDto createDto) {
		logger.info("Creating CinemaHall: {} with {} rows and {} seats per row", createDto.getName(),
				createDto.getRows(), createDto.getSeatsPerRow());

		CinemaHall hall = cinemaHallMapper.toEntity(createDto);

		List<Seat> seats = generateSeats(createDto, hall);
		hall.setSeats(seats);

		CinemaHall savedHall = hallRepository.save(hall);
		seatRepository.saveAll(seats);

		logger.info("CinemaHall created successfully with ID: {}", savedHall.getId());

		return cinemaHallMapper.toResponseDto(savedHall);
	}

	@Transactional(readOnly = true)
	public CinemaHallResponseDto getHallById(Long id) {
		logger.info("Reading CinemaHall by id: {}", id);

		CinemaHall hall = hallRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + id));
		return cinemaHallMapper.toResponseDto(hall);
	}

	@Transactional
	public CinemaHallResponseDto updateHall(Long id, CinemaHallCreateDto updateDto) {
		logger.info("Updating CinemaHall with id {}", id);

		CinemaHall hall = hallRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + id));

		hall.setName(updateDto.getName());

		seatRepository.deleteAll(hall.getSeats());
		hall.getSeats().clear();

		List<Seat> newSeats = generateSeats(updateDto, hall);
		hall.getSeats().addAll(newSeats);

		CinemaHall updatedHall = hallRepository.save(hall);
		seatRepository.saveAll(newSeats);

		logger.info("CinemaHall updated successfully with ID: {}", updatedHall.getId());
		return cinemaHallMapper.toResponseDto(updatedHall);

	}

	@Transactional
	public void deleteHall(Long id) {
		logger.info("Deleting CinemaHall by id: {}", id);
		CinemaHall hall = hallRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + id));

		hallRepository.delete(hall);
		logger.info("CinemaHall deleted successfully with ID: {}", id);
	}

	@Transactional(readOnly = true)
	public List<CinemaHallResponseDto> getAllHalls() {
		logger.info("Retrieving all CinemaHalls");

		List<CinemaHall> halls = hallRepository.findAll();
		return halls.stream().map(cinemaHallMapper::toResponseDto).toList();
	}

	private List<Seat> generateSeats(CinemaHallCreateDto dto, CinemaHall hall) {
		List<Seat> seats = new ArrayList<>();
		SeatType seatType = dto.getDefaultSeatType() != null ? dto.getDefaultSeatType() : SeatType.STANDARD;

		for (int row = 1; row <= dto.getRows(); row++) {
			for (int number = 1; number <= dto.getSeatsPerRow(); number++) {
				Seat seat = Seat.builder().row(row).number(number).seatType(seatType).hall(hall).build();
				seats.add(seat);
			}
		}
		return seats;
	}

	@Transactional(readOnly = true)
	public boolean existsById(Long id) {
		return hallRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public CinemaHall getEntityById(Long id) {
		return hallRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + id));
	}
}