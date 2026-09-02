package ua.lviv.bas.cinema.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;
import ua.lviv.bas.cinema.user.repository.UserRepository;
import ua.lviv.bas.cinema.service.bonus.BonusLedgerService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirthdayBonusSchedulerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BonusLedgerService bonusLedgerService;

    @InjectMocks
    private BirthdayBonusScheduler birthdayBonusScheduler;

    @Test
    void awardBirthdayBonusesWhenNoneFoundShouldNotAward() {
        when(userRepository.findVerifiedUsersWithBirthday(eq(VerificationStatus.VERIFIED), anyInt(), anyInt()))
                .thenReturn(List.of());

        birthdayBonusScheduler.awardBirthdayBonuses();

        verifyNoInteractions(bonusLedgerService);
    }

    @Test
    void awardBirthdayBonusesShouldAwardEachBirthdayUser() {
        var user1 = User.builder().id(1L).email("a@test.com").build();
        var user2 = User.builder().id(2L).email("b@test.com").build();

        when(userRepository.findVerifiedUsersWithBirthday(eq(VerificationStatus.VERIFIED), anyInt(), anyInt()))
                .thenReturn(List.of(user1, user2));

        birthdayBonusScheduler.awardBirthdayBonuses();

        verify(bonusLedgerService).awardBirthdayBonus(user1);
        verify(bonusLedgerService).awardBirthdayBonus(user2);
    }

    @Test
    void awardBirthdayBonusesShouldContinueWhenOneUserFails() {
        var user1 = User.builder().id(1L).email("a@test.com").build();
        var user2 = User.builder().id(2L).email("b@test.com").build();

        when(userRepository.findVerifiedUsersWithBirthday(eq(VerificationStatus.VERIFIED), anyInt(), anyInt()))
                .thenReturn(List.of(user1, user2));
        doThrow(new RuntimeException("boom")).when(bonusLedgerService).awardBirthdayBonus(user1);

        birthdayBonusScheduler.awardBirthdayBonuses();

        verify(bonusLedgerService).awardBirthdayBonus(user1);
        verify(bonusLedgerService).awardBirthdayBonus(user2);
    }
}
