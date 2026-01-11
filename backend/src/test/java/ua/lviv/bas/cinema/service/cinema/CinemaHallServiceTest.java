package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
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

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallWithSeatsResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;

@ExtendWith(MockitoExtension.class)
public class CinemaHallServiceTest {

	@Mock
	private CinemaHallRepository hallRepository;

	@Mock
	private SeatRepository seatRepository;

	@Mock
	private CinemaHallMapper hallMapper;

	@Mock
	private SeatMapper seatMapper;

	@InjectMocks
	private CinemaHallService cinemaHallService;

	@Test
	void createHall_Success() {
		CinemaHallRequest request = CinemaHallRequest.builder().name("Hall A").build();

		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Hall A");

		CinemaHallResponse response = CinemaHallResponse.builder().id(1L).name("Hall A").build();

		when(hallRepository.existsByName("Hall A")).thenReturn(false);
		when(hallRepository.save(any(CinemaHall.class))).thenReturn(cinemaHall);
		when(hallMapper.toCinemaHallResponse(cinemaHall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.createHall(request);

		assertThat(result.getName()).isEqualTo("Hall A");
		verify(hallRepository).existsByName("Hall A");
	}

	@Test
	void createHall_WithSeats() {
		CinemaHallRequest request = CinemaHallRequest.builder().name("Hall B").rows(5).seatsPerRow(10)
				.defaultSeatType(SeatType.STANDARD).build();

		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Hall B");

		CinemaHallResponse response = CinemaHallResponse.builder().id(1L).name("Hall B").capacity(50).build();

		when(hallRepository.existsByName("Hall B")).thenReturn(false);
		when(hallRepository.save(any(CinemaHall.class))).thenReturn(cinemaHall);
		when(hallMapper.toCinemaHallResponse(cinemaHall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.createHall(request);

		assertThat(result.getName()).isEqualTo("Hall B");
		assertThat(result.getCapacity()).isEqualTo(50);
	}

	@Test
	void createHall_WhenNameExists_ShouldThrowException() {
		CinemaHallRequest request = CinemaHallRequest.builder().name("Hall A").build();

		when(hallRepository.existsByName("Hall A")).thenReturn(true);

		assertThatThrownBy(() -> cinemaHallService.createHall(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getHallById_Success() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Hall A");

		CinemaHallResponse response = CinemaHallResponse.builder().id(1L).name("Hall A").build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallMapper.toCinemaHallResponse(cinemaHall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.getHallById(1L);

		assertThat(result.getName()).isEqualTo("Hall A");
	}

	@Test
	void getHallById_WhenNotFound_ShouldThrowException() {
		when(hallRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cinemaHallService.getHallById(1L)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void updateHall_Success() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Old Name");

		CinemaHallRequest request = CinemaHallRequest.builder().name("New Name").build();

		CinemaHallResponse response = CinemaHallResponse.builder().id(1L).name("New Name").build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.existsByName("New Name")).thenReturn(false);
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);
		when(hallMapper.toCinemaHallResponse(cinemaHall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.updateHall(1L, request);

		assertThat(result.getName()).isEqualTo("New Name");
	}

	@Test
	void updateHall_WithSeats() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Hall A");

		CinemaHallRequest request = CinemaHallRequest.builder().name("Hall A").rows(5).seatsPerRow(10)
				.defaultSeatType(SeatType.STANDARD).build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.save(cinemaHall)).thenReturn(cinemaHall);

		cinemaHallService.updateHall(1L, request);

		verify(seatRepository).deleteByHallId(1L);
		verify(seatRepository).saveAll(anyList());
	}

	@Test
	void updateHall_WhenNameExists_ShouldThrowException() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Old Name");

		CinemaHallRequest request = CinemaHallRequest.builder().name("Existing Name").build();

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));
		when(hallRepository.existsByName("Existing Name")).thenReturn(true);

		assertThatThrownBy(() -> cinemaHallService.updateHall(1L, request))
				.isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deleteHall_Success() {
		when(hallRepository.existsById(1L)).thenReturn(true);
		doNothing().when(hallRepository).deleteById(1L);

		cinemaHallService.deleteHall(1L);

		verify(hallRepository).deleteById(1L);
	}

	@Test
	void deleteHall_WhenNotFound_ShouldThrowException() {
		when(hallRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> cinemaHallService.deleteHall(1L)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void getAllHalls_Success() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);

		CinemaHallResponse response = CinemaHallResponse.builder().id(1L).name("Hall A").build();

		when(hallRepository.findAll()).thenReturn(Collections.singletonList(cinemaHall));
		when(hallMapper.toCinemaHallResponseList(Collections.singletonList(cinemaHall)))
				.thenReturn(Collections.singletonList(response));

		List<CinemaHallResponse> result = cinemaHallService.getAllHalls();

		assertThat(result).hasSize(1);
	}

	@Test
	void getHallWithSeats_Success() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Hall A");

		List<Seat> seats = Arrays.asList(Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).build(),
				Seat.builder().id(2L).row(1).number(2).seatType(SeatType.STANDARD).build());
		cinemaHall.setSeats(seats);

		List<SeatResponse> seatResponses = Arrays.asList(SeatResponse.builder().id(1L).build(),
				SeatResponse.builder().id(2L).build());

		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.of(cinemaHall));
		when(seatMapper.toSeatResponseList(seats)).thenReturn(seatResponses);

		CinemaHallWithSeatsResponse result = cinemaHallService.getHallWithSeats(1L);

		assertThat(result.getName()).isEqualTo("Hall A");
		assertThat(result.getCapacity()).isEqualTo(2);
	}

	@Test
	void getHallLayout_Success() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);
		cinemaHall.setName("Hall A");

