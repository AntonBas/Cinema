package ua.lviv.bas.cinema.service.bonus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.config.properties.BonusProperties;
import ua.lviv.bas.cinema.domain.bonus.BonusCard;
import ua.lviv.bas.cinema.domain.bonus.BonusRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransaction;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusValidationException;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.InsufficientPointsException;
import ua.lviv.bas.cinema.repository.bonus.BonusCardRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusRulesRepository;
import ua.lviv.bas.cinema.repository.bonus.BonusTransactionRepository;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BonusServiceTest {

    @Mock
    private BonusCardRepository bonusCardRepository;
    @Mock
    private BonusRulesRepository bonusRulesRepository;
    @Mock
    private BonusTransactionRepository bonusTransactionRepository;
    @Mock
    private BonusProperties bonusProperties;
    @Mock
    private AuditService auditService;
    @InjectMocks
    private BonusService bonusService;
    @Captor
    private ArgumentCaptor<BonusCard> cardCaptor;
    @Captor
    private ArgumentCaptor<BonusTransaction> transactionCaptor;

    private final Long USER_ID = 1L;
    private final Long BOOKING_ID = 1L;
    private final Long PAYMENT_ID = 1L;
    private final BonusTransactionType WELCOME = BonusTransactionType.WELCOME_BONUS;
    private final BonusTransactionType BIRTHDAY = BonusTransactionType.BIRTHDAY_BONUS;
    private final BonusTransactionType PAYMENT_TYPE = BonusTransactionType.PAYMENT_ACCRUAL;
    private final BonusTransactionType SPEND = BonusTransactionType.BOOKING_SPEND;

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(auditService).logChange(anyString(), anyLong(), anyString(), any(), any(), any());
    }

    @Test
    void getBalanceShouldReturnBalance() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();
        BonusRules spendRule = BonusRules.builder().minPointsPerTransaction(10).maxPointsPerTransaction(50).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(SPEND)).thenReturn(Optional.of(spendRule));

        var result = bonusService.getBalance(USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.pointsBalance()).isEqualTo(100);
        assertThat(result.pointValue()).isEqualTo(new BigDecimal("1.00"));
        assertThat(result.balanceValue()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.minUsablePoints()).isEqualTo(10);
        assertThat(result.maxUsablePoints()).isEqualTo(50);
    }

    @Test
    void getBalanceShouldThrowExceptionWhenCardNotFound() {
        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bonusService.getBalance(USER_ID)).isInstanceOf(BonusCardNotFoundException.class);
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

        bonusService.awardWelcomeBonus(user);

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

        bonusService.awardWelcomeBonus(user);

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

        bonusService.awardBirthdayBonus(user);

        assertThat(card.getPointsBalance()).isEqualTo(100);
        assertThat(card.getLastBirthdayBonusDate()).isEqualTo(LocalDate.now());
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void awardBirthdayBonusWhenNotVerifiedShouldDoNothing() {
        User user = User.builder().verificationStatus(VerificationStatus.NOT_VERIFIED).build();

        bonusService.awardBirthdayBonus(user);

        verify(bonusCardRepository, never()).findByUserId(any());
    }

    @Test
    void awardBirthdayBonusWhenNoBirthDateShouldDoNothing() {
        User user = User.builder().verificationStatus(VerificationStatus.VERIFIED).dateOfBirth(null).build();

        bonusService.awardBirthdayBonus(user);

        verify(bonusCardRepository, never()).findByUserId(any());
    }

    @Test
    void awardBirthdayBonusWhenAlreadyReceivedThisYearShouldDoNothing() {
        User user = User.builder().id(USER_ID).verificationStatus(VerificationStatus.VERIFIED)
                .dateOfBirth(LocalDate.now()).build();
        BonusCard card = BonusCard.builder().pointsBalance(0).lastBirthdayBonusDate(LocalDate.now()).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        bonusService.awardBirthdayBonus(user);

        verify(bonusRulesRepository, never()).findByBonusTypeAndActiveTrue(any());
        verify(bonusCardRepository, never()).save(any());
    }

    @Test
    void addPromotionPointsShouldAddPoints() {
        User user = User.builder().id(USER_ID).build();
        BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusService.addPromotionPoints(user, 50, "PROMO");

        assertThat(card.getPointsBalance()).isEqualTo(150);
        verify(bonusCardRepository).findByUserId(USER_ID);
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void addPromotionPointsWhenPointsInvalidShouldThrowException() {
        User user = User.builder().id(USER_ID).build();

        assertThatThrownBy(() -> bonusService.addPromotionPoints(user, 0, "PROMO"))
                .isInstanceOf(BonusValidationException.class);
        assertThatThrownBy(() -> bonusService.addPromotionPoints(user, null, "PROMO"))
                .isInstanceOf(BonusValidationException.class);
    }

    @Test
    void validateRedemptionWhenSufficientPointsShouldPass() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        bonusService.validateRedemption(USER_ID, 50);
    }

    @Test
    void validateRedemptionWhenInsufficientPointsShouldThrowException() {
        BonusCard card = BonusCard.builder().pointsBalance(30).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> bonusService.validateRedemption(USER_ID, 50))
                .isInstanceOf(InsufficientPointsException.class);
    }

    @Test
    void spendPointsShouldSucceed() {
        Booking booking = Booking.builder().id(BOOKING_ID).build();
        BonusCard card = BonusCard.builder().id(1L).pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));
        when(bonusTransactionRepository.save(any(BonusTransaction.class))).thenAnswer(i -> i.getArgument(0));

        bonusService.spendPoints(USER_ID, 30, booking);

        assertThat(card.getPointsBalance()).isEqualTo(70);
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

        bonusService.accruePointsForPayment(USER_ID, 50, booking, payment);

        assertThat(card.getPointsBalance()).isEqualTo(150);
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void accruePointsForPaymentWhenPointsNullShouldDoNothing() {
        Booking booking = Booking.builder().id(BOOKING_ID).build();
        Payment payment = Payment.builder().id(PAYMENT_ID).build();

        bonusService.accruePointsForPayment(USER_ID, null, booking, payment);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void accruePointsForPaymentWhenPointsZeroShouldDoNothing() {
        Booking booking = Booking.builder().id(BOOKING_ID).build();
        Payment payment = Payment.builder().id(PAYMENT_ID).build();

        bonusService.accruePointsForPayment(USER_ID, 0, booking, payment);

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

        bonusService.refundPoints(booking);

        assertThat(card.getPointsBalance()).isEqualTo(150);
        verify(bonusCardRepository).save(any(BonusCard.class));
        verify(bonusTransactionRepository).save(any(BonusTransaction.class));
    }

    @Test
    void refundPointsWhenNoPointsUsedShouldDoNothing() {
        User user = User.builder().id(USER_ID).build();
        Booking booking = Booking.builder().id(BOOKING_ID).bonusPointsUsed(null).user(user).build();

        bonusService.refundPoints(booking);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void refundPointsWhenPointsZeroShouldDoNothing() {
        User user = User.builder().id(USER_ID).build();
        Booking booking = Booking.builder().id(BOOKING_ID).bonusPointsUsed(0).user(user).build();

        bonusService.refundPoints(booking);

        verify(bonusCardRepository, never()).save(any());
        verify(bonusTransactionRepository, never()).save(any());
    }

    @Test
    void calculateAccrualPointsShouldCalculateCorrectly() {
        BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("1.5")).minPointsPerTransaction(10)
                .maxPointsPerTransaction(500).build();

        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

        Integer result = bonusService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isEqualTo(150);
    }

    @Test
    void calculateAccrualPointsShouldApplyMinLimit() {
        BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("0.1")).minPointsPerTransaction(50).build();

        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

        Integer result = bonusService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isEqualTo(50);
    }

    @Test
    void calculateAccrualPointsShouldApplyMaxLimit() {
        BonusRules rule = BonusRules.builder().moneyRatio(new BigDecimal("10.0")).maxPointsPerTransaction(500).build();

        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.of(rule));

        Integer result = bonusService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isEqualTo(500);
    }

    @Test
    void calculateAccrualPointsWhenNoRuleShouldReturnZero() {
        when(bonusRulesRepository.findByBonusTypeAndActiveTrue(PAYMENT_TYPE)).thenReturn(Optional.empty());

        Integer result = bonusService.calculateAccrualPoints(new BigDecimal("100"));

        assertThat(result).isZero();
    }

    @Test
    void calculateAccrualPointsWhenAmountNullShouldReturnZero() {
        Integer result = bonusService.calculateAccrualPoints(null);

        assertThat(result).isZero();
    }

    @Test
    void calculateAccrualPointsWhenAmountZeroShouldReturnZero() {
        Integer result = bonusService.calculateAccrualPoints(BigDecimal.ZERO);

        assertThat(result).isZero();
    }

    @Test
    void validatePointsForBookingShouldSucceed() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
        when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

        bonusService.validatePointsForBooking(USER_ID, 30, new BigDecimal("100"));
    }

    @Test
    void validatePointsForBookingWhenDiscountExceedsShouldThrowException() {
        BonusCard card = BonusCard.builder().pointsBalance(100).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));
        when(bonusProperties.getPointValue()).thenReturn(new BigDecimal("1.00"));
        when(bonusProperties.getMaxDiscountPercentage()).thenReturn(new BigDecimal("0.5"));

        assertThatThrownBy(() -> bonusService.validatePointsForBooking(USER_ID, 60, new BigDecimal("100")))
                .isInstanceOf(BonusValidationException.class);
    }

    @Test
    void getOrCreateCardWhenExistsShouldReturnCard() {
        BonusCard card = new BonusCard();
        User user = User.builder().id(USER_ID).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.of(card));

        BonusCard result = bonusService.getOrCreateCard(user);

        assertThat(result).isEqualTo(card);
        verify(bonusCardRepository, never()).save(any());
    }

    @Test
    void getOrCreateCardWhenNotExistsShouldCreateCard() {
        User user = User.builder().id(USER_ID).build();

        when(bonusCardRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(bonusCardRepository.save(any(BonusCard.class))).thenAnswer(i -> i.getArgument(0));

        BonusCard result = bonusService.getOrCreateCard(user);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getPointsBalance()).isZero();
        assertThat(result.isWelcomeBonusReceived()).isFalse();
        verify(bonusCardRepository).save(any(BonusCard.class));
    }
}