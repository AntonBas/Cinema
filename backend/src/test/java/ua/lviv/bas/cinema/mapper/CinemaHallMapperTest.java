package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;

class CinemaHallMapperTest {

	private CinemaHallMapper cinemaHallMapper;
	private CinemaHall cinemaHallWithSeats;
	private CinemaHall cinemaHallEmptySeats;
	private CinemaHall cinemaHallNullSeats;

	@BeforeEach
	void setUp() {
		cinemaHallMapper = Mappers.getMapper(CinemaHallMapper.class);

		cinemaHallWithSeats = CinemaHall.builder().id(1L).name("Hall A").build();

		Seat seat1 = Seat.builder().id(1L).row(1).number(1).hall(cinemaHallWithSeats).build();
		Seat seat2 = Seat.builder().id(2L).row(1).number(2).hall(cinemaHallWithSeats).build();
		Seat seat3 = Seat.builder().id(3L).row(2).number(1).hall(cinemaHallWithSeats).build();
		cinemaHallWithSeats.setSeats(Arrays.asList(seat1, seat2, seat3));

		cinemaHallEmptySeats = CinemaHall.builder().id(2L).name("Hall B").build();
		cinemaHallEmptySeats.setSeats(Collections.emptyList());

		cinemaHallNullSeats = CinemaHall.builder().id(3L).name("Hall C").build();
		cinemaHallNullSeats.setSeats(null);
	}

	@Test
	void toDto_ShouldMapAllFieldsCorrectly_WhenHallHasSeats() {
		CinemaHallResponse dto = cinemaHallMapper.toDto(cinemaHallWithSeats);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getName()).isEqualTo("Hall A");
		assertThat(dto.getCapacity()).isEqualTo(3);
	}

	@Test
	void toDto_ShouldReturnZeroCapacity_WhenSeatsListIsEmpty() {
		CinemaHallResponse dto = cinemaHallMapper.toDto(cinemaHallEmptySeats);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(2L);
		assertThat(dto.getName()).isEqualTo("Hall B");
		assertThat(dto.getCapacity()).isZero();
	}

	@Test
	void toDto_ShouldReturnZeroCapacity_WhenSeatsListIsNull() {
		CinemaHallResponse dto = cinemaHallMapper.toDto(cinemaHallNullSeats);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(3L);
		assertThat(dto.getName()).isEqualTo("Hall C");
		assertThat(dto.getCapacity()).isZero();
	}

	@Test
	void toDto_ShouldHandleNullInput() {
		CinemaHallResponse dto = cinemaHallMapper.toDto(null);

		assertThat(dto).isNull();
	}

	@Test
	void toDto_ShouldMapHallWithoutBuilder() {
		CinemaHall hall = new CinemaHall();
		hall.setId(4L);
		hall.setName("Hall D");
		hall.setSeats(Collections.emptyList());

		CinemaHallResponse dto = cinemaHallMapper.toDto(hall);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(4L);
		assertThat(dto.getName()).isEqualTo("Hall D");
		assertThat(dto.getCapacity()).isZero();
	}

	@Test
	void toDtoList_ShouldMapListOfHalls() {
		List<CinemaHall> halls = Arrays.asList(cinemaHallWithSeats, cinemaHallEmptySeats);

		List<CinemaHallResponse> dtos = cinemaHallMapper.toDtoList(halls);

		assertThat(dtos).isNotNull().hasSize(2);

		assertThat(dtos.get(0))
				.extracting(CinemaHallResponse::getId, CinemaHallResponse::getName, CinemaHallResponse::getCapacity)
				.containsExactly(1L, "Hall A", 3);

		assertThat(dtos.get(1))
				.extracting(CinemaHallResponse::getId, CinemaHallResponse::getName, CinemaHallResponse::getCapacity)
				.containsExactly(2L, "Hall B", 0);
	}

	@Test
	void toDtoList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<CinemaHall> halls = Collections.emptyList();

		List<CinemaHallResponse> dtos = cinemaHallMapper.toDtoList(halls);

		assertThat(dtos).isNotNull().isEmpty();
	}

	@Test
	void toDtoList_ShouldReturnNull_WhenInputIsNull() {
		List<CinemaHallResponse> dtos = cinemaHallMapper.toDtoList(null);

		assertThat(dtos).isNull();
	}

	@Test
	void toDtoList_ShouldHandleSingleElementList() {
		List<CinemaHall> halls = Collections.singletonList(cinemaHallWithSeats);

		List<CinemaHallResponse> dtos = cinemaHallMapper.toDtoList(halls);

		assertThat(dtos).isNotNull().hasSize(1).first()
				.extracting(CinemaHallResponse::getId, CinemaHallResponse::getName).containsExactly(1L, "Hall A");
	}

	@Test
	void toDtoList_ShouldHandleListWithNullElements() {
		List<CinemaHall> halls = Arrays.asList(cinemaHallWithSeats, null, cinemaHallEmptySeats);

		List<CinemaHallResponse> dtos = cinemaHallMapper.toDtoList(halls);

		assertThat(dtos).isNotNull().hasSize(3);

		assertThat(dtos.get(0)).isNotNull();
		assertThat(dtos.get(1)).isNull();
		assertThat(dtos.get(2)).isNotNull();
	}

	@Test
	void toDto_ShouldVerifyAllResponseFieldsAreMapped() {
		CinemaHall hall = CinemaHall.builder().id(5L).name("Test Hall").build();
		hall.setSeats(Collections.emptyList());

		CinemaHallResponse dto = cinemaHallMapper.toDto(hall);

		assertThat(dto)
				.extracting(CinemaHallResponse::getId, CinemaHallResponse::getName, CinemaHallResponse::getCapacity)
				.containsExactly(5L, "Test Hall", 0);
	}

	@Test
	void toDto_ShouldMapHallWithSpecialCharactersInName() {
		CinemaHall hall = CinemaHall.builder().id(6L).name("Hall & Center (VIP)").build();
		hall.setSeats(Collections.emptyList());

		CinemaHallResponse dto = cinemaHallMapper.toDto(hall);

		assertThat(dto).isNotNull();
		assertThat(dto.getName()).isEqualTo("Hall & Center (VIP)");
	}

	@Test
	void toDto_ShouldMapHallWithLongName() {
		String longName = "A".repeat(100);
		CinemaHall hall = CinemaHall.builder().id(7L).name(longName).build();

		CinemaHallResponse dto = cinemaHallMapper.toDto(hall);

		assertThat(dto).isNotNull();
		assertThat(dto.getName()).isEqualTo(longName);
	}

	@Test
	void toDto_ShouldHandleMaxIdValue() {
		CinemaHall hall = CinemaHall.builder().id(Long.MAX_VALUE).name("Max Hall").build();

		CinemaHallResponse dto = cinemaHallMapper.toDto(hall);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(Long.MAX_VALUE);
	}
}