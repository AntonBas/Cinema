package ua.lviv.bas.cinema.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.service.query.UserQueryService;
import ua.lviv.bas.cinema.service.user.BonusUserService;

@ExtendWith(MockitoExtension.class)
class BirthdayBonusSchedulerTest {

	@Mock
	private UserQueryService userQueryService;

	@Mock
	private BonusUserService bonusUserService;

	@InjectMocks
	private BirthdayBonusScheduler scheduler;

	@Test
	void awardBirthdayBonuses_whenUsersFound_shouldAwardBonuses() {
		User user1 = User.builder().id(1L).email("user1@test.com").build();
		User user2 = User.builder().id(2L).email("user2@test.com").build();

		when(userQueryService.findVerifiedUsersWithBirthdayToday(anyInt(), anyInt())).thenReturn(List.of(user1, user2));

		scheduler.awardBirthdayBonuses();

		verify(bonusUserService).awardBirthdayBonus(user1);
		verify(bonusUserService).awardBirthdayBonus(user2);
	}

	@Test
	void awardBirthdayBonuses_whenServiceThrowsException_shouldContinueProcessing() {
		User user1 = User.builder().id(1L).email("user1@test.com").build();
		User user2 = User.builder().id(2L).email("user2@test.com").build();

		when(userQueryService.findVerifiedUsersWithBirthdayToday(anyInt(), anyInt())).thenReturn(List.of(user1, user2));

		doThrow(new RuntimeException("DB error")).when(bonusUserService).awardBirthdayBonus(user1);

		scheduler.awardBirthdayBonuses();

		verify(bonusUserService).awardBirthdayBonus(user1);
		verify(bonusUserService).awardBirthdayBonus(user2);
	}

	@Test
	void awardBirthdayBonuses_whenNoUsers_shouldLogZeroCount() {
		when(userQueryService.findVerifiedUsersWithBirthdayToday(anyInt(), anyInt())).thenReturn(List.of());

		scheduler.awardBirthdayBonuses();

		verify(bonusUserService, never()).awardBirthdayBonus(any(User.class));
	}
}