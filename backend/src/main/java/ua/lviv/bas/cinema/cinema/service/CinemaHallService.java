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
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.hall.CinemaHallHasSessionsException;
import ua.lviv.bas.cinema.exception.domain.hall.HallLayoutHasTicketsException;
import ua.lviv.bas.cinema.exception.domain.hall.InvalidCoupleRowsConfigurationException;
import ua.lviv.bas.cinema.exception.domain.hall.InvalidSeatsPerRowForCoupleRowsException;
import ua.lviv.bas.cinema.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.common.CacheableList;
import ua.lviv.bas.cinema.common.UniquenessValidator;
import ua.lviv.bas.cinema.audit.service.AuditDetails;
import ua.lviv.bas.cinema.audit.service.AuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @CacheEvict(value = {"cinemaHalls", "seats"}, allEntries = true)
    @Transactional
    public CinemaHallResponse createHall(CinemaHallRequest request) {
        log.info("Creating cinema hall: {}", request.name());
        validateHallNameUniqueness(request.name(), null);
        validateCoupleRowsConfiguration(request);
        validateSeatsPerRowForCoupleRows(request);

        var hall = CinemaHall.builder().name(request.name()).build();

        if (request.rows() != null && request.seatsPerRow() != null) {
            var seats = generateSeatLayout(hall, request);
            hall.setSeats(seats);
            log.debug("Generated {} seats during hall creation", seats.size());
        }

        var saved = hallRepository.save(hall);
        log.debug("Cinema hall created with ID: {}", saved.getId());
        auditCreate(saved, request);

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

    @CacheEvict(value = {"cinemaHalls", "seats"}, allEntries = true)
    @Transactional
    public CinemaHallResponse updateHall(Long id, CinemaHallRequest request) {
        log.info("Updating cinema hall with id: {}", id);

        var hall = hallRepository.findByIdWithSeats(id)
                .orElseThrow(() -> new EntityNotFoundException("Cinema hall", id));
        String oldName = hall.getName();

        validateHallHasNoFutureSessions(hall);
        if (!hall.getName().equals(request.name())) {
            validateHallNameUniqueness(request.name(), id);
        }
        validateCoupleRowsConfiguration(request);
        validateSeatsPerRowForCoupleRows(request);

        hall.setName(request.name());

        if (isLayoutChanged(hall, request)) {
            validateHallHasNoTickets(id);
            hall.getSeats().clear();
            hallRepository.flush();
            var newSeats = generateSeatLayout(hall, request);
            hall.getSeats().addAll(newSeats);
        }

        var updated = hallRepository.save(hall);
        log.debug("Cinema hall updated with ID: {}", updated.getId());
        auditUpdate(id, oldName, updated);

        return hallMapper.toCinemaHallResponse(updated);
    }

    @CacheEvict(value = {"cinemaHalls", "seats"}, allEntries = true)
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

    private void validateHallHasNoTickets(Long hallId) {
        if (seatRepository.hasTicketsForHall(hallId)) {
            throw new HallLayoutHasTicketsException();
        }
    }

    private boolean isLayoutChanged(CinemaHall hall, CinemaHallRequest request) {
        if (hall.getSeats().isEmpty()) {
            return true;
        }
        var seatsByRow = hall.getSeats().stream().collect(Collectors.groupingBy(Seat::getRow));
        int currentRows = seatsByRow.size();
        long standardSeatsInFirstRow = seatsByRow.getOrDefault(1, List.of()).stream()
                .filter(seat -> seat.getSeatType() != SeatType.COUPLE).count();
        return currentRows != request.rows() || standardSeatsInFirstRow != request.seatsPerRow();
    }

    private List<Seat> generateSeatLayout(CinemaHall hall, CinemaHallRequest request) {
        List<Seat> seats = new ArrayList<>();
        SeatType defaultType = request.defaultSeatType();
        List<Integer> coupleRows = request.coupleRows() != null ? request.coupleRows() : List.of();

        for (int row = 1; row <= request.rows(); row++) {
            if (coupleRows.contains(row)) {
                int coupleSeatNumber = 1;
                for (int pos = 1; pos <= request.seatsPerRow(); pos += 2) {
                    seats.add(Seat.builder().row(row).number(coupleSeatNumber++).seatType(SeatType.COUPLE).hall(hall)
                            .build());
                }
            } else {
                for (int number = 1; number <= request.seatsPerRow(); number++) {
                    SeatType seatType = row > request.rows() - 2 ? SeatType.VIP : defaultType;
                    seats.add(Seat.builder().row(row).number(number).seatType(seatType).hall(hall).build());
                }
            }
        }
        return seats;
    }

    private void validateCoupleRowsConfiguration(CinemaHallRequest request) {
        if (request.coupleRows() != null && !request.coupleRows().isEmpty()) {
            for (Integer row : request.coupleRows()) {
                if (row < 1 || row > request.rows()) {
                    throw new InvalidCoupleRowsConfigurationException(row, request.rows());
                }
            }
        }
    }

    private void validateSeatsPerRowForCoupleRows(CinemaHallRequest request) {
        boolean hasCoupleRows = request.coupleRows() != null && !request.coupleRows().isEmpty();
        if (hasCoupleRows && request.seatsPerRow() % 2 != 0) {
            throw new InvalidSeatsPerRowForCoupleRowsException(request.coupleRows().toString());
        }
    }

    private void auditCreate(CinemaHall hall, CinemaHallRequest request) {
        var details = AuditDetails.of().put("name", hall.getName()).put("rows", request.rows())
                .put("seatsPerRow", request.seatsPerRow()).build();
        auditService.logChange("CinemaHall", hall.getId(), hall.getName(), AuditAction.CREATED, null, details);
    }

    private void auditUpdate(Long id, String oldName, CinemaHall updated) {
        var oldDetails = AuditDetails.of().put("name", oldName).build();
        var newDetails = AuditDetails.of().put("name", updated.getName()).build();
        auditService.logChange("CinemaHall", id, oldName, AuditAction.UPDATED, oldDetails, newDetails);
    }

    private void auditDelete(Long id, String hallName) {
        var details = AuditDetails.of().put("deleted", hallName).build();
        auditService.logChange("CinemaHall", id, hallName, AuditAction.DELETED, details, null);
    }
}