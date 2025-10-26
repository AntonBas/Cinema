package ua.lviv.bas.cinema.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.CinemaHallDto;
import ua.lviv.bas.cinema.dto.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.CinemaHallWithSeatsDto;
import ua.lviv.bas.cinema.dto.HallLayoutDto;
import ua.lviv.bas.cinema.dto.SeatLayoutRequest;
import ua.lviv.bas.cinema.dto.SeatRowDto;
import ua.lviv.bas.cinema.exception.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.exception.DuplicateEntityException;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CinemaHallService {

	private final CinemaHallRepository hallRepository;
	private final SeatRepository seatRepository;
	private final CinemaHallMapper hallMapper;
	private final SeatMapper seatMapper;

	@Transactional
	public CinemaHallDto createHall(CinemaHallRequest request) {
		log.info("Creating cinema hall: {}", request.getName());

		if (hallRepository.existsByName(request.getName())) {
			throw new DuplicateEntityException("Cinema hall with name '" + request.getName() + "' already exists");
		}

		CinemaHall hall = CinemaHall.builder().name(request.getName()).build();

		CinemaHall saved = hallRepository.save(hall);
		log.debug("Cinema hall created with ID: {}", saved.getId());
		return hallMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public CinemaHallDto getHallById(long id) {
		log.debug("Retrieving cinema hall by id: {}", id);
		return hallRepository.findById(id).map(hallMapper::toDto)
				.orElseThrow(() -> new CinemaHallNotFoundException(id));
	}

	@Transactional
	public CinemaHallDto updateHall(Long id, CinemaHallRequest request) {
		log.info("Updating cinema hall with id: {}", id);

		CinemaHall existing = hallRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Cinema hall not found with id: " + id));

		if (!existing.getName().equals(request.getName()) && hallRepository.existsByName(request.getName())) {
			throw new DuplicateEntityException("Cinema hall with name '" + request.getName() + "' already exists");
		}

		existing.setName(request.getName());
		CinemaHall updated = hallRepository.save(existing);
		log.debug("Cinema hall updated with ID: {}", updated.getId());
		return hallMapper.toDto(updated);
	}

	@Transactional
	public void deleteHall(Long id) {
		log.info("Deleting cinema hall with id: {}", id);

		if (!hallRepository.existsById(id)) {
			throw new CinemaHallNotFoundException(id);
		}

		hallRepository.deleteById(id);
		log.debug("Cinema hall deleted with ID: {}", id);
	}

	@Transactional(readOnly = true)
	public List<CinemaHallDto> getAllHalls() {
		log.debug("Retrieving all cinema halls");
		return hallMapper.toDtoList(hallRepository.findAll());
	}

	@Transactional
	public CinemaHallWithSeatsDto generateSeats(Long hallId, SeatLayoutRequest request) {
		log.info("Generating seats for hall {}: {} rows, {} seats per row", hallId, request.getRows(),
				request.getSeatsPerRow());

		CinemaHall hall = hallRepository.findById(hallId)
				.orElseThrow(() -> new EntityNotFoundException("Cinema hall not found with id: " + hallId));

		seatRepository.deleteByHallId(hallId);

		List<Seat> seats = generateSeatLayout(hall, request);
		hall.setSeats(seats);

		CinemaHall saved = hallRepository.save(hall);
		log.debug("Generated {} seats for hall ID: {}", seats.size(), hallId);

		return buildHallWithSeatsDto(saved);
	}

	@Transactional(readOnly = true)
	public CinemaHallWithSeatsDto getHallWithSeats(Long hallId) {
		log.debug("Retrieving cinema hall with seats by id: {}", hallId);

		CinemaHall hall = hallRepository.findByIdWithSeats(hallId)
				.orElseThrow(() -> new EntityNotFoundException("Cinema hall not found with id: " + hallId));

		return buildHallWithSeatsDto(hall);
	}

	@Transactional
	public HallLayoutDto getHallLayout(Long hallId) {
		log.debug("Retrieving hall layout for id: {}", hallId);

		CinemaHall hall = hallRepository.findByIdWithSeats(hallId)
				.orElseThrow(() -> new EntityNotFoundException("Cinema hall not found with id: " + hallId));

		return buildHallLayoutDto(hall);
	}

	@Transactional(readOnly = true)
	public List<CinemaHallDto> searchHalls(String name) {
		log.debug("Searching cinema halls by name: {}", name);

		if (name == null || name.trim().isEmpty()) {
			return hallMapper.toDtoList(hallRepository.findAll());
		}

		return hallMapper.toDtoList(hallRepository.findByNameContainingIgnoreCase(name.trim()));
	}

	private List<Seat> generateSeatLayout(CinemaHall hall, SeatLayoutRequest request) {
		List<Seat> seats = new java.util.ArrayList<>();

		for (int row = 1; row <= request.getRows(); row++) {
			for (int number = 1; number <= request.getSeatsPerRow(); number++) {
				SeatType seatType = determineSeatType(request, row, number);

				Seat seat = Seat.builder().row(row).number(number).seatType(seatType).hall(hall).build();

				seats.add(seat);
			}
		}

		return seats;
	}

	private SeatType determineSeatType(SeatLayoutRequest request, int row, int number) {
		if (row > request.getRows() - 2) {
			return SeatType.VIP;
		}
		return request.getDefaultSeatType();
	}

	private CinemaHallWithSeatsDto buildHallWithSeatsDto(CinemaHall hall) {
		return CinemaHallWithSeatsDto.builder().id(hall.getId()).name(hall.getName()).capacity(hall.getCapacity())
				.seats(seatMapper.toDtoList(hall.getSeats())).build();
	}

	private HallLayoutDto buildHallLayoutDto(CinemaHall hall) {
		List<SeatRowDto> rows = hall.getSeats().stream().collect(Collectors.groupingBy(Seat::getRow)).entrySet()
				.stream()
				.map(entry -> SeatRowDto.builder().rowNumber(entry.getKey()).seatsCount(entry.getValue().size())
						.seats(seatMapper.toDtoList(entry.getValue())).build())
				.sorted(Comparator.comparing(SeatRowDto::getRowNumber)).collect(Collectors.toList());

		int totalRows = rows.size();
		int maxSeatsPerRow = rows.stream().mapToInt(SeatRowDto::getSeatsCount).max().orElse(0);

		return HallLayoutDto.builder().hallId(hall.getId()).hallName(hall.getName()).totalRows(totalRows)
				.maxSeatsPerRow(maxSeatsPerRow).totalSeats(hall.getCapacity()).rows(rows).build();
	}
}