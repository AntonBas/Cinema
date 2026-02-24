package ua.lviv.bas.cinema.service.cinema;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.SeatRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

	private final SeatRepository seatRepository;
	private final SeatMapper seatMapper;

	@Transactional(readOnly = true)
	@Cacheable(value = "seats", key = "#id")
	public SeatResponse getSeatById(Long id) {
		log.debug("Retrieving seat by id: {}", id);
		return seatRepository.findById(id).map(seatMapper::toSeatResponse)
				.orElseThrow(() -> new SeatNotFoundException(id));
	}

	@Transactional
	public SeatResponse updateSeatType(Long id, SeatType seatType) {
		log.info("Updating seat type for seat id: {} to {}", id, seatType);
		Seat seat = seatRepository.findById(id).orElseThrow(() -> new SeatNotFoundException(id));
		seat.setSeatType(seatType);
		Seat updated = seatRepository.save(seat);
		return seatMapper.toSeatResponse(updated);
	}

	@Transactional
	public SeatResponse setSeatActiveStatus(Long id, boolean active) {
		log.info("Setting seat active status: id={}, active={}", id, active);
		Seat seat = seatRepository.findById(id).orElseThrow(() -> new SeatNotFoundException(id));
		seat.setActive(active);
		Seat updated = seatRepository.save(seat);
		return seatMapper.toSeatResponse(updated);
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "seats", key = "'hall:' + #hallId")
	public List<SeatResponse> getSeatsByHall(Long hallId) {
		log.debug("Retrieving seats for hall id: {}", hallId);
		return seatMapper.toSeatResponseList(seatRepository.findByHallId(hallId));
	}

	@Transactional(readOnly = true)
	public SeatResponse getSeatByPosition(Long hallId, int row, int number) {
		log.debug("Retrieving seat at position: hall={}, row={}, number={}", hallId, row, number);
		return seatRepository.findByHallIdAndRowAndNumber(hallId, row, number).map(seatMapper::toSeatResponse)
				.orElseThrow(() -> new SeatNotFoundException(hallId, row, number));
	}

	@Transactional(readOnly = true)
	public List<Seat> getSeatsByIds(List<Long> seatIds) {
		log.debug("Retrieving seats by ids: {}", seatIds);
		return seatRepository.findAllById(seatIds);
	}

	@Transactional(readOnly = true)
	public Map<Integer, List<Seat>> getSeatsGroupedByRow(Long hallId) {
		log.debug("Retrieving seats grouped by row for hall id: {}", hallId);
		return seatRepository.findByHallId(hallId).stream().collect(Collectors.groupingBy(Seat::getRow));
	}
}