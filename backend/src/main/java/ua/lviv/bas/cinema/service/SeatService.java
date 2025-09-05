package ua.lviv.bas.cinema.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.SeatRepository;
import ua.lviv.bas.cinema.dao.TicketRepository;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.SeatCreateDto;
import ua.lviv.bas.cinema.dto.SeatDto;
import ua.lviv.bas.cinema.mapper.SeatMapper;

@Service
@RequiredArgsConstructor
public class SeatService {

	private static final Logger logger = LogManager.getLogger(SeatService.class);

	private final SeatRepository seatRepository;
	private final TicketRepository ticketRepository;
	private final SeatMapper seatMapper;

	@Transactional(readOnly = true)
	public List<SeatDto> getSeatsByHallId(Long hallId) {
		logger.info("Retrieving seats for CinemaHall with id: {}", hallId);
		List<Seat> seats = seatRepository.findByHallId(hallId);
		return seats.stream().map(seatMapper::toDto).toList();
	}

	@Transactional
	public SeatDto createSeat(SeatCreateDto seatCreateDto) {
		logger.info("Creating seat: Row {}, Number {}", seatCreateDto.getRow(), seatCreateDto.getNumber());

		Seat seat = seatMapper.toEntity(seatCreateDto);
		Seat savedSeat = seatRepository.save(seat);

		logger.info("Seat created successfully with ID: {}", savedSeat.getId());
		return seatMapper.toDto(savedSeat);
	}

	@Transactional(readOnly = true)
	public SeatDto getSeatById(Long id) {
		logger.info("Reading seat with id: {}", id);
		Seat seat = seatRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Seat not found with id: " + id));
		return seatMapper.toDto(seat);
	}

	@Transactional
	public SeatDto updateSeat(Long id, SeatCreateDto seatUpdateDto) {
		logger.info("Updating seat with id: {}", id);
		Seat existingSeat = seatRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Seat not found with id: " + id));

		existingSeat.setRow(seatUpdateDto.getRow());
		existingSeat.setNumber(seatUpdateDto.getNumber());
		existingSeat.setSeatType(seatUpdateDto.getSeatType());

		Seat updatedSeat = seatRepository.save(existingSeat);
		logger.info("Seat updated successfully with ID: {}", updatedSeat.getId());
		return seatMapper.toDto(updatedSeat);
	}

	@Transactional
	public void deleteSeat(Long id) {
		logger.info("Deleting seat with id: {}", id);

		if (!seatRepository.existsById(id)) {
			throw new EntityNotFoundException("Seat not found with id: " + id);
		}

		seatRepository.deleteById(id);
		logger.info("Seat deleted successfully with ID: {}", id);
	}

	@Transactional(readOnly = true)
	public List<SeatDto> getAvailableSeatsForSession(Long hallId, Long sessionId) {
		logger.info("Retrieving available seats for Hall ID {} and Session ID {}", hallId, sessionId);
		List<Seat> allSeats = seatRepository.findByHallId(hallId);

		List<Seat> availableSeats = allSeats.stream()
				.filter(seat -> !ticketRepository.existsBySeatIdAndSessionId(seat.getId(), sessionId)).toList();

		logger.info("Found {} available seats out of {} total seats for session {}", availableSeats.size(),
				allSeats.size(), sessionId);

		return availableSeats.stream().map(seat -> {
			SeatDto dto = seatMapper.toDto(seat);
			dto.setAvailable(true);
			return dto;
		}).toList();
	}

	@Transactional(readOnly = true)
	public List<SeatDto> getAllSeatsForSession(Long hallId, Long sessionId) {
		logger.info("Retrieving all seats with availability for Hall ID {} and Session ID {}", hallId, sessionId);
		List<Seat> allSeats = seatRepository.findByHallId(hallId);

		return allSeats.stream().map(seat -> {
			SeatDto dto = seatMapper.toDto(seat);
			boolean isAvailable = !ticketRepository.existsBySeatIdAndSessionId(seat.getId(), sessionId);
			dto.setAvailable(isAvailable);
			return dto;
		}).toList();
	}

	@Transactional(readOnly = true)
	public Seat getSeatEntityById(Long id) {
		return seatRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Seat not found with id: " + id));
	}

	@Transactional(readOnly = true)
	public boolean existsById(Long id) {
		return seatRepository.existsById(id);
	}

	@Transactional(readOnly = true)
	public boolean isSeatAvailable(Long seatId, Long sessionId) {
		return !ticketRepository.existsBySeatIdAndSessionId(seatId, sessionId);
	}

	@Transactional(readOnly = true)
	public int getAvailableSeatsCountForSession(Long hallId, Long sessionId) {
		long totalSeats = seatRepository.countByHallId(hallId);
		long occupiedSeats = ticketRepository.countBySessionId(sessionId);
		return (int) (totalSeats - occupiedSeats);
	}
}
