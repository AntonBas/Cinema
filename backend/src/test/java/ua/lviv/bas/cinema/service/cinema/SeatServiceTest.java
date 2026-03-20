package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.SeatRepository;

@ExtendWith(MockitoExtension.class)
public class SeatServiceTest {

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private SeatMapper seatMapper;

	@InjectMocks
	private SeatService seatService;

	private final Long SEAT_ID = 1L;
	private final Long HALL_ID = 10L;
	private final int ROW = 1;
	private final int NUMBER = 5;
	private Seat seat;
	private SeatResponse response;

	@BeforeEach
	void setUp() {
		seat = new Seat();
		seat.setId(SEAT_ID);
		seat.setRow(ROW);
		seat.setNumber(NUMBER);
		seat.setSeatType(SeatType.STANDARD);
		seat.setActive(true);

		response = new SeatResponse(SEAT_ID, ROW, NUMBER, SeatType.STANDARD, true);
	}

	@Test
	void getSeatById_ShouldReturnSeat() {
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.getSeatById(SEAT_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getSeatById_NotFound_ThrowsException() {
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatService.getSeatById(SEAT_ID)).isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void updateSeatType_ShouldUpdateType() {
		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.updateSeatType(SEAT_ID, SeatType.VIP);

		assertThat(result).isEqualTo(response);
		assertThat(seat.getSeatType()).isEqualTo(SeatType.VIP);
		verify(seatRepository).save(seat);
	}

	@Test
	void setSeatActiveStatus_ShouldUpdateStatus() {
		seat.setActive(false);

		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.setSeatActiveStatus(SEAT_ID, true);

		assertThat(result).isEqualTo(response);
		assertThat(seat.isActive()).isTrue();
		verify(seatRepository).save(seat);
	}

	@Test
	void getSeatsByHall_ShouldReturnList() {
		when(seatRepository.findByHallId(HALL_ID)).thenReturn(List.of(seat));
		when(seatMapper.toSeatResponseList(List.of(seat))).thenReturn(List.of(response));

		List<SeatResponse> result = seatService.getSeatsByHall(HALL_ID);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getSeatByPosition_ShouldReturnSeat() {
		when(seatRepository.findByHallIdAndRowAndNumber(HALL_ID, ROW, NUMBER)).thenReturn(Optional.of(seat));
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.getSeatByPosition(HALL_ID, ROW, NUMBER);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getSeatByPosition_NotFound_ThrowsException() {
		when(seatRepository.findByHallIdAndRowAndNumber(HALL_ID, ROW, NUMBER)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatService.getSeatByPosition(HALL_ID, ROW, NUMBER))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void getSeatsByIds_ShouldReturnList() {
		List<Long> ids = List.of(SEAT_ID);
		when(seatRepository.findAllById(ids)).thenReturn(List.of(seat));

		List<Seat> result = seatService.getSeatsByIds(ids);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(seat);
	}

	@Test
	void getSeatsGroupedByRow_ShouldReturnMap() {
		Seat seat2 = new Seat();
		seat2.setId(2L);
		seat2.setRow(1);
		seat2.setNumber(6);

		when(seatRepository.findByHallId(HALL_ID)).thenReturn(List.of(seat, seat2));

		var result = seatService.getSeatsGroupedByRow(HALL_ID);

		assertThat(result).hasSize(1);
		assertThat(result.get(1)).hasSize(2);
	}
}