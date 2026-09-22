package ua.lviv.bas.cinema.cinema.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.request.HallLayoutRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.request.SeatLayoutItemRequest;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import({ TestcontainersConfig.class, NoOpCacheTestConfig.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CinemaHallServiceLayoutIntegrationTest {

    @Autowired
    private CinemaHallService cinemaHallService;
    @Autowired
    private CinemaHallRepository cinemaHallRepository;
    @Autowired
    private SeatRepository seatRepository;

    @Test
    void updateLayoutShouldReplaceAllSeatsReusingSamePositions() {
        var hall = cinemaHallRepository.save(CinemaHall.builder().name("ZZTEST Layout").build());
        seatRepository.save(Seat.builder().row(1).number(1).x(0).y(0).hall(hall)
                .seatType(SeatType.STANDARD).build());
        seatRepository.save(Seat.builder().row(1).number(2).x(60).y(0).hall(hall)
                .seatType(SeatType.STANDARD).build());

        var request = new HallLayoutRequest(List.of(
                new SeatLayoutItemRequest(null, 1, 1, SeatType.VIP, 0, 0, true),
                new SeatLayoutItemRequest(null, 1, 2, SeatType.VIP, 60, 0, true)));

        var result = cinemaHallService.updateLayout(hall.getId(), request);

        assertThat(result.totalSeats()).isEqualTo(2);
        assertThat(seatRepository.findByHallId(hall.getId())).hasSize(2)
                .allMatch(seat -> seat.getSeatType() == SeatType.VIP);
    }
}
