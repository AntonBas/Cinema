package ua.lviv.bas.cinema.service.cinema;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.SeatMapper;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatService {

	private final SeatRepository seatRepository;
	private final SeatMapper seatMapper;

	@Transactional
	@Caching(evict = { @CacheEvict(value = "seats", key = "#seatId"),
			@CacheEvict(value = "seats", key = "'hall:' + #hallId"),
			@CacheEvict(value = "cinemaHalls", key = "'layout:' + #hallId") })
	public SeatResponse updateSeatType(Long hallId, Long seatId, SeatType seatType) {
		log.info("Updating seat type for seat id: {} to {}", seatId, seatType);
		Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));
		seat.setSeatType(seatType);
		Seat updated = seatRepository.save(seat);
		return seatMapper.toSeatResponse(updated);
	}

	@Transactional
	@Caching(evict = { @CacheEvict(value = "seats", key = "#seatId"),
			@CacheEvict(value = "seats", key = "'hall:' + #hallId"),
			@CacheEvict(value = "cinemaHalls", key = "'layout:' + #hallId") })
	public SeatResponse setSeatActiveStatus(Long hallId, Long seatId, boolean active) {
		log.info("Setting seat active status: seatId={}, active={}", seatId, active);
		Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));
		seat.setActive(active);
		Seat updated = seatRepository.save(seat);
		return seatMapper.toSeatResponse(updated);
	}

}