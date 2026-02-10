package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

	private final Long HALL_ID = 1L;
	private final String HALL_NAME = "Hall A";

	@Test
	void createHall_Success() {
		CinemaHallRequest request = CinemaHallRequest.builder().name(HALL_NAME).rows(5).seatsPerRow(10)
				.defaultSeatType(SeatType.STANDARD).build();

		CinemaHall cinemaHall = createCinemaHall();
		CinemaHallResponse response = createCinemaHallResponse();

		when(hallRepository.existsByName(HALL_NAME)).thenReturn(false);
		when(hallRepository.save(any(CinemaHall.class))).thenReturn(cinemaHall);
		when(hallMapper.toCinemaHallResponse(cinemaHall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.createHall(request);

		assertThat(result).isEqualTo(response);
		verify(hallRepository).save(any(CinemaHall.class));
	}

	@Test
	void createHall_DuplicateName_ThrowsException() {
		CinemaHallRequest request = CinemaHallRequest.builder().name(HALL_NAME).build();

		when(hallRepository.existsByName(HALL_NAME)).thenReturn(true);

		assertThatThrownBy(() -> cinemaHallService.createHall(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getHallById_Success() {
		CinemaHall cinemaHall = createCinemaHall();
		CinemaHallResponse response = createCinemaHallResponse();

		when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(cinemaHall));
		when(hallMapper.toCinemaHallResponse(cinemaHall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.getHallById(HALL_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getHallById_NotFound_ThrowsException() {
		when(hallRepository.findById(HALL_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cinemaHallService.getHallById(HALL_ID))
				.isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void updateHall_Success() {
		CinemaHall existingHall = createCinemaHall();
		existingHall.setName("Old Name");

		CinemaHallRequest request = CinemaHallRequest.builder().name("New Name").build();

		CinemaHallResponse response = createCinemaHallResponse();

		when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(existingHall));
		when(hallRepository.existsByName("New Name")).thenReturn(false);
		when(hallRepository.save(existingHall)).thenReturn(existingHall);
		when(hallMapper.toCinemaHallResponse(existingHall)).thenReturn(response);

		CinemaHallResponse result = cinemaHallService.updateHall(HALL_ID, request);

		assertThat(result).isEqualTo(response);
		assertThat(existingHall.getName()).isEqualTo("New Name");
	}

	@Test
	void updateHall_WithLayout_Success() {
		CinemaHall existingHall = createCinemaHall();

		CinemaHallRequest request = CinemaHallRequest.builder().name(HALL_NAME).rows(5).seatsPerRow(10)
				.defaultSeatType(SeatType.STANDARD).build();

		CinemaHallResponse response = createCinemaHallResponse();

		when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(existingHall));
		when(hallRepository.save(existingHall)).thenReturn(existingHall);
		when(hallMapper.toCinemaHallResponse(existingHall)).thenReturn(response);

		cinemaHallService.updateHall(HALL_ID, request);

		verify(seatRepository).deleteByHallId(HALL_ID);
		verify(seatRepository).saveAll(anyList());
	}

	@Test
	void updateHall_DuplicateName_ThrowsException() {
		CinemaHall existingHall = createCinemaHall();
		existingHall.setName("Old Name");

		CinemaHallRequest request = CinemaHallRequest.builder().name("Existing Name").build();

		when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(existingHall));
		when(hallRepository.existsByName("Existing Name")).thenReturn(true);

		assertThatThrownBy(() -> cinemaHallService.updateHall(HALL_ID, request))
				.isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deleteHall_Success() {
		when(hallRepository.existsById(HALL_ID)).thenReturn(true);

		cinemaHallService.deleteHall(HALL_ID);

		verify(hallRepository).deleteById(HALL_ID);
	}

	@Test
	void deleteHall_NotFound_ThrowsException() {
		when(hallRepository.existsById(HALL_ID)).thenReturn(false);

		assertThatThrownBy(() -> cinemaHallService.deleteHall(HALL_ID)).isInstanceOf(CinemaHallNotFoundException.class);
	}

	@Test
	void getAllOrSearchHalls_NoSearch_Success() {
		CinemaHall cinemaHall = createCinemaHall();
		CinemaHallResponse response = createCinemaHallResponse();

		when(hallRepository.findAll()).thenReturn(List.of(cinemaHall));
		when(hallMapper.toCinemaHallResponseList(List.of(cinemaHall))).thenReturn(List.of(response));

		List<CinemaHallResponse> result = cinemaHallService.getAllOrSearchHalls(null);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getAllOrSearchHalls_WithSearch_Success() {
		String searchName = "Hall";
		CinemaHall cinemaHall = createCinemaHall();
		CinemaHallResponse response = createCinemaHallResponse();

		when(hallRepository.findByNameContainingIgnoreCase(searchName)).thenReturn(List.of(cinemaHall));
		when(hallMapper.toCinemaHallResponseList(List.of(cinemaHall))).thenReturn(List.of(response));

		List<CinemaHallResponse> result = cinemaHallService.getAllOrSearchHalls(searchName);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getHallWithSeats_Success() {
		CinemaHall cinemaHall = createCinemaHall();
		Seat seat = Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).build();
		cinemaHall.setSeats(List.of(seat));

		SeatResponse seatResponse = SeatResponse.builder().id(1L).build();

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(cinemaHall));
		when(seatMapper.toSeatResponseList(List.of(seat))).thenReturn(List.of(seatResponse));

		CinemaHallWithSeatsResponse result = cinemaHallService.getHallWithSeats(HALL_ID);

		assertThat(result.getId()).isEqualTo(HALL_ID);
		assertThat(result.getName()).isEqualTo(HALL_NAME);
		assertThat(result.getSeats()).hasSize(1);
	}

	@Test
	void getHallLayout_Success() {
		CinemaHall cinemaHall = createCinemaHall();
		Seat seat1 = Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).build();
		Seat seat2 = Seat.builder().id(2L).row(1).number(2).seatType(SeatType.STANDARD).build();
		cinemaHall.setSeats(List.of(seat1, seat2));

		when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(cinemaHall));

		HallLayoutResponse result = cinemaHallService.getHallLayout(HALL_ID);

		assertThat(result.getHallId()).isEqualTo(HALL_ID);
		assertThat(result.getHallName()).isEqualTo(HALL_NAME);
		assertThat(result.getTotalSeats()).isEqualTo(2);
	}

	@Test
	void getHallEntityById_Success() {
		CinemaHall cinemaHall = createCinemaHall();

		when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(cinemaHall));

		CinemaHall result = cinemaHallService.getHallEntityById(HALL_ID);

		assertThat(result).isEqualTo(cinemaHall);
	}

	private CinemaHall createCinemaHall() {
		CinemaHall hall = new CinemaHall();
		hall.setId(HALL_ID);
		hall.setName(HALL_NAME);
		return hall;
	}

	private CinemaHallResponse createCinemaHallResponse() {
		return CinemaHallResponse.builder().id(HALL_ID).name(HALL_NAME).build();
	}
}