package ua.lviv.bas.cinema.controller.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.dto.bonus.response.BonusBalanceResponse;
import ua.lviv.bas.cinema.dto.bonus.response.BonusTransactionResponse;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.exception.domain.financial.bonus.BonusCardNotFoundException;
import ua.lviv.bas.cinema.service.bonus.BonusService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BonusControllerTest {

    @Mock
    private BonusService bonusService;

    @InjectMocks
    private BonusController bonusController;

    @Test
    void getBalanceShouldReturnBalance() {
        Long userId = 1L;

        BonusBalanceResponse balanceResponse = new BonusBalanceResponse(250, new BigDecimal("1.00"),
                new BigDecimal("250.00"), 100, 1000, new BigDecimal("100.00"), new BigDecimal("1000.00"));

        when(bonusService.getBalance(userId)).thenReturn(balanceResponse);

        BonusBalanceResponse response = bonusController.getBalance(userId);

        assertThat(response).isNotNull();
        assertThat(response.pointsBalance()).isEqualTo(250);
        assertThat(response.pointValue()).isEqualTo(new BigDecimal("1.00"));
        assertThat(response.balanceValue()).isEqualTo(new BigDecimal("250.00"));
    }

    @Test
    void getBalanceShouldThrowWhenCardNotFound() {
        Long userId = 1L;
        when(bonusService.getBalance(userId)).thenThrow(new BonusCardNotFoundException(userId));

        assertThrows(BonusCardNotFoundException.class, () -> bonusController.getBalance(userId));
    }

    @Test
    void getTransactionsShouldReturnPagedTransactions() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        BonusTransactionResponse transaction1 = new BonusTransactionResponse(1L, BonusTransactionType.WELCOME_BONUS,
                "+150", LocalDateTime.now(), 150);

        BonusTransactionResponse transaction2 = new BonusTransactionResponse(2L, BonusTransactionType.BOOKING_SPEND,
                "-25", LocalDateTime.now(), 125);

        Page<BonusTransactionResponse> page = new PageImpl<>(List.of(transaction1, transaction2), pageable, 2);

        when(bonusService.getTransactions(eq(userId), any(Pageable.class))).thenReturn(page);

        PageResponse<BonusTransactionResponse> response = bonusController.getTransactions(userId, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(2);
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.content().getFirst().id()).isEqualTo(1L);
        assertThat(response.content().getFirst().type()).isEqualTo(BonusTransactionType.WELCOME_BONUS);
        assertThat(response.content().get(0).pointsChange()).isEqualTo("+150");
        assertThat(response.content().get(0).newBalance()).isEqualTo(150);
        assertThat(response.content().get(1).id()).isEqualTo(2L);
        assertThat(response.content().get(1).type()).isEqualTo(BonusTransactionType.BOOKING_SPEND);
        assertThat(response.content().get(1).pointsChange()).isEqualTo("-25");
        assertThat(response.content().get(1).newBalance()).isEqualTo(125);
    }

    @Test
    void getTransactionsShouldReturnEmptyPage() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 20);

        Page<BonusTransactionResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(bonusService.getTransactions(eq(userId), any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<BonusTransactionResponse> response = bonusController.getTransactions(userId, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).isEmpty();
        assertThat(response.totalElements()).isZero();
        assertThat(response.empty()).isTrue();
    }

    @Test
    void getTransactionsWithCustomPageableShouldReturnPagedTransactions() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(1, 5);

        BonusTransactionResponse transaction = new BonusTransactionResponse(1L, BonusTransactionType.PAYMENT_ACCRUAL,
                "+50", LocalDateTime.now(), 200);

        Page<BonusTransactionResponse> page = new PageImpl<>(List.of(transaction), pageable, 10);

        when(bonusService.getTransactions(eq(userId), eq(pageable))).thenReturn(page);

        PageResponse<BonusTransactionResponse> response = bonusController.getTransactions(userId, pageable);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(1);
        assertThat(response.number()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.totalElements()).isEqualTo(10);
        assertThat(response.totalPages()).isEqualTo(2);
        assertThat(response.content().getFirst().type()).isEqualTo(BonusTransactionType.PAYMENT_ACCRUAL);
        assertThat(response.content().getFirst().pointsChange()).isEqualTo("+50");
    }
}