		List<Seat> seats = Arrays.asList(Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).build(),
				Seat.builder().id(2L).row(1).number(2).seatType(SeatType.STANDARD).build());
		cinemaHall.setSeats(seats);

		List<SeatResponse> seatResponses = Arrays.asList(SeatResponse.builder().id(1L).build(),
				SeatResponse.builder().id(2L).build());

		when(hallRepository.findByIdWithSeats(1L)).thenReturn(Optional.of(cinemaHall));
		when(seatMapper.toSeatResponseList(anyList())).thenReturn(seatResponses);

		HallLayoutResponse result = cinemaHallService.getHallLayout(1L);

		assertThat(result.getHallName()).isEqualTo("Hall A");
		assertThat(result.getTotalSeats()).isEqualTo(2);
	}

	@Test
	void searchHalls_WithName() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);

		CinemaHallResponse response = CinemaHallResponse.builder().id(1L).name("Hall A").build();

		when(hallRepository.findByNameContainingIgnoreCase("Hall")).thenReturn(Collections.singletonList(cinemaHall));
		when(hallMapper.toCinemaHallResponseList(Collections.singletonList(cinemaHall)))
				.thenReturn(Collections.singletonList(response));

		List<CinemaHallResponse> result = cinemaHallService.searchHalls("Hall");

		assertThat(result).hasSize(1);
	}

	@Test
	void searchHalls_WithEmptyName() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);

		CinemaHallResponse response = CinemaHallResponse.builder().id(1L).name("Hall A").build();

		when(hallRepository.findAll()).thenReturn(Collections.singletonList(cinemaHall));
		when(hallMapper.toCinemaHallResponseList(Collections.singletonList(cinemaHall)))
				.thenReturn(Collections.singletonList(response));

		List<CinemaHallResponse> result = cinemaHallService.searchHalls("");

		assertThat(result).hasSize(1);
		verify(hallRepository).findAll();
	}

	@Test
	void getHallEntityById_Success() {
		CinemaHall cinemaHall = new CinemaHall();
		cinemaHall.setId(1L);

		when(hallRepository.findById(1L)).thenReturn(Optional.of(cinemaHall));

		CinemaHall result = cinemaHallService.getHallEntityById(1L);

		assertThat(result).isEqualTo(cinemaHall);
	}
}