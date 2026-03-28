package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.domain.projection.cinema.CinemaHallProjection;
import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.dto.hall.response.SeatRowResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallHasSessionsException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.validation.CoupleRowSeatsValidator;

@Slf4j
@Service
@RequiredArgsConstructor
public class CinemaHallService {

	private final CinemaHallRepository hallRepository;
	private final SeatRepository seatRepository;
	private final CinemaHallMapper hallMapper;
	private final SeatMapper seatMapper;

	@Transactional
	@CacheEvict(value = "cinemaHalls", allEntries = true)
	public CinemaHallResponse createHall(CinemaHallRequest request) {
		log.info("Creating cinema hall: {}", request.name());

		try {
			CoupleRowSeatsValidator.setCurrentRequest(request);

			if (hallRepository.existsByName(request.name())) {
				throw new DuplicateEntityException("CinemaHall", request.name());
			}

			validateCoupleRowsConfiguration(request);

			CinemaHall hall = CinemaHall.builder().name(request.name()).build();

			if (request.rows() != null && request.seatsPerRow() != null) {
				List<Seat> seats = generateSeatLayout(hall, request);
				hall.setSeats(seats);
				log.debug("Generated {} seats during hall creation", seats.size());
			}

			CinemaHall saved = hallRepository.save(hall);
			log.debug("Cinema hall created with ID: {}", saved.getId());
			return hallMapper.toCinemaHallResponse(saved);
		} finally {
			CoupleRowSeatsValidator.clear();
		}
	}

	@Transactional
	@CacheEvict(value = "cinemaHalls", allEntries = true)
	public CinemaHallResponse updateHall(Long id, CinemaHallRequest request) {
		log.info("Updating cinema hall with id: {}", id);

		try {
			CoupleRowSeatsValidator.setCurrentRequest(request);

			CinemaHall existing = hallRepository.findByIdWithSeats(id)
					.orElseThrow(() -> new CinemaHallNotFoundException(id));

			boolean hasFutureSessions = existing.getSessions().stream()
					.anyMatch(session -> session.getStartTime().isAfter(LocalDateTime.now()));

			if (hasFutureSessions) {
				throw new CinemaHallHasSessionsException(existing.getName(), id);
			}

			if (!existing.getName().equals(request.name()) && hallRepository.existsByName(request.name())) {
				throw new DuplicateEntityException("CinemaHall", request.name());
			}

			validateCoupleRowsConfiguration(request);

			existing.setName(request.name());

			boolean layoutChanged = !isLayoutSame(existing, request);

			if (layoutChanged) {
				boolean hasTickets = seatRepository.hasTicketsForHall(id);

				if (hasTickets) {
					throw new IllegalStateException("Cannot update hall layout because seats have booked tickets");
				}

				seatRepository.deleteByHallId(id);
				List<Seat> newSeats = generateSeatLayout(existing, request);
				seatRepository.saveAll(newSeats);
			}

			CinemaHall updated = hallRepository.save(existing);
			log.debug("Cinema hall updated with ID: {}", updated.getId());
			return hallMapper.toCinemaHallResponse(updated);
		} finally {
			CoupleRowSeatsValidator.clear();
		}
	}

	@Transactional
	@CacheEvict(value = "cinemaHalls", allEntries = true)
	public void deleteHall(Long id) {
		log.info("Deleting cinema hall with id: {}", id);

		CinemaHall hall = hallRepository.findByIdWithSeats(id).orElseThrow(() -> new CinemaHallNotFoundException(id));

		boolean hasFutureSessions = hall.getSessions().stream()
				.anyMatch(session -> session.getStartTime().isAfter(LocalDateTime.now()));

		if (hasFutureSessions) {
			throw new CinemaHallHasSessionsException(hall.getName(), id);
		}

		hallRepository.delete(hall);
		log.debug("Cinema hall deleted with ID: {}", id);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "cinemaHalls", key = "#id")
	public CinemaHallResponse getHallById(long id) {
		log.debug("Retrieving cinema hall by id: {}", id);
		return hallRepository.findByIdWithSeats(id).map(hallMapper::toCinemaHallResponse)
				.orElseThrow(() -> new CinemaHallNotFoundException(id));
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "cinemaHalls", key = "'all'")
	public List<CinemaHallResponse> getAllHalls() {
		log.debug("Retrieving all cinema halls");
		List<CinemaHallProjection> projections = hallRepository.findAllProjected();
		return hallMapper.toCinemaHallResponseListFromProjection(projections);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "cinemaHalls", key = "'layout:' + #hallId")
	public HallLayoutResponse getHallLayout(Long hallId) {
		log.debug("Retrieving hall layout for id: {}", hallId);

		CinemaHall hall = hallRepository.findByIdWithSeats(hallId)
				.orElseThrow(() -> new CinemaHallNotFoundException(hallId));

		return buildHallLayoutDto(hall);
	}

