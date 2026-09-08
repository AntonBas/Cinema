package ua.lviv.bas.cinema.cinema.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatMapper seatMapper;

    @CacheEvict(value = {"cinemaHalls", "seatAvailability"}, allEntries = true)
    @Transactional
    public SeatResponse updateSeatType(Long hallId, Long seatId, SeatType seatType) {
        log.info("Updating seat type for seat id: {} in hall {} to {}", seatId, hallId, seatType);
        var seat = findSeatInHall(hallId, seatId);
        seat.setSeatType(seatType);
        var updated = seatRepository.save(seat);
        return seatMapper.toSeatResponse(updated);
    }

    @CacheEvict(value = {"cinemaHalls", "seatAvailability"}, allEntries = true)
    @Transactional
    public SeatResponse setSeatActiveStatus(Long hallId, Long seatId, boolean active) {
        log.info("Setting seat active status: hallId={}, seatId={}, active={}", hallId, seatId, active);
        var seat = findSeatInHall(hallId, seatId);
        seat.setActive(active);
        var updated = seatRepository.save(seat);
        return seatMapper.toSeatResponse(updated);
    }

    private Seat findSeatInHall(Long hallId, Long seatId) {
        var seat = seatRepository.findById(seatId).orElseThrow(() -> new EntityNotFoundException("Seat", seatId));
        if (!seat.getHall().getId().equals(hallId)) {
            throw new EntityNotFoundException("Seat", seatId);
        }
        return seat;
    }
}