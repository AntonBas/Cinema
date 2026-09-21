package ua.lviv.bas.cinema.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.request.HallLayoutRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.request.SeatLayoutItemRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.hall.CinemaHallHasSessionsException;
import ua.lviv.bas.cinema.exception.domain.hall.DuplicateSeatPositionException;
import ua.lviv.bas.cinema.exception.domain.hall.SeatHasTicketsException;
import ua.lviv.bas.cinema.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.common.CacheableList;
import ua.lviv.bas.cinema.common.UniquenessValidator;
import ua.lviv.bas.cinema.audit.service.AuditDetails;
import ua.lviv.bas.cinema.audit.service.AuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinemaHallService {

    private final CinemaHallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final CinemaHallMapper hallMapper;
    private final AuditService auditService;

    @CacheEvict(value = "cinemaHalls", allEntries = true)
    @Transactional
    public CinemaHallResponse createHall(CinemaHallRequest request) {
        log.info("Creating cinema hall: {}", request.name());
        validateHallNameUniqueness(request.name(), null);

        var hall = CinemaHall.builder().name(request.name()).build();
        var saved = hallRepository.save(hall);
        log.debug("Cinema hall created with ID: {}", saved.getId());
        auditCreate(saved);

        return hallMapper.toCinemaHallResponse(saved);
    }

    @Cacheable(value = "cinemaHalls", key = "#id")
    public CinemaHallResponse getHall(Long id) {
        log.debug("Retrieving cinema hall by id: {}", id);
        return hallRepository.findByIdWithSeats(id).map(hallMapper::toCinemaHallResponse)
                .orElseThrow(() -> new EntityNotFoundException("Cinema hall", id));
    }

    @Cacheable(value = "cinemaHalls", key = "'list'")
    public List<CinemaHallListResponse> getHalls() {
        log.debug("Retrieving all cinema halls");
        var projections = hallRepository.findAllProjected();
        return new CacheableList<>(projections.stream().map(hallMapper::toCinemaHallListResponse).toList());
    }

    @Cacheable(value = "cinemaHalls", key = "'layout:' + #hallId")
    public HallLayoutResponse getHallLayout(Long hallId) {
        log.debug("Retrieving hall layout for id: {}", hallId);
        var hall = hallRepository.findByIdWithSeats(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Cinema hall", hallId));
        return hallMapper.toHallLayoutResponse(hall);
    }

    public CinemaHall getHallEntity(Long id) {
        log.debug("Retrieving cinema hall entity by id: {}", id);
        return hallRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Cinema hall", id));
    }

    @CacheEvict(value = "cinemaHalls", allEntries = true)
    @Transactional
    public CinemaHallResponse updateHall(Long id, CinemaHallRequest request) {
        log.info("Renaming cinema hall with id: {}", id);

        var hall = hallRepository.findByIdWithSeats(id)
                .orElseThrow(() -> new EntityNotFoundException("Cinema hall", id));
        String oldName = hall.getName();

        validateHallHasNoFutureSessions(hall);
        if (!hall.getName().equals(request.name())) {
            validateHallNameUniqueness(request.name(), id);
        }
        hall.setName(request.name());

        var updated = hallRepository.save(hall);
        log.debug("Cinema hall renamed, ID: {}", updated.getId());
        auditUpdate(id, oldName, updated);

        return hallMapper.toCinemaHallResponse(updated);
    }

    @CacheEvict(value = { "cinemaHalls", "seatAvailability" }, allEntries = true)
    @Transactional
    public HallLayoutResponse updateLayout(Long hallId, HallLayoutRequest request) {
        log.info("Updating seat layout for hall id: {}", hallId);

        var hall = hallRepository.findByIdWithSeats(hallId)
                .orElseThrow(() -> new EntityNotFoundException("Cinema hall", hallId));
        validateHallHasNoFutureSessions(hall);
        validateNoDuplicatePositions(request);

        var existingById = hall.getSeats().stream().collect(Collectors.toMap(Seat::getId, s -> s));
        var requestedIds = request.seats().stream().map(SeatLayoutItemRequest::id).filter(Objects::nonNull)
                .collect(Collectors.toSet());

        var removedIds = existingById.keySet().stream().filter(id -> !requestedIds.contains(id)).toList();
        var repositionedIds = request.seats().stream()
                .filter(item -> item.id() != null && existingById.containsKey(item.id()))
                .filter(item -> hasPositionChanged(existingById.get(item.id()), item))
                .map(SeatLayoutItemRequest::id)
                .toList();

        var protectedIds = new ArrayList<>(removedIds);
        protectedIds.addAll(repositionedIds);
        validateNoTicketsOn(protectedIds);

        hall.getSeats().removeIf(seat -> removedIds.contains(seat.getId()));
        for (var item : request.seats()) {
            if (item.id() == null) {
                hall.getSeats().add(toNewSeat(hall, item));
            } else {
                applyChanges(existingById.get(item.id()), item);
            }
        }

        var saved = hallRepository.save(hall);
        log.debug("Hall layout updated for hall id: {} ({} seats)", hallId, saved.getSeats().size());
        auditLayoutUpdate(hallId, hall.getName(), saved);

        return hallMapper.toHallLayoutResponse(saved);
    }

    @CacheEvict(value = "cinemaHalls", allEntries = true)
    @Transactional
    public void deleteHall(Long id) {
        log.info("Deleting cinema hall with id: {}", id);

        var hall = hallRepository.findByIdWithSeats(id)
                .orElseThrow(() -> new EntityNotFoundException("Cinema hall", id));
        String hallName = hall.getName();

        validateHallHasNoFutureSessions(hall);
        hallRepository.delete(hall);

        log.debug("Cinema hall deleted with ID: {}", id);
        auditDelete(id, hallName);
    }

    private void validateHallNameUniqueness(String name, Long excludeId) {
        UniquenessValidator.validate(excludeId, () -> hallRepository.existsByName(name),
                id -> hallRepository.existsByNameAndIdNot(name, id),
                () -> new DuplicateEntityException("CinemaHall", name));
    }

    private void validateHallHasNoFutureSessions(CinemaHall hall) {
        boolean hasFutureSessions = hall.getSessions().stream()
                .anyMatch(session -> session.getStartTime().isAfter(LocalDateTime.now()));
        if (hasFutureSessions) {
            throw new CinemaHallHasSessionsException(hall.getName(), hall.getId());
        }
    }

    private void validateNoDuplicatePositions(HallLayoutRequest request) {
        Set<String> seenPositions = new HashSet<>();
        for (var item : request.seats()) {
            if (!seenPositions.add(item.row() + ":" + item.number())) {
                throw new DuplicateSeatPositionException(item.row(), item.number());
            }
        }
    }

    private void validateNoTicketsOn(List<Long> seatIds) {
        if (seatIds.isEmpty()) {
            return;
        }
        var ticketedSeatIds = seatRepository.findTicketedSeatIds(seatIds);
        if (!ticketedSeatIds.isEmpty()) {
            throw new SeatHasTicketsException(ticketedSeatIds);
        }
    }

    private boolean hasPositionChanged(Seat existing, SeatLayoutItemRequest item) {
        return !existing.getRow().equals(item.row()) || !existing.getNumber().equals(item.number());
    }

    private Seat toNewSeat(CinemaHall hall, SeatLayoutItemRequest item) {
        return Seat.builder().row(item.row()).number(item.number()).seatType(item.seatType()).x(item.x())
                .y(item.y()).active(item.active()).hall(hall).build();
    }

    private void applyChanges(Seat seat, SeatLayoutItemRequest item) {
        seat.setRow(item.row());
        seat.setNumber(item.number());
        seat.setSeatType(item.seatType());
        seat.setX(item.x());
        seat.setY(item.y());
        seat.setActive(item.active());
    }

    private void auditCreate(CinemaHall hall) {
        var details = AuditDetails.of().put("name", hall.getName()).build();
        auditService.logChange("CinemaHall", hall.getId(), hall.getName(), AuditAction.CREATED, null, details);
    }

    private void auditUpdate(Long id, String oldName, CinemaHall updated) {
        var oldDetails = AuditDetails.of().put("name", oldName).build();
        var newDetails = AuditDetails.of().put("name", updated.getName()).build();
        auditService.logChange("CinemaHall", id, oldName, AuditAction.UPDATED, oldDetails, newDetails);
    }

    private void auditLayoutUpdate(Long id, String hallName, CinemaHall updated) {
        var details = AuditDetails.of().put("seatsCount", updated.getSeats().size()).build();
        auditService.logChange("CinemaHall", id, hallName, AuditAction.UPDATED, null, details);
    }

    private void auditDelete(Long id, String hallName) {
        var details = AuditDetails.of().put("deleted", hallName).build();
        auditService.logChange("CinemaHall", id, hallName, AuditAction.DELETED, details, null);
    }
}
