package ua.lviv.bas.cinema.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.booking.dto.request.BookingCreateRequest;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.bonus.service.BonusQueryService;
import ua.lviv.bas.cinema.common.PriceCalculatorService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCreationServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private BonusQueryService bonusQueryService;
    @Mock
    private PriceCalculatorService priceCalculator;
    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private BookingCreationService bookingCreationService;

    @Captor
    private ArgumentCaptor<Booking> bookingCaptor;
    @Captor
    private ArgumentCaptor<List<SeatReservation>> seatReservationsCaptor;

    private User testUser;
    private Session testSession;
    private Seat testSeat1;
    private Seat testSeat2;
    private TicketType adultTicketType;
    private TicketType childTicketType;
    private BookingCreateRequest createRequest;
    private Booking savedBooking;

    private static final Long USER_ID = 1L;
    private static final Long SESSION_ID = 2L;
    private static final Long SEAT_ID_1 = 3L;
    private static final Long SEAT_ID_2 = 4L;
    private static final Long TICKET_TYPE_ADULT_ID = 5L;
    private static final Long TICKET_TYPE_CHILD_ID = 6L;
    private static final Long BOOKING_ID = 10L;
    private static final BigDecimal BASE_PRICE = new BigDecimal("200.00");
    private static final BigDecimal SEAT_1_PRICE = new BigDecimal("200.00");
    private static final BigDecimal SEAT_2_PRICE = new BigDecimal("280.00");
    private static final BigDecimal TOTAL_PRICE = new BigDecimal("480.00");
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal FINAL_PRICE = new BigDecimal("380.00");
    private static final Integer BONUS_POINTS_USED = 100;
    private static final int EXPIRATION_MINUTES = 20;
    private static final int TEMP_HOLD_MINUTES = 5;
    private static final int SESSION_TOO_CLOSE_MINUTES = 30;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bookingCreationService, "expirationMinutes", EXPIRATION_MINUTES);
        ReflectionTestUtils.setField(bookingCreationService, "tempHoldMinutes", TEMP_HOLD_MINUTES);
        ReflectionTestUtils.setField(bookingCreationService, "sessionTooCloseMinutes", SESSION_TOO_CLOSE_MINUTES);

        testUser = User.builder().id(USER_ID).email("test@example.com").build();

        Movie movie = Movie.builder().id(100L).title("Test Movie").durationMinutes(120).build();
        CinemaHall hall = CinemaHall.builder().id(200L).name("Hall A").build();
        LocalDateTime sessionTime = LocalDateTime.now().plusHours(2);

        testSession = Session.builder().id(SESSION_ID).movie(movie).hall(hall).basePrice(BASE_PRICE)
                .status(CinemaSessionStatus.SCHEDULED).startTime(sessionTime).build();

        testSeat1 = Seat.builder().id(SEAT_ID_1).row(1).number(1).seatType(SeatType.STANDARD).active(true).build();
        testSeat2 = Seat.builder().id(SEAT_ID_2).row(1).number(2).seatType(SeatType.VIP).active(true).build();

        adultTicketType = TicketType.builder().id(TICKET_TYPE_ADULT_ID).displayName("Adult").build();
        childTicketType = TicketType.builder().id(TICKET_TYPE_CHILD_ID).displayName("Child").build();

        BookingCreateRequest.SeatSelectionRequest seatSelection1 = new BookingCreateRequest.SeatSelectionRequest(
                SEAT_ID_1, TICKET_TYPE_ADULT_ID);
        BookingCreateRequest.SeatSelectionRequest seatSelection2 = new BookingCreateRequest.SeatSelectionRequest(
                SEAT_ID_2, TICKET_TYPE_CHILD_ID);

        createRequest = new BookingCreateRequest(SESSION_ID, Arrays.asList(seatSelection1, seatSelection2),
                BONUS_POINTS_USED);

        savedBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
                .status(BookingStatus.PENDING).totalPrice(TOTAL_PRICE).bonusPointsUsed(BONUS_POINTS_USED)
                .bonusDiscountAmount(DISCOUNT_AMOUNT).finalPrice(FINAL_PRICE)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)).build();
    }

    @Test
    void createAndPersistShouldSucceed() {
        SeatReservation pendingReservation1 = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();
        SeatReservation pendingReservation2 = SeatReservation.builder().id(2L).seat(testSeat2).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation1));
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation2));
        when(ticketTypeRepository.findAllById(List.of(TICKET_TYPE_ADULT_ID, TICKET_TYPE_CHILD_ID)))
                .thenReturn(List.of(adultTicketType, childTicketType));
        when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
        when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);
        doNothing().when(bonusQueryService).validatePointsForBooking(USER_ID, BONUS_POINTS_USED, TOTAL_PRICE);
        when(priceCalculator.calculateBonusDiscount(BONUS_POINTS_USED)).thenReturn(DISCOUNT_AMOUNT);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        Booking result = bookingCreationService.createAndPersist(createRequest, testUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(BOOKING_ID);
        verify(bookingRepository).save(bookingCaptor.capture());
        verify(seatReservationRepository).saveAll(seatReservationsCaptor.capture());

        Booking capturedBooking = bookingCaptor.getValue();
        assertThat(capturedBooking.getUser()).isEqualTo(testUser);
        assertThat(capturedBooking.getSession()).isEqualTo(testSession);
        assertThat(capturedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(capturedBooking.getBonusPointsUsed()).isEqualTo(BONUS_POINTS_USED);
    }

    @Test
    void createAndPersistWhenNoExistingReservationShouldCreateNewOne() {
        SeatReservation newReservation1 = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();
        SeatReservation newReservation2 = SeatReservation.builder().id(2L).seat(testSeat2).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
        when(seatReservationService.lockSeat(SEAT_ID_1)).thenReturn(testSeat1);
        when(seatReservationService.lockSeat(SEAT_ID_2)).thenReturn(testSeat2);
        when(seatReservationService.holdLockedSeat(testSession, testSeat1, testUser)).thenReturn(newReservation1);
        when(seatReservationService.holdLockedSeat(testSession, testSeat2, testUser)).thenReturn(newReservation2);
        when(ticketTypeRepository.findAllById(List.of(TICKET_TYPE_ADULT_ID, TICKET_TYPE_CHILD_ID)))
                .thenReturn(List.of(adultTicketType, childTicketType));
        when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
        when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);
        doNothing().when(bonusQueryService).validatePointsForBooking(USER_ID, BONUS_POINTS_USED, TOTAL_PRICE);
        when(priceCalculator.calculateBonusDiscount(BONUS_POINTS_USED)).thenReturn(DISCOUNT_AMOUNT);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        Booking result = bookingCreationService.createAndPersist(createRequest, testUser);

        assertThat(result).isNotNull();
        verify(seatReservationService).holdLockedSeat(testSession, testSeat1, testUser);
        verify(seatReservationService).holdLockedSeat(testSession, testSeat2, testUser);
    }

    @Test
    void createAndPersistShouldLockSeatsInAscendingIdOrderRegardlessOfRequestOrder() {
        BookingCreateRequest.SeatSelectionRequest reversedSelection1 = new BookingCreateRequest.SeatSelectionRequest(
                SEAT_ID_2, TICKET_TYPE_CHILD_ID);
        BookingCreateRequest.SeatSelectionRequest reversedSelection2 = new BookingCreateRequest.SeatSelectionRequest(
                SEAT_ID_1, TICKET_TYPE_ADULT_ID);
        var reversedRequest = new BookingCreateRequest(SESSION_ID,
                Arrays.asList(reversedSelection1, reversedSelection2), BONUS_POINTS_USED);

        SeatReservation newReservation1 = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();
        SeatReservation newReservation2 = SeatReservation.builder().id(2L).seat(testSeat2).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
        when(seatReservationService.lockSeat(SEAT_ID_1)).thenReturn(testSeat1);
        when(seatReservationService.lockSeat(SEAT_ID_2)).thenReturn(testSeat2);
        when(seatReservationService.holdLockedSeat(testSession, testSeat1, testUser)).thenReturn(newReservation1);
        when(seatReservationService.holdLockedSeat(testSession, testSeat2, testUser)).thenReturn(newReservation2);
        when(ticketTypeRepository.findAllById(List.of(TICKET_TYPE_ADULT_ID, TICKET_TYPE_CHILD_ID)))
                .thenReturn(List.of(adultTicketType, childTicketType));
        when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
        when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);
        doNothing().when(bonusQueryService).validatePointsForBooking(USER_ID, BONUS_POINTS_USED, TOTAL_PRICE);
        when(priceCalculator.calculateBonusDiscount(BONUS_POINTS_USED)).thenReturn(DISCOUNT_AMOUNT);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        bookingCreationService.createAndPersist(reversedRequest, testUser);

        var inOrder = inOrder(seatReservationService);
        inOrder.verify(seatReservationService).lockSeat(SEAT_ID_1);
        inOrder.verify(seatReservationService).lockSeat(SEAT_ID_2);
    }

    @Test
    void createAndPersistWhenSessionNotFoundShouldThrowException() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingCreationService.createAndPersist(createRequest, testUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createAndPersistWhenTicketTypeNotFoundShouldThrowException() {
        SeatReservation pendingReservation = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation));
        when(ticketTypeRepository.findAllById(List.of(TICKET_TYPE_ADULT_ID, TICKET_TYPE_CHILD_ID)))
                .thenReturn(List.of(childTicketType));

        assertThatThrownBy(() -> bookingCreationService.createAndPersist(createRequest, testUser))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createAndPersistWhenDuplicateSeatIdShouldThrowException() {
        var duplicateSelection1 = new BookingCreateRequest.SeatSelectionRequest(SEAT_ID_1, TICKET_TYPE_ADULT_ID);
        var duplicateSelection2 = new BookingCreateRequest.SeatSelectionRequest(SEAT_ID_1, TICKET_TYPE_ADULT_ID);
        var duplicateRequest = new BookingCreateRequest(SESSION_ID,
                Arrays.asList(duplicateSelection1, duplicateSelection2), BONUS_POINTS_USED);

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));

        assertThatThrownBy(() -> bookingCreationService.createAndPersist(duplicateRequest, testUser))
                .isInstanceOf(BookingValidationException.class);

        verifyNoInteractions(seatReservationService);
        verify(bookingRepository, never()).save(any());
    }
}