	@Transactional(readOnly = true)
	public CinemaHall getHallEntityById(Long id) {
		log.debug("Retrieving cinema hall entity by id: {}", id);
		return hallRepository.findById(id).orElseThrow(() -> new CinemaHallNotFoundException(id));
	}

	private List<Seat> generateSeatLayout(CinemaHall hall, CinemaHallRequest request) {
		List<Seat> seats = new ArrayList<>();
		SeatType defaultType = request.defaultSeatType();
		List<Integer> coupleRows = request.coupleRows() != null ? request.coupleRows() : List.of(7);

		for (int row = 1; row <= request.rows(); row++) {
			if (coupleRows.contains(row)) {
				int coupleSeatNumber = 1;
				for (int pos = 1; pos <= request.seatsPerRow(); pos += 2) {
					Seat seat = Seat.builder().row(row).number(coupleSeatNumber++).seatType(SeatType.COUPLE).hall(hall)
							.build();
					seats.add(seat);
				}
			} else {
				for (int number = 1; number <= request.seatsPerRow(); number++) {
					SeatType seatType = row > request.rows() - 2 ? SeatType.VIP : defaultType;
					Seat seat = Seat.builder().row(row).number(number).seatType(seatType).hall(hall).build();
					seats.add(seat);
				}
			}
		}
		return seats;
	}

	private boolean isLayoutSame(CinemaHall hall, CinemaHallRequest request) {
		if (hall.getSeats().isEmpty()) {
			return false;
		}

		Map<Integer, List<Seat>> seatsByRow = hall.getSeats().stream().collect(Collectors.groupingBy(Seat::getRow));

		int currentRows = seatsByRow.size();

		long standardSeatsInFirstRow = seatsByRow.getOrDefault(1, List.of()).stream()
				.filter(seat -> seat.getSeatType() != SeatType.COUPLE).count();

		return currentRows == request.rows() && standardSeatsInFirstRow == request.seatsPerRow();
	}

	private HallLayoutResponse buildHallLayoutDto(CinemaHall hall) {
		List<SeatRowResponse> rows = hall.getSeats().stream().collect(Collectors.groupingBy(Seat::getRow)).entrySet()
				.stream().map(entry -> {
					List<SeatResponse> seatResponses = seatMapper.toSeatResponseList(entry.getValue());
					return new SeatRowResponse(entry.getKey(), entry.getValue().size(), seatResponses);
				}).sorted(Comparator.comparing(SeatRowResponse::rowNumber)).collect(Collectors.toList());

		int totalRows = rows.size();
		int maxSeatsPerRow = rows.stream().mapToInt(SeatRowResponse::seatsCount).max().orElse(0);

		return new HallLayoutResponse(hall.getId(), hall.getName(), totalRows, maxSeatsPerRow, hall.getSeats().size(),
				rows);
	}

	private void validateCoupleRowsConfiguration(CinemaHallRequest request) {
		if (request.coupleRows() != null && !request.coupleRows().isEmpty()) {
			for (Integer row : request.coupleRows()) {
				if (row < 1 || row > request.rows()) {
					throw new IllegalArgumentException(
							"Couple row " + row + " is out of range. Hall has " + request.rows() + " rows");
				}
			}
		}
	}
}