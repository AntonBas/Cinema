package ua.lviv.bas.cinema.service.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.BookingMapper;
import ua.lviv.bas.cinema.repository.booking.BookingRepository;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketTypeRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.common.PriceCalculatorService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

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
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private TicketTypeRepository ticketTypeRepository;
    @Mock
    private SeatReservationRepository seatReservationRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private BonusService bonusService;
    @Mock
    private PriceCalculatorService priceCalculator;
    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private BookingService bookingService;

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
    private BookingResponse bookingResponse;

    private static final Long USER_ID = 1L;
    private static final Long SESSION_ID = 2L;
    private static final Long SEAT_ID_1 = 3L;
    private static final Long SEAT_ID_2 = 4L;
    private static final Long TICKET_TYPE_ADULT_ID = 5L;
    private static final Long TICKET_TYPE_CHILD_ID = 6L;
    private static final Long BOOKING_ID = 10L;
    private static final String BOOKING_NUMBER = "BK-20240115-00123";
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
        lenient().doNothing().when(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());

        ReflectionTestUtils.setField(bookingService, "expirationMinutes", EXPIRATION_MINUTES);
        ReflectionTestUtils.setField(bookingService, "tempHoldMinutes", TEMP_HOLD_MINUTES);
        ReflectionTestUtils.setField(bookingService, "sessionTooCloseMinutes", SESSION_TOO_CLOSE_MINUTES);

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

        bookingResponse = new BookingResponse(BOOKING_ID, BOOKING_NUMBER, BookingStatus.PENDING, SESSION_ID,
                sessionTime, "Test Movie", "Hall A", TOTAL_PRICE, BONUS_POINTS_USED, DISCOUNT_AMOUNT, FINAL_PRICE, null,
                sessionTime.plusMinutes(EXPIRATION_MINUTES), Collections.emptyList());
    }

    @Test
    void createBookingShouldSucceed() {
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
        when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
        when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
        when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
        when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);
        doNothing().when(bonusService).validatePointsForBooking(USER_ID, BONUS_POINTS_USED, TOTAL_PRICE);
        when(priceCalculator.calculateBonusDiscount(BONUS_POINTS_USED)).thenReturn(DISCOUNT_AMOUNT);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

        BookingResponse result = bookingService.createBooking(createRequest, testUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(BOOKING_ID);
        verify(bookingRepository).save(bookingCaptor.capture());
        verify(seatReservationRepository).saveAll(seatReservationsCaptor.capture());
        verify(bonusService).spendPoints(eq(USER_ID), eq(BONUS_POINTS_USED), any(Booking.class));

        Booking capturedBooking = bookingCaptor.getValue();
        assertThat(capturedBooking.getUser()).isEqualTo(testUser);
        assertThat(capturedBooking.getSession()).isEqualTo(testSession);
        assertThat(capturedBooking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void createBookingWhenNoExistingReservationShouldCreateNewOne() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_2,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.empty());
        when(seatRepository.findById(SEAT_ID_1)).thenReturn(Optional.of(testSeat1));
        when(seatRepository.findById(SEAT_ID_2)).thenReturn(Optional.of(testSeat2));
        when(seatReservationRepository.save(any(SeatReservation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.of(adultTicketType));
        when(ticketTypeRepository.findById(TICKET_TYPE_CHILD_ID)).thenReturn(Optional.of(childTicketType));
        when(priceCalculator.calculateSeatPrice(testSession, testSeat1, adultTicketType)).thenReturn(SEAT_1_PRICE);
        when(priceCalculator.calculateSeatPrice(testSession, testSeat2, childTicketType)).thenReturn(SEAT_2_PRICE);
        doNothing().when(bonusService).validatePointsForBooking(USER_ID, BONUS_POINTS_USED, TOTAL_PRICE);
        when(priceCalculator.calculateBonusDiscount(BONUS_POINTS_USED)).thenReturn(DISCOUNT_AMOUNT);

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(bookingMapper.toResponse(any(Booking.class))).thenReturn(bookingResponse);

        BookingResponse result = bookingService.createBooking(createRequest, testUser);

        assertThat(result).isNotNull();
        verify(seatReservationService).validateAvailability(SESSION_ID, SEAT_ID_1);
        verify(seatReservationService).validateAvailability(SESSION_ID, SEAT_ID_2);
    }

    @Test
    void createBookingWhenSessionNotFoundShouldThrowException() {
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
                .isInstanceOf(SessionNotFoundException.class);
    }

    @Test
    void createBookingWhenTicketTypeNotFoundShouldThrowException() {
        SeatReservation pendingReservation = SeatReservation.builder().id(1L).seat(testSeat1).session(testSession)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(TEMP_HOLD_MINUTES))
                .reservedByUser(testUser).build();

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(testSession));
        when(seatReservationRepository.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(SESSION_ID, SEAT_ID_1,
                ReservationStatus.PENDING, USER_ID)).thenReturn(Optional.of(pendingReservation));
        when(ticketTypeRepository.findById(TICKET_TYPE_ADULT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
                .isInstanceOf(TicketTypeNotFoundException.class);
    }

    @Test
    void getBookingShouldSucceed() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(savedBooking));
        when(bookingMapper.toResponse(savedBooking)).thenReturn(bookingResponse);

        BookingResponse result = bookingService.getBooking(BOOKING_ID, testUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(BOOKING_ID);
    }

    @Test
    void getBookingWhenNotFoundShouldThrowException() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBooking(BOOKING_ID, testUser))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void cancelBookingShouldSucceed() {
        Booking booking = Booking.builder().id(BOOKING_ID).user(testUser).status(BookingStatus.PENDING)
                .bonusPointsUsed(BONUS_POINTS_USED)
                .seatReservations(Arrays.asList(SeatReservation.builder().build(), SeatReservation.builder().build()))
                .build();

        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(seatReservationRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        bookingService.cancelBooking(BOOKING_ID, testUser);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bonusService).refundPoints(booking);
        verify(seatReservationRepository).saveAll(anyList());
    }

    @Test
    void cancelBookingWhenBookingNotFoundShouldThrowException() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, testUser))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void cancelBookingWhenCannotBeCancelledShouldThrowException() {
        Booking booking = Booking.builder().id(BOOKING_ID).user(testUser).status(BookingStatus.EXPIRED).build();

        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, testUser))
                .isInstanceOf(BookingValidationException.class);
    }

    @Test
    void confirmBookingShouldSucceed() {
        Booking booking = Booking.builder().id(BOOKING_ID).status(BookingStatus.PENDING)
                .seatReservations(Arrays.asList(SeatReservation.builder().build(), SeatReservation.builder().build()))
                .build();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);

        bookingService.confirmBooking(BOOKING_ID);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getSeatReservations().get(0).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(booking.getSeatReservations().get(1).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirmBookingWhenBookingNotFoundShouldThrowException() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmBooking(BOOKING_ID))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void confirmBookingWhenNotPendingShouldThrowException() {
        Booking booking = Booking.builder().id(BOOKING_ID).status(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(BOOKING_ID))
                .isInstanceOf(BookingOperationException.class);
    }
}