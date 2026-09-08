package ua.lviv.bas.cinema.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.booking.dto.request.BookingCreateRequest;
import ua.lviv.bas.cinema.booking.dto.response.BookingResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingConcurrentModificationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.booking.mapper.BookingMapper;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.audit.service.AuditService;

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
    private BookingMapper bookingMapper;
    @Mock
    private BonusLedgerService bonusLedgerService;
    @Mock
    private BookingCreationService bookingCreationService;
    @Mock
    private SeatReservationService seatReservationService;
    @Mock
    private AuditService auditService;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private BookingService bookingService;

    private User testUser;
    private Session testSession;
    private BookingCreateRequest createRequest;
    private Booking savedBooking;
    private BookingResponse bookingResponse;

    private static final Long USER_ID = 1L;
    private static final Long SESSION_ID = 2L;
    private static final Long SEAT_ID_1 = 3L;
    private static final Long TICKET_TYPE_ADULT_ID = 5L;
    private static final Long BOOKING_ID = 10L;
    private static final String BOOKING_NUMBER = "BK-20240115-00123";
    private static final BigDecimal TOTAL_PRICE = new BigDecimal("480.00");
    private static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("100.00");
    private static final BigDecimal FINAL_PRICE = new BigDecimal("380.00");
    private static final Integer BONUS_POINTS_USED = 100;
    private static final int EXPIRATION_MINUTES = 20;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());

        testUser = User.builder().id(USER_ID).email("test@example.com").build();

        LocalDateTime sessionTime = LocalDateTime.now().plusHours(2);
        testSession = Session.builder().id(SESSION_ID).startTime(sessionTime).build();

        var seatSelection1 = new BookingCreateRequest.SeatSelectionRequest(SEAT_ID_1, TICKET_TYPE_ADULT_ID);
        createRequest = new BookingCreateRequest(SESSION_ID, List.of(seatSelection1), BONUS_POINTS_USED);

        savedBooking = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
                .status(BookingStatus.PENDING).totalPrice(TOTAL_PRICE).bonusPointsUsed(BONUS_POINTS_USED)
                .bonusDiscountAmount(DISCOUNT_AMOUNT).finalPrice(FINAL_PRICE)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)).build();

        bookingResponse = new BookingResponse(BOOKING_ID, BOOKING_NUMBER, BookingStatus.PENDING, SESSION_ID,
                sessionTime, "Test Movie", "Hall A", TOTAL_PRICE, BONUS_POINTS_USED, DISCOUNT_AMOUNT, FINAL_PRICE, null,
                sessionTime.plusMinutes(EXPIRATION_MINUTES), Collections.emptyList());
    }

    @Test
    void createBookingShouldPersistViaCreationServiceThenSpendBonusPoints() {
        when(bookingCreationService.createAndPersist(createRequest, testUser)).thenReturn(savedBooking);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(savedBooking));
        when(bookingMapper.toResponse(savedBooking)).thenReturn(bookingResponse);

        BookingResponse result = bookingService.createBooking(createRequest, testUser);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(BOOKING_ID);

        var inOrder = inOrder(bookingCreationService, bookingRepository, bonusLedgerService);
        inOrder.verify(bookingCreationService).createAndPersist(createRequest, testUser);
        inOrder.verify(bookingRepository).findById(BOOKING_ID);
        inOrder.verify(bonusLedgerService).spendPoints(USER_ID, BONUS_POINTS_USED, savedBooking);
    }

    @Test
    void createBookingWithoutBonusPointsShouldNotSpendPoints() {
        var bookingWithoutBonus = Booking.builder().id(BOOKING_ID).user(testUser).session(testSession)
                .status(BookingStatus.PENDING).totalPrice(TOTAL_PRICE).bonusPointsUsed(0).finalPrice(TOTAL_PRICE)
                .build();
        var requestWithoutBonus = new BookingCreateRequest(SESSION_ID,
                List.of(new BookingCreateRequest.SeatSelectionRequest(SEAT_ID_1, TICKET_TYPE_ADULT_ID)), 0);

        when(bookingCreationService.createAndPersist(requestWithoutBonus, testUser)).thenReturn(bookingWithoutBonus);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(bookingWithoutBonus));
        when(bookingMapper.toResponse(bookingWithoutBonus)).thenReturn(bookingResponse);

        bookingService.createBooking(requestWithoutBonus, testUser);

        verifyNoInteractions(bonusLedgerService);
    }

    @Test
    void createBookingWhenSessionNotFoundShouldPropagateException() {
        when(bookingCreationService.createAndPersist(createRequest, testUser))
                .thenThrow(new EntityNotFoundException("Session", SESSION_ID));

        assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
                .isInstanceOf(EntityNotFoundException.class);

        verifyNoInteractions(bonusLedgerService);
    }

    @Test
    void createBookingWhenBonusSpendFailsShouldCancelBookingAndPropagateException() {
        var seatReservation = SeatReservation.builder().id(1L).status(ReservationStatus.CONFIRMED).build();
        savedBooking.getSeatReservations().add(seatReservation);

        when(bookingCreationService.createAndPersist(createRequest, testUser)).thenReturn(savedBooking);
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(savedBooking));
        doThrow(new InsufficientPointsException(0, BONUS_POINTS_USED)).when(bonusLedgerService)
                .spendPoints(USER_ID, BONUS_POINTS_USED, savedBooking);
        when(bookingRepository.saveAndFlush(savedBooking)).thenReturn(savedBooking);

        assertThatThrownBy(() -> bookingService.createBooking(createRequest, testUser))
                .isInstanceOf(InsufficientPointsException.class);

        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(savedBooking.getBonusPointsUsed()).isEqualTo(0);
        assertThat(savedBooking.getBonusDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(savedBooking.getFinalPrice()).isEqualByComparingTo(TOTAL_PRICE);
        verify(seatReservationService).releaseReservations(savedBooking.getSeatReservations(), SESSION_ID);
        verify(bookingRepository).saveAndFlush(savedBooking);
        verifyNoInteractions(bookingMapper);
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
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void cancelBookingShouldSucceed() {
        Booking booking = Booking.builder().id(BOOKING_ID).user(testUser).status(BookingStatus.PENDING)
                .session(testSession).bonusPointsUsed(BONUS_POINTS_USED)
                .seatReservations(Arrays.asList(SeatReservation.builder().build(), SeatReservation.builder().build()))
                .build();

        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.saveAndFlush(booking)).thenReturn(booking);

        bookingService.cancelBooking(BOOKING_ID, testUser);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(bonusLedgerService).refundPoints(booking);
        verify(seatReservationService).releaseReservations(booking.getSeatReservations(), SESSION_ID);
    }

    @Test
    void cancelBookingWhenOptimisticLockFailsShouldNotRefundBonusPoints() {
        Booking booking = Booking.builder().id(BOOKING_ID).user(testUser).status(BookingStatus.PENDING)
                .session(testSession).bonusPointsUsed(BONUS_POINTS_USED)
                .seatReservations(Arrays.asList(SeatReservation.builder().build(), SeatReservation.builder().build()))
                .build();

        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.saveAndFlush(booking))
                .thenThrow(new ObjectOptimisticLockingFailureException(Booking.class, BOOKING_ID));

        assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, testUser))
                .isInstanceOf(BookingConcurrentModificationException.class);

        verifyNoInteractions(bonusLedgerService);
    }

    @Test
    void cancelBookingWhenBookingNotFoundShouldThrowException() {
        when(bookingRepository.findByIdAndUserId(BOOKING_ID, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(BOOKING_ID, testUser))
                .isInstanceOf(EntityNotFoundException.class);
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
        Booking booking = Booking.builder().id(BOOKING_ID).status(BookingStatus.PENDING).session(testSession)
                .seatReservations(Arrays.asList(SeatReservation.builder().build(), SeatReservation.builder().build()))
                .build();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.saveAndFlush(booking)).thenReturn(booking);

        bookingService.confirmBooking(BOOKING_ID);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getSeatReservations().get(0).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(booking.getSeatReservations().get(1).getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(seatReservationService).evictAvailabilityCache(SESSION_ID);
    }

    @Test
    void confirmBookingWhenBookingNotFoundShouldThrowException() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmBooking(BOOKING_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void confirmBookingWhenNotPendingShouldThrowException() {
        Booking booking = Booking.builder().id(BOOKING_ID).status(BookingStatus.CANCELLED).build();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(BOOKING_ID))
                .isInstanceOf(BookingOperationException.class);
    }

    @Test
    void confirmBookingWhenAlreadyConfirmedShouldBeNoOp() {
        Booking booking = Booking.builder().id(BOOKING_ID).status(BookingStatus.CONFIRMED).build();

        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        bookingService.confirmBooking(BOOKING_ID);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingRepository, never()).saveAndFlush(any());
    }
}
