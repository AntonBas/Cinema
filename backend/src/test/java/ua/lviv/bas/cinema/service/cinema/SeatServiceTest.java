package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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
class SeatServiceTest {

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
	private final SeatType SEAT_TYPE = SeatType.VIP;

	@Test
	void getSeatById_Success() {
		Seat seat = createSeat();
		SeatResponse response = createSeatResponse();

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
	void updateSeatType_Success() {
		Seat seat = createSeat();
		seat.setSeatType(SeatType.STANDARD);
		SeatResponse response = createSeatResponse();

		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.updateSeatType(SEAT_ID, SEAT_TYPE);

		assertThat(result).isEqualTo(response);
		assertThat(seat.getSeatType()).isEqualTo(SEAT_TYPE);
	}

	@Test
	void setSeatActiveStatus_Success() {
		Seat seat = createSeat();
		seat.setActive(false);
		SeatResponse response = createSeatResponse();

		when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.setSeatActiveStatus(SEAT_ID, true);

		assertThat(result).isEqualTo(response);
		assertThat(seat.isActive()).isTrue();
	}

	@Test
	void getSeatsByHall_Success() {
		Seat seat = createSeat();
		SeatResponse response = createSeatResponse();

		when(seatRepository.findByHallId(HALL_ID)).thenReturn(List.of(seat));
		when(seatMapper.toSeatResponseList(List.of(seat))).thenReturn(List.of(response));

		List<SeatResponse> result = seatService.getSeatsByHall(HALL_ID);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getSeatByPosition_Success() {
		Seat seat = createSeat();
		SeatResponse response = createSeatResponse();

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
	void isSeatAvailable_True() {
		when(seatRepository.existsByHallIdAndRowAndNumberAndActiveTrue(HALL_ID, ROW, NUMBER)).thenReturn(true);

		boolean result = seatService.isSeatAvailable(HALL_ID, ROW, NUMBER);

		assertThat(result).isTrue();
	}

	@Test
	void isSeatAvailable_False() {
		when(seatRepository.existsByHallIdAndRowAndNumberAndActiveTrue(HALL_ID, ROW, NUMBER)).thenReturn(false);

		boolean result = seatService.isSeatAvailable(HALL_ID, ROW, NUMBER);

		assertThat(result).isFalse();
	}

	@Test
	void getSeatsByType_Success() {
		Seat seat = createSeat();
		SeatResponse response = createSeatResponse();

		when(seatRepository.findByHallIdAndSeatType(HALL_ID, SEAT_TYPE)).thenReturn(List.of(seat));
		when(seatMapper.toSeatResponseList(List.of(seat))).thenReturn(List.of(response));

		List<SeatResponse> result = seatService.getSeatsByType(HALL_ID, SEAT_TYPE);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getActiveSeatsByHall_Success() {
		Seat seat = createSeat();
		SeatResponse response = createSeatResponse();

		when(seatRepository.findByHallIdAndActiveTrue(HALL_ID)).thenReturn(List.of(seat));
		when(seatMapper.toSeatResponseList(List.of(seat))).thenReturn(List.of(response));

		List<SeatResponse> result = seatService.getActiveSeatsByHall(HALL_ID);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getSeatsByIds_Success() {
		Seat seat = createSeat();
		List<Long> ids = List.of(SEAT_ID);

		when(seatRepository.findAllById(ids)).thenReturn(List.of(seat));

		List<Seat> result = seatService.getSeatsByIds(ids);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(seat);
	}

	private Seat createSeat() {
		Seat seat = new Seat();
		seat.setId(SEAT_ID);
		seat.setRow(ROW);
		seat.setNumber(NUMBER);
		seat.setSeatType(SEAT_TYPE);
		seat.setActive(true);
		return seat;
	}

	private SeatResponse createSeatResponse() {
		return SeatResponse.builder().id(SEAT_ID).row(ROW).number(NUMBER).seatType(SEAT_TYPE).active(true).build();
	}
}