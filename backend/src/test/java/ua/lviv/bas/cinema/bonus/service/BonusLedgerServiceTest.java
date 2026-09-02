package ua.lviv.bas.cinema.bonus.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.bonus.domain.BonusCard;
import ua.lviv.bas.cinema.bonus.domain.BonusRules;
import ua.lviv.bas.cinema.bonus.domain.BonusTransaction;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.bonus.repository.BonusCardRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusRulesRepository;
import ua.lviv.bas.cinema.bonus.repository.BonusTransactionRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BonusLedgerServiceTest {

    @Mock
    private BonusCardRepository bonusCardRepository;
    @Mock
    private BonusRulesRepository bonusRulesRepository;
    @Mock
    private BonusTransactionRepository bonusTransactionRepository;
    @Mock
    private BonusQueryService bonusQueryService;
    @Mock
    private AuditService auditService;
    @InjectMocks
    private BonusLedgerService bonusLedgerService;
    @Captor
    private ArgumentCaptor<BonusCard> cardCaptor;
    @Captor
    private ArgumentCaptor<BonusTransaction> transactionCaptor;

    private final Long USER_ID = 1L;
    private final Long BOOKING_ID = 1L;
    private final Long PAYMENT_ID = 1L;
    private final BonusTransactionType WELCOME = BonusTransactionType.WELCOME_BONUS;
    private final BonusTransactionType BIRTHDAY = BonusTransactionType.BIRTHDAY_BONUS;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }

    @Test
    void awardWelcomeBonusWhenNotReceivedShouldAddPoints() {
        User user = User.builder().id(USER_ID).build();
        BonusCard card = BonusCard.builder().pointsBalance(0).welcomeBonusReceived(false).build();
        BonusRules rule = BonusRules.builder().points(50).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(WELCOME)).thenReturn(Optional.of(rule));
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusLedgerService.awardWelcomeBonus(user);

        assertThat(card.getPointsBalance()).isEqualTo(50);
        assertThat(card.isWelcomeBonusReceived()).isTrue();
        verify(bonusCardRepository).save(cardCaptor.capture());
        verify(bonusTransactionRepository).save(transactionCaptor.capture());

        BonusTransaction transaction = transactionCaptor.getValue();
        assertThat(transaction.getPointsChange()).isEqualTo(50);
        assertThat(transaction.getType()).isEqualTo(WELCOME);
    }

    @Test
    void awardWelcomeBonusWhenAlreadyReceivedShouldDoNothing() {
        User user = User.builder().id(USER_ID).build();
        BonusCard card = BonusCard.builder().welcomeBonusReceived(true).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        bonusLedgerService.awardWelcomeBonus(user);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void awardBirthdayBonusWhenBirthdayShouldAddPoints() {
        User user = User.builder().id(USER_ID).verificationStatus(VerificationStatus.VERIFIED)
                .dateOfBirth(LocalDate.now()).build();
        BonusCard card = BonusCard.builder().pointsBalance(0).lastBirthdayBonusDate(null).build();
        BonusRules rule = BonusRules.builder().points(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(BIRTHDAY)).thenReturn(Optional.of(rule));
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusLedgerService.awardBirthdayBonus(user);

        assertThat(card.getPointsBalance()).isEqualTo(100);
        assertThat(card.getLastBirthdayBonusDate()).isEqualTo(LocalDate.now());
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void awardBirthdayBonusWhenNotVerifiedShouldDoNothing() {
        User user = User.builder().verificationStatus(VerificationStatus.NOT_VERIFIED).build();

        bonusLedgerService.awardBirthdayBonus(user);

        verify(bonusCardRepository, never()).findByUserId(any());
    }

    @Test
    void awardBirthdayBonusWhenNoBirthDateShouldDoNothing() {
        User user = User.builder().verificationStatus(VerificationStatus.VERIFIED).dateOfBirth(null).build();

        bonusLedgerService.awardBirthdayBonus(user);

        verify(bonusCardRepository, never()).findByUserId(any());
    }

    @Test
    void awardBirthdayBonusWhenAlreadyReceivedThisYearShouldDoNothing() {
        User user = User.builder().id(USER_ID).verificationStatus(VerificationStatus.VERIFIED)
                .dateOfBirth(LocalDate.now()).build();
        BonusCard card = BonusCard.builder().pointsBalance(0).lastBirthdayBonusDate(LocalDate.now()).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        bonusLedgerService.awardBirthdayBonus(user);

        verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
        verify(bonusCardRepository, never()).save(any());
    }

    @Test
    void addPromotionPointsShouldAddPoints() {
        User user = User.builder().id(USER_ID).build();
        BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusLedgerService.addPromotionPoints(user, 50, "PROMO");

        assertThat(card.getPointsBalance()).isEqualTo(150);
        verify(bonusCardRepository).findByUserId(USER_ID);
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void addPromotionPointsWhenPointsInvalidShouldThrowException() {
        User user = User.builder().id(USER_ID).build();

        assertThatThrownBy(() -> bonusLedgerService.addPromotionPoints(user, 0, "PROMO"))
                .isInstanceOf(BonusValidationException.class);
        assertThatThrownBy(() -> bonusLedgerService.addPromotionPoints(user, null, "PROMO"))
                .isInstanceOf(BonusValidationException.class);
    }

    @Test
    void spendPointsShouldSucceed() {
        Booking booking = Booking.builder().id(BOOKING_ID).build();
        BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

        doNothing().when(bonusQueryService).validateRedemption(USER_ID, 30);
        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusLedgerService.spendPoints(USER_ID, 30, booking);

        assertThat(card.getPointsBalance()).isEqualTo(70);
        verify(bonusQueryService).validateRedemption(USER_ID, 30);
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void accruePointsForPaymentShouldSucceed() {
        Booking booking = Booking.builder().id(BOOKING_ID).build();
        Payment payment = Payment.builder().id(PAYMENT_ID).build();
        BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusLedgerService.accruePointsForPayment(USER_ID, 50, booking, payment);

        assertThat(card.getPointsBalance()).isEqualTo(150);
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void accruePointsForPaymentWhenPointsNullShouldDoNothing() {
        Booking booking = Booking.builder().id(BOOKING_ID).build();
        Payment payment = Payment.builder().id(PAYMENT_ID).build();

        bonusLedgerService.accruePointsForPayment(USER_ID, null, booking, payment);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void accruePointsForPaymentWhenPointsZeroShouldDoNothing() {
        Booking booking = Booking.builder().id(BOOKING_ID).build();
        Payment payment = Payment.builder().id(PAYMENT_ID).build();

        bonusLedgerService.accruePointsForPayment(USER_ID, 0, booking, payment);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void refundPointsShouldSucceed() {
        User user = User.builder().id(USER_ID).build();
        Booking booking = Booking.builder().id(BOOKING_ID).bonusPointsUsed(50).user(user).build();
        BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusLedgerService.refundPoints(booking);

        assertThat(card.getPointsBalance()).isEqualTo(150);
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void refundPointsWhenNoPointsUsedShouldDoNothing() {
        User user = User.builder().id(USER_ID).build();
        Booking booking = Booking.builder().id(BOOKING_ID).bonusPointsUsed(null).user(user).build();

        bonusLedgerService.refundPoints(booking);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void refundPointsWhenPointsZeroShouldDoNothing() {
        User user = User.builder().id(USER_ID).build();
        Booking booking = Booking.builder().id(BOOKING_ID).bonusPointsUsed(0).user(user).build();

        bonusLedgerService.refundPoints(booking);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void refundPointsForTicketShouldSucceed() {
        BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

        when(bonusTransactionRepository.existsByReferenceId("REFUND_TICKET_1")).thenReturn(false);
        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusLedgerService.refundPointsForTicket(USER_ID, 30, "REFUND_TICKET_1");

        assertThat(card.getPointsBalance()).isEqualTo(130);
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void refundPointsForTicketWhenAlreadyAppliedShouldSkip() {
        when(bonusTransactionRepository.existsByReferenceId("REFUND_TICKET_1")).thenReturn(true);

        bonusLedgerService.refundPointsForTicket(USER_ID, 30, "REFUND_TICKET_1");

        verify(bonusCardRepository, never()).findByUserId(any());
        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void getOrCreateCardWhenExistsShouldReturnCard() {
        BonusCard card = new BonusCard();
        User user = User.builder().id(USER_ID).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        BonusCard result = bonusLedgerService.getOrCreateCard(user);

        assertThat(result).isEqualTo(card);
        verify(bonusCardRepository, never()).save(any());
    }

    @Test
    void getOrCreateCardWhenNotExistsShouldCreateCard() {
        User user = User.builder().id(USER_ID).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));

        BonusCard result = bonusLedgerService.getOrCreateCard(user);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getPointsBalance()).isZero();
        assertThat(result.isWelcomeBonusReceived()).isFalse();
        verify(bonusCardRepository).save(any(BonusCard.class));
    }
}
