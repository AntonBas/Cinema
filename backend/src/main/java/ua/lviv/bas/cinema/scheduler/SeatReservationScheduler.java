package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatReservationScheduler {

	private final SeatReservationRepository seatReservationRepository;

	@Scheduled(fixedRateString = "${scheduler.seat-reservation.expiration-interval:6000}")
	@Transactional
	public void expireTempSeatReservation() {
		LocalDateTime now = LocalDateTime.now();
		List<SeatReservation> expiredReservations = seatReservationRepository
				.findByStatusAndReservedUntilBefore(ReservationStatus.PENDING, now);

		if (expiredReservations.isEmpty()) {
			return;
		}

		seatReservationRepository.deleteAll(expiredReservations);
		log.info("Deleted {} expired temporary seat reservations", expiredReservations.size());
	}
}