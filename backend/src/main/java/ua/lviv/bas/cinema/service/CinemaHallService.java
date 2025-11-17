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
import ua.lviv.bas.cinema.dto.cinemaHall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallWithSeatsResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatRowResponse;
import ua.lviv.bas.cinema.exception.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.exception.DuplicateEntityException;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.CinemaHallRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class CinemaHallService {

	private final CinemaHallRepository hallRepository;
	private final CinemaHallMapper hallMapper;
	private final SeatMapper seatMapper;

	@Transactional
	public CinemaHallResponse createHall(CinemaHallRequest request) {
		log.info("Creating cinema hall: {}", request.getName());

		if (hallRepository.existsByName(request.getName())) {
			throw new DuplicateEntityException("Cinema hall with name '" + request.getName() + "' already exists");
		}

		CinemaHall hall = CinemaHall.builder().name(request.getName()).build();

		if (request.getRows() != null && request.getSeatsPerRow() != null) {
			List<Seat> seats = generateSeatLayout(hall, request);
			hall.setSeats(seats);
			log.debug("Generated {} seats during hall creation", seats.size());
		}

		CinemaHall saved = hallRepository.save(hall);
		log.debug("Cinema hall created with ID: {}", saved.getId());
		return hallMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public CinemaHallResponse getHallById(long id) {
		log.debug("Retrieving cinema hall by id: {}", id);
		return hallRepository.findById(id).map(hallMapper::toDto)
				.orElseThrow(() -> new CinemaHallNotFoundException(id));
	}

	@Transactional
	public CinemaHallResponse updateHall(Long id, CinemaHallRequest request) {
		log.info("Updating cinema hall with id: {}", id);

		CinemaHall existing = hallRepository.findByIdWithSeats(id)
				.orElseThrow(() -> new CinemaHallNotFoundException("Cinema hall not found with id: " + id));

		if (!existing.getName().equals(request.getName()) && hallRepository.existsByName(request.getName())) {
			throw new DuplicateEntityException("Cinema hall with name '" + request.getName() + "' already exists");
		}

		existing.setName(request.getName());

		if (request.getRows() != null && request.getSeatsPerRow() != null) {
			log.info("Updating seat layout: {} rows, {} seats per row", request.getRows(), request.getSeatsPerRow());

			existing.getSeats().clear();
			List<Seat> newSeats = generateSeatLayout(existing, request);
			existing.getSeats().addAll(newSeats);
		}

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
	public List<CinemaHallResponse> getAllHalls() {
		log.debug("Retrieving all cinema halls");
		return hallMapper.toDtoList(hallRepository.findAll());
	}

	@Transactional(readOnly = true)
	public CinemaHallWithSeatsResponse getHallWithSeats(Long hallId) {
		log.debug("Retrieving cinema hall with seats by id: {}", hallId);

		CinemaHall hall = hallRepository.findByIdWithSeats(hallId)
				.orElseThrow(() -> new EntityNotFoundException("Cinema hall not found with id: " + hallId));

		return buildHallWithSeatsDto(hall);
	}

	@Transactional
	public HallLayoutResponse getHallLayout(Long hallId) {
		log.debug("Retrieving hall layout for id: {}", hallId);

		CinemaHall hall = hallRepository.findByIdWithSeats(hallId)
				.orElseThrow(() -> new EntityNotFoundException("Cinema hall not found with id: " + hallId));

		return buildHallLayoutDto(hall);
	}

	@Transactional(readOnly = true)
	public List<CinemaHallResponse> searchHalls(String name) {
		log.debug("Searching cinema halls by name: {}", name);

		if (name == null || name.trim().isEmpty()) {
			return hallMapper.toDtoList(hallRepository.findAll());
		}

		return hallMapper.toDtoList(hallRepository.findByNameContainingIgnoreCase(name.trim()));
	}

	@Transactional(readOnly = true)
	public CinemaHall getHallEntityById(Long id) {
		log.debug("Retrieving cinema hall entity by id: {}", id);
		return hallRepository.findById(id).orElseThrow(() -> new CinemaHallNotFoundException(id));
	}

	private List<Seat> generateSeatLayout(CinemaHall hall, CinemaHallRequest request) {
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

	private SeatType determineSeatType(CinemaHallRequest request, int row, int number) {
		if (row > request.getRows() - 2) {
			return SeatType.VIP;
		}
		return request.getDefaultSeatType();
	}

	private CinemaHallWithSeatsResponse buildHallWithSeatsDto(CinemaHall hall) {
		return CinemaHallWithSeatsResponse.builder().id(hall.getId()).name(hall.getName()).capacity(hall.getCapacity())
				.seats(seatMapper.toDtoList(hall.getSeats())).build();
	}

	private HallLayoutResponse buildHallLayoutDto(CinemaHall hall) {
		List<SeatRowResponse> rows = hall.getSeats().stream().collect(Collectors.groupingBy(Seat::getRow)).entrySet()
				.stream()
				.map(entry -> SeatRowResponse.builder().rowNumber(entry.getKey()).seatsCount(entry.getValue().size())
						.seats(seatMapper.toDtoList(entry.getValue())).build())
				.sorted(Comparator.comparing(SeatRowResponse::getRowNumber)).collect(Collectors.toList());

		int totalRows = rows.size();
		int maxSeatsPerRow = rows.stream().mapToInt(SeatRowResponse::getSeatsCount).max().orElse(0);

		return HallLayoutResponse.builder().hallId(hall.getId()).hallName(hall.getName()).totalRows(totalRows)
				.maxSeatsPerRow(maxSeatsPerRow).totalSeats(hall.getCapacity()).rows(rows).build();
	}
}