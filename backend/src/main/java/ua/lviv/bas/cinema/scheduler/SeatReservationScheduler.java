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

	@Scheduled(fixedRate = 60000)
	@Transactional
	public void expireTempSeatReservation() {
		LocalDateTime now = LocalDateTime.now();
		List<SeatReservation> expiredReservations = seatReservationRepository
				.findByStatusAndReservedUntilBefore(ReservationStatus.PENDING, now);

		if (expiredReservations.isEmpty()) {
			return;
		}

		expiredReservations.forEach(sr -> sr.setStatus(ReservationStatus.EXPIRED));
		seatReservationRepository.saveAll(expiredReservations);

		log.info("Expired {} temporary seat reservations", expiredReservations.size());
	}

	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void cleanupExpiredSeatReservations() {
		LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
		List<SeatReservation> oldExpired = seatReservationRepository
				.findByStatusAndReservedUntilBefore(ReservationStatus.EXPIRED, sevenDaysAgo);

		if (!oldExpired.isEmpty()) {
			seatReservationRepository.deleteAll(oldExpired);
			log.info("Cleaned up {} old expired seat reservations", oldExpired.size());
		}
	}
}