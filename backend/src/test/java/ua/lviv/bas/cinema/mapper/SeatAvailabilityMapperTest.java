package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse;

@ExtendWith(MockitoExtension.class)
public class SeatAvailabilityMapperTest {

	private SeatAvailabilityMapper seatAvailabilityMapper = new SeatAvailabilityMapperImpl();

	@Test
	void toSeatAvailabilityInfo_ShouldMapAllBasicFields() {
		Seat seat = Seat.builder().id(1L).row(5).number(12).seatType(SeatType.VIP).active(true).build();

		SeatAvailabilityResponse.SeatInfo seatInfo = seatAvailabilityMapper.toSeatAvailabilityInfo(seat);

		assertNotNull(seatInfo);
		assertEquals(1L, seatInfo.getId());
		assertEquals(5, seatInfo.getRow());
		assertEquals(12, seatInfo.getSeatNumber());
		assertEquals("VIP", seatInfo.getSeatType());

		assertNull(seatInfo.getAvailable());
		assertNull(seatInfo.getTemporarilyReserved());
		assertNull(seatInfo.getTicketPrices());
	}

	@Test
	void toSeatAvailabilityInfo_ShouldMapDifferentSeatTypes() {
		Seat seatStandard = Seat.builder().id(2L).row(3).number(7).seatType(SeatType.STANDARD).active(true).build();

		Seat seatCouple = Seat.builder().id(3L).row(4).number(8).seatType(SeatType.COUPLE).active(true).build();

		Seat seatVip = Seat.builder().id(4L).row(6).number(15).seatType(SeatType.VIP).active(true).build();

		SeatAvailabilityResponse.SeatInfo info1 = seatAvailabilityMapper.toSeatAvailabilityInfo(seatStandard);
		SeatAvailabilityResponse.SeatInfo info2 = seatAvailabilityMapper.toSeatAvailabilityInfo(seatCouple);
		SeatAvailabilityResponse.SeatInfo info3 = seatAvailabilityMapper.toSeatAvailabilityInfo(seatVip);

		assertNotNull(info1);
		assertEquals("STANDARD", info1.getSeatType());
		assertEquals(2L, info1.getId());
		assertEquals(3, info1.getRow());
		assertEquals(7, info1.getSeatNumber());

		assertNotNull(info2);
		assertEquals("COUPLE", info2.getSeatType());
		assertEquals(3L, info2.getId());
		assertEquals(4, info2.getRow());
		assertEquals(8, info2.getSeatNumber());

		assertNotNull(info3);
		assertEquals("VIP", info3.getSeatType());
		assertEquals(4L, info3.getId());
		assertEquals(6, info3.getRow());
		assertEquals(15, info3.getSeatNumber());

		assertNull(info1.getAvailable());
		assertNull(info1.getTemporarilyReserved());
		assertNull(info1.getTicketPrices());
	}

	@Test
	void toSeatAvailabilityInfo_ShouldHandleNullSeat() {
		SeatAvailabilityResponse.SeatInfo seatInfo = seatAvailabilityMapper.toSeatAvailabilityInfo(null);
		assertNull(seatInfo);
	}

	@Test
	void toSeatAvailabilityInfo_ShouldHandleSeatWithNullValues() {
		Seat seat = Seat.builder().id(null).row(null).number(null).seatType(null).active(true).build();

		SeatAvailabilityResponse.SeatInfo seatInfo = seatAvailabilityMapper.toSeatAvailabilityInfo(seat);

		assertNotNull(seatInfo);
		assertNull(seatInfo.getId());
		assertNull(seatInfo.getRow());
		assertNull(seatInfo.getSeatNumber());
		assertNull(seatInfo.getSeatType());
		assertNull(seatInfo.getAvailable());
		assertNull(seatInfo.getTemporarilyReserved());
		assertNull(seatInfo.getTicketPrices());
	}

