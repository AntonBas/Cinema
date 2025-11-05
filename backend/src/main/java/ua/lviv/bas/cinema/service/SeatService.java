package ua.lviv.bas.cinema.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.shared.SeatDto;
import ua.lviv.bas.cinema.exception.SeatNotFoundException;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.SeatRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

	private final SeatRepository seatRepository;
	private final SeatMapper seatMapper;

	@Transactional(readOnly = true)
	public SeatDto getSeatById(Long id) {
		log.debug("Retrieving seat by id: {}", id);
		return seatRepository.findById(id).map(seatMapper::toDto).orElseThrow(() -> new SeatNotFoundException(id));
	}

	@Transactional
	public SeatDto updateSeatType(Long id, SeatType seatType) {
		log.info("Updating seat type for seat id: {} to {}", id, seatType);

		Seat seat = seatRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Seat not found with id: " + id));

		seat.setSeatType(seatType);
		Seat updated = seatRepository.save(seat);
		log.debug("Seat type updated for seat ID: {}", id);
		return seatMapper.toDto(updated);
	}

	@Transactional(readOnly = true)
	public List<SeatDto> getSeatsByHall(Long hallId) {
		log.debug("Retrieving seats for hall id: {}", hallId);
		return seatMapper.toDtoList(seatRepository.findByHallId(hallId));
	}

	@Transactional(readOnly = true)
	public SeatDto getSeatByPosition(Long hallId, int row, int number) {
		log.debug("Retrieving seat at position: hall={}, row={}, number={}", hallId, row, number);
		return seatRepository.findByHallIdAndRowAndNumber(hallId, row, number).map(seatMapper::toDto)
				.orElseThrow(() -> new SeatNotFoundException(hallId, row, number));
	}

	@Transactional(readOnly = true)
	public boolean isSeatAvailable(Long hallId, int row, int number) {
		log.debug("Checking seat availability: hall={}, row={}, number={}", hallId, row, number);
		return seatRepository.existsByHallIdAndRowAndNumber(hallId, row, number);
	}

	@Transactional(readOnly = true)
	public long countSeatsByHall(Long hallId) {
		log.debug("Counting seats for hall id: {}", hallId);
		return seatRepository.countByHallId(hallId);
	}

	@Transactional(readOnly = true)
	public List<SeatDto> getSeatsByType(Long hallId, SeatType seatType) {
		log.debug("Retrieving {} seats for hall id: {}", seatType, hallId);
		return seatMapper.toDtoList(seatRepository.findByHallIdAndSeatType(hallId, seatType));
	}

}
