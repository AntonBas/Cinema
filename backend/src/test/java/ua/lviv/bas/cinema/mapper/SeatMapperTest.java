package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.SeatCreateDto;
import ua.lviv.bas.cinema.dto.SeatDto;

class SeatMapperTest {

    private final SeatMapper seatMapper = Mappers.getMapper(SeatMapper.class);

    @Test
    void toEntity_ShouldMapCreateDtoToEntity() {
        SeatCreateDto createDto = SeatCreateDto.builder()
                .row(2)
                .number(5)
                .seatType(SeatType.VIP)
                .build();

        Seat result = seatMapper.toEntity(createDto);

        assertNotNull(result);
        assertEquals(2, result.getRow());
        assertEquals(5, result.getNumber());
        assertEquals(SeatType.VIP, result.getSeatType());
        assertNull(result.getId());
        assertNull(result.getHall());
        assertNull(result.getTickets());
    }

    @Test
    void toDto_ShouldMapEntityToDto() {
        Seat seat = Seat.builder()
                .id(1L)
                .row(3)
                .number(7)
                .seatType(SeatType.STANDARD)
                .build();

        SeatDto result = seatMapper.toDto(seat);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(3, result.getRow());
        assertEquals(7, result.getNumber());
        assertEquals(SeatType.STANDARD, result.getSeatType());
    }
}