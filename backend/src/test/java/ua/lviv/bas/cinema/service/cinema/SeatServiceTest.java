package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
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
public class SeatServiceTest {

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private SeatMapper seatMapper;

	@InjectMocks
	private SeatService seatService;

	@Test
	void getSeatById_Success() {
		Seat seat = new Seat();
		seat.setId(1L);
		seat.setRow(1);
		seat.setNumber(1);

		SeatResponse response = SeatResponse.builder().id(1L).row(1).number(1).build();

		when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.getSeatById(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getRow()).isEqualTo(1);
	}

	@Test
	void getSeatById_WhenNotFound_ShouldThrowException() {
		when(seatRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatService.getSeatById(1L)).isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void updateSeatType_Success() {
		Seat seat = new Seat();
		seat.setId(1L);
		seat.setSeatType(SeatType.STANDARD);

		SeatResponse response = SeatResponse.builder().id(1L).seatType(SeatType.VIP).build();

		when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.updateSeatType(1L, SeatType.VIP);

		assertThat(result.getSeatType()).isEqualTo(SeatType.VIP);
	}

	@Test
	void getSeatsByHall_Success() {
		Seat seat1 = new Seat();
		seat1.setId(1L);
		seat1.setRow(1);
		seat1.setNumber(1);

		Seat seat2 = new Seat();
		seat2.setId(2L);
		seat2.setRow(1);
		seat2.setNumber(2);

		SeatResponse response1 = SeatResponse.builder().id(1L).build();
		SeatResponse response2 = SeatResponse.builder().id(2L).build();

		when(seatRepository.findByHallId(1L)).thenReturn(Arrays.asList(seat1, seat2));
		when(seatMapper.toSeatResponseList(Arrays.asList(seat1, seat2)))
				.thenReturn(Arrays.asList(response1, response2));

		List<SeatResponse> result = seatService.getSeatsByHall(1L);

		assertThat(result).hasSize(2);
	}

	@Test
	void getSeatByPosition_Success() {
		Seat seat = new Seat();
		seat.setId(1L);
		seat.setRow(1);
		seat.setNumber(1);

		SeatResponse response = SeatResponse.builder().id(1L).row(1).number(1).build();

		when(seatRepository.findByHallIdAndRowAndNumber(1L, 1, 1)).thenReturn(Optional.of(seat));
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.getSeatByPosition(1L, 1, 1);

		assertThat(result.getRow()).isEqualTo(1);
		assertThat(result.getNumber()).isEqualTo(1);
	}

	@Test
	void getSeatByPosition_WhenNotFound_ShouldThrowException() {
		when(seatRepository.findByHallIdAndRowAndNumber(1L, 1, 1)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> seatService.getSeatByPosition(1L, 1, 1)).isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void isSeatAvailable_WhenActive() {
		when(seatRepository.existsByHallIdAndRowAndNumberAndActiveTrue(1L, 1, 1)).thenReturn(true);

		boolean result = seatService.isSeatAvailable(1L, 1, 1);

		assertThat(result).isTrue();
	}

	@Test
	void countSeatsByHall_Success() {
		when(seatRepository.countByHallId(1L)).thenReturn(50L);

		long result = seatService.countSeatsByHall(1L);

		assertThat(result).isEqualTo(50);
	}

	@Test
	void getSeatsByType_Success() {
		Seat seat = new Seat();
		seat.setId(1L);
		seat.setSeatType(SeatType.VIP);

		SeatResponse response = SeatResponse.builder().id(1L).seatType(SeatType.VIP).build();

		when(seatRepository.findByHallIdAndSeatType(1L, SeatType.VIP)).thenReturn(Collections.singletonList(seat));
		when(seatMapper.toSeatResponseList(Collections.singletonList(seat)))
				.thenReturn(Collections.singletonList(response));

		List<SeatResponse> result = seatService.getSeatsByType(1L, SeatType.VIP);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getSeatType()).isEqualTo(SeatType.VIP);
	}

	@Test
	void activateSeat_Success() {
		Seat seat = new Seat();
		seat.setId(1L);
		seat.setActive(false);

		SeatResponse response = SeatResponse.builder().id(1L).active(true).build();

		when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.activateSeat(1L);

		assertThat(result.isActive()).isTrue();
	}

	@Test
	void deactivateSeat_Success() {
		Seat seat = new Seat();
		seat.setId(1L);
		seat.setActive(true);

		SeatResponse response = SeatResponse.builder().id(1L).active(false).build();

		when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));
		when(seatRepository.save(seat)).thenReturn(seat);
		when(seatMapper.toSeatResponse(seat)).thenReturn(response);

		SeatResponse result = seatService.deactivateSeat(1L);

		assertThat(result.isActive()).isFalse();
	}

	@Test
	void getActiveSeatsByHall_Success() {
		Seat seat = new Seat();
		seat.setId(1L);
		seat.setActive(true);

		SeatResponse response = SeatResponse.builder().id(1L).active(true).build();

		when(seatRepository.findByHallIdAndActiveTrue(1L)).thenReturn(Collections.singletonList(seat));
		when(seatMapper.toSeatResponseList(Collections.singletonList(seat)))
				.thenReturn(Collections.singletonList(response));

		List<SeatResponse> result = seatService.getActiveSeatsByHall(1L);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).isActive()).isTrue();
	}

	@Test
	void getDistinctRowsByHall_Success() {
		when(seatRepository.findDistinctRowsByHallId(1L)).thenReturn(Arrays.asList(1, 2, 3));

		List<Integer> result = seatService.getDistinctRowsByHall(1L);

		assertThat(result).containsExactly(1, 2, 3);
	}

	@Test
	void getSeatsByIds_Success() {
		Seat seat1 = new Seat();
		seat1.setId(1L);

		Seat seat2 = new Seat();
		seat2.setId(2L);

		when(seatRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(seat1, seat2));

		List<Seat> result = seatService.getSeatsByIds(Arrays.asList(1L, 2L));

		assertThat(result).hasSize(2);
	}
}