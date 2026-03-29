package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallHasSessionsException;
import ua.lviv.bas.cinema.exception.domain.cinema.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.repository.cinema.CinemaHallRepository;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallProjection;

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

	private final Long HALL_ID = 1L;
	private final String HALL_NAME = "Hall A";

	@Test
	void createHallShouldSaveNewHall() {
		CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, 5, 10, SeatType.STANDARD, null);

		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();

		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 0);

		when(hallRepository.existsByName(HALL_NAME)).thenReturn(false);
		when(hallRepository.save(any(CinemaHall.class))).thenReturn(hall);
		when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.createHall(request);

		assertThat(result.id()).isEqualTo(HALL_ID);
		assertThat(result.name()).isEqualTo(HALL_NAME);
	}

	@Test
	void createHallShouldThrowExceptionWhenNameExists() {
		CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, null, null, null, null);

		when(hallRepository.existsByName(HALL_NAME)).thenReturn(true);

		assertThatThrownBy(() -> cinemaHallService.createHall(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getHallByIdShouldReturnHall() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();

		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 0);

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
		when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.getHallById(HALL_ID);

		assertThat(result.id()).isEqualTo(HALL_ID);
	}

	@Test
	void getHallByIdShouldThrowExceptionWhenNotFound() {
		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cinemaHallService.getHallById(HALL_ID))
				.isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void getAllHallsShouldReturnList() {
		CinemaHallProjection projection = new CinemaHallProjection() {
			@Override
			public Long getId() {
				return HALL_ID;
			}

			@Override
			public String getName() {
				return HALL_NAME;
			}

			@Override
			public Long getSeatsCount() {
				return 50L;
			}
		};

		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 50);

		when(hallRepository.findAllProjected()).thenReturn(List.of(projection));
		when(hallMapper.toCinemaHallResponseListFromProjection(List.of(projection))).thenReturn(List.of(response));

		List<CinemaHallResponse> result = cinemaHallService.getAllHalls();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(HALL_ID);
	}

	@Test
	void updateHallShouldUpdateNameOnly() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name("Old Name").build();

		List<Seat> seats = new ArrayList<>();
		for (int row = 1; row <= 5; row++) {
			for (int num = 1; num <= 10; num++) {
				seats.add(Seat.builder().row(row).number(num).seatType(SeatType.STANDARD).hall(hall).build());
			}
		}
		hall.setSeats(seats);

		CinemaHallRequest request = new CinemaHallRequest("New Name", 5, 10, SeatType.STANDARD, null);

		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, "New Name", 0);

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
		when(hallRepository.existsByName("New Name")).thenReturn(false);
		when(hallRepository.save(hall)).thenReturn(hall);
		when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.updateHall(HALL_ID, request);

		assertThat(result.name()).isEqualTo("New Name");
		verify(seatRepository, never()).deleteByHallId(any());
		verify(seatRepository, never()).saveAll(any());
	}

	@Test
	void updateHallShouldUpdateLayoutWhenChanged() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
		hall.setSeats(List.of());

		CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, 5, 10, SeatType.STANDARD, null);

		CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 0);

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
		when(hallRepository.save(hall)).thenReturn(hall);
		when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);
		when(seatRepository.hasTicketsForHall(HALL_ID)).thenReturn(false);

		CinemaHallResponse result = cinemaHallService.updateHall(HALL_ID, request);

		assertThat(result.name()).isEqualTo(HALL_NAME);
		verify(seatRepository).deleteByHallId(HALL_ID);
		verify(seatRepository).saveAll(any());
	}

	@Test
	void updateHallShouldThrowExceptionWhenNameExists() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name("Old Name").build();

		CinemaHallRequest request = new CinemaHallRequest("Existing Name", null, null, null, null);

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
		when(hallRepository.existsByName("Existing Name")).thenReturn(true);

		assertThatThrownBy(() -> cinemaHallService.updateHall(HALL_ID, request))
				.isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void updateHallShouldThrowExceptionWhenHallHasFutureSessions() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).build();

		Session session = Session.builder().startTime(LocalDateTime.now().plusDays(1)).build();
		hall.setSessions(List.of(session));

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

		assertThatThrownBy(
				() -> cinemaHallService.updateHall(HALL_ID, new CinemaHallRequest(null, null, null, null, null)))
				.isInstanceOf(CinemaHallHasSessionsException.class);
	}

	@Test
	void deleteHallShouldDeleteHall() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

		cinemaHallService.deleteHall(HALL_ID);

		verify(hallRepository).delete(hall);
		verify(hallRepository, never()).deleteById(any());
	}

	@Test
	void deleteHallShouldThrowExceptionWhenNotFound() {
		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cinemaHallService.deleteHall(HALL_ID)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void deleteHallShouldThrowExceptionWhenHallHasFutureSessions() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();

		Session session = Session.builder().startTime(LocalDateTime.now().plusDays(1)).build();
		hall.setSessions(List.of(session));

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

		assertThatThrownBy(() -> cinemaHallService.deleteHall(HALL_ID))
				.isInstanceOf(CinemaHallHasSessionsException.class);
	}

	@Test
	void deleteHallShouldThrowExceptionWithHallName() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();

		Session session = Session.builder().startTime(LocalDateTime.now().plusDays(1)).build();
		hall.setSessions(List.of(session));

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

		assertThatThrownBy(() -> cinemaHallService.deleteHall(HALL_ID))
				.isInstanceOf(CinemaHallHasSessionsException.class).hasMessageContaining(HALL_NAME);
	}

	@Test
	void getHallLayoutShouldReturnLayout() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();

		List<Seat> seats = new ArrayList<>();
		for (int row = 1; row <= 5; row++) {
			for (int num = 1; num <= 10; num++) {
				seats.add(Seat.builder().row(row).number(num).seatType(SeatType.STANDARD).hall(hall).build());
			}
		}
		hall.setSeats(seats);

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

		HallLayoutResponse result = cinemaHallService.getHallLayout(HALL_ID);

		assertThat(result.hallId()).isEqualTo(HALL_ID);
		assertThat(result.hallName()).isEqualTo(HALL_NAME);
	}

	@Test
	void getHallEntityByIdShouldReturnHall() {
		CinemaHall hall = CinemaHall.builder().id(HALL_ID).build();

		when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));

		CinemaHall result = cinemaHallService.getHallEntityById(HALL_ID);

		assertThat(result.getId()).isEqualTo(HALL_ID);
	}
}