	@Test
	void toSeatAvailabilityInfo_ShouldHandleInactiveSeat() {
		Seat seat = Seat.builder().id(5L).row(2).number(4).seatType(SeatType.STANDARD).active(false).build();

		SeatAvailabilityResponse.SeatInfo seatInfo = seatAvailabilityMapper.toSeatAvailabilityInfo(seat);

		assertNotNull(seatInfo);
		assertEquals(5L, seatInfo.getId());
		assertEquals(2, seatInfo.getRow());
		assertEquals(4, seatInfo.getSeatNumber());
		assertEquals("STANDARD", seatInfo.getSeatType());
		assertNull(seatInfo.getAvailable());
		assertNull(seatInfo.getTemporarilyReserved());
		assertNull(seatInfo.getTicketPrices());
	}

	@Test
	void toSeatAvailabilityInfo_ShouldIgnoreActiveField() {
		Seat seatActive = Seat.builder().id(6L).row(1).number(1).seatType(SeatType.VIP).active(true).build();

		Seat seatInactive = Seat.builder().id(7L).row(1).number(2).seatType(SeatType.VIP).active(false).build();

		SeatAvailabilityResponse.SeatInfo info1 = seatAvailabilityMapper.toSeatAvailabilityInfo(seatActive);
		SeatAvailabilityResponse.SeatInfo info2 = seatAvailabilityMapper.toSeatAvailabilityInfo(seatInactive);

		assertNotNull(info1);
		assertEquals(6L, info1.getId());
		assertEquals("VIP", info1.getSeatType());
		assertNull(info1.getAvailable());

		assertNotNull(info2);
		assertEquals(7L, info2.getId());
		assertEquals("VIP", info2.getSeatType());
		assertNull(info2.getAvailable());
	}

	@Test
	void toSeatAvailabilityInfo_ShouldMapSeatWithMaxValues() {
		Seat seat = Seat.builder().id(Long.MAX_VALUE).row(Integer.MAX_VALUE).number(Integer.MAX_VALUE)
				.seatType(SeatType.VIP).active(true).build();

		SeatAvailabilityResponse.SeatInfo seatInfo = seatAvailabilityMapper.toSeatAvailabilityInfo(seat);

		assertNotNull(seatInfo);
		assertEquals(Long.MAX_VALUE, seatInfo.getId());
		assertEquals(Integer.MAX_VALUE, seatInfo.getRow());
		assertEquals(Integer.MAX_VALUE, seatInfo.getSeatNumber());
		assertEquals("VIP", seatInfo.getSeatType());
		assertNull(seatInfo.getAvailable());
		assertNull(seatInfo.getTemporarilyReserved());
		assertNull(seatInfo.getTicketPrices());
	}

	@Test
	void toSeatAvailabilityInfo_ShouldMapSeatWithMinValues() {
		Seat seat = Seat.builder().id(1L).row(1).number(1).seatType(SeatType.STANDARD).active(true).build();

		SeatAvailabilityResponse.SeatInfo seatInfo = seatAvailabilityMapper.toSeatAvailabilityInfo(seat);

		assertNotNull(seatInfo);
		assertEquals(1L, seatInfo.getId());
		assertEquals(1, seatInfo.getRow());
		assertEquals(1, seatInfo.getSeatNumber());
		assertEquals("STANDARD", seatInfo.getSeatType());
		assertNull(seatInfo.getAvailable());
		assertNull(seatInfo.getTemporarilyReserved());
		assertNull(seatInfo.getTicketPrices());
	}

	@Test
	void toSeatAvailabilityInfo_ShouldOnlyMapBasicFieldsAndIgnoreOthers() {
		Seat seat = Seat.builder().id(10L).row(8).number(16).seatType(SeatType.COUPLE).active(true).build();

		SeatAvailabilityResponse.SeatInfo seatInfo = seatAvailabilityMapper.toSeatAvailabilityInfo(seat);

		assertNotNull(seatInfo);

		assertEquals(10L, seatInfo.getId());
		assertEquals(8, seatInfo.getRow());
		assertEquals(16, seatInfo.getSeatNumber());
		assertEquals("COUPLE", seatInfo.getSeatType());

		assertNull(seatInfo.getAvailable());
		assertNull(seatInfo.getTemporarilyReserved());
		assertNull(seatInfo.getTicketPrices());
	}
}