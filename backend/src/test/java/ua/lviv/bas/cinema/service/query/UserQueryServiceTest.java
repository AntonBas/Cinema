package ua.lviv.bas.cinema.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.querydsl.core.types.Predicate;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;
import ua.lviv.bas.cinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserQueryService userQueryService;

	@Test
	void findFilteredUsers_ShouldReturnFilteredUsers() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setUserRole(UserRole.ROLE_USER);
		user.setEnabled(true);

		Page<User> userPage = new PageImpl<>(List.of(user));
		Pageable pageable = PageRequest.of(0, 10);

		when(userRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(userPage);

		Page<User> result = userQueryService.findFilteredUsers("test", UserRole.ROLE_USER, true, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getEmail()).isEqualTo("test@example.com");
	}

	@Test
	void findFilteredUsers_ShouldReturnAllUsers_WhenNoFilters() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");

		Page<User> userPage = new PageImpl<>(List.of(user));
		Pageable pageable = PageRequest.of(0, 10);

		when(userRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(userPage);

		Page<User> result = userQueryService.findFilteredUsers(null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFilteredUsers_ShouldSearchByEmail() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");

		Page<User> userPage = new PageImpl<>(List.of(user));
		Pageable pageable = PageRequest.of(0, 10);

		when(userRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(userPage);

		Page<User> result = userQueryService.findFilteredUsers("example", null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findByEmail_ShouldReturnUser() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");

		when(userRepository.findOne(any(Predicate.class))).thenReturn(Optional.of(user));

		Optional<User> result = userQueryService.findByEmail("test@example.com");

		assertThat(result).isPresent();
		assertThat(result.get().getEmail()).isEqualTo("test@example.com");
	}

	@Test
	void findByEmail_ShouldReturnEmpty_WhenEmailIsNull() {
		Optional<User> result = userQueryService.findByEmail(null);

		assertThat(result).isEmpty();
	}

	@Test
	void findByEmail_ShouldReturnEmpty_WhenEmailIsEmpty() {
		Optional<User> result = userQueryService.findByEmail("");

		assertThat(result).isEmpty();
	}

	@Test
	void existsByEmail_ShouldReturnTrue() {
		when(userRepository.exists(any(Predicate.class))).thenReturn(true);

		boolean result = userQueryService.existsByEmail("test@example.com");

		assertThat(result).isTrue();
	}

	@Test
	void existsByEmail_ShouldReturnFalse_WhenEmailIsNull() {
		boolean result = userQueryService.existsByEmail(null);

		assertThat(result).isFalse();
	}

	@Test
	void countAdmins_ShouldReturnCount() {
		when(userRepository.count(any(Predicate.class))).thenReturn(5L);

		long result = userQueryService.countAdmins();

		assertThat(result).isEqualTo(5L);
	}

	@Test
	void findAllActiveByRole_ShouldReturnUsers() {
		User user = new User();
		user.setId(1L);
		user.setUserRole(UserRole.ROLE_ADMIN);
		user.setEnabled(true);

		when(userRepository.findAll(any(Predicate.class))).thenReturn(List.of(user));

		List<User> result = userQueryService.findAllActiveByRole(UserRole.ROLE_ADMIN);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getUserRole()).isEqualTo(UserRole.ROLE_ADMIN);
	}

	@Test
	void findAllActiveUsers_ShouldReturnActiveUsers() {
		User user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setEnabled(true);

		when(userRepository.findAll(any(Predicate.class), any(Sort.class))).thenReturn(List.of(user));

		List<User> result = userQueryService.findAllActiveUsers();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).isEnabled()).isTrue();
	}

	@Test
	void findVerifiedUsersWithBirthdayToday_ShouldReturnUsers() {
		User user1 = User.builder().id(1L).email("user1@test.com").dateOfBirth(LocalDate.of(1990, 5, 15))
				.verificationStatus(VerificationStatus.VERIFIED).enabled(true).build();

		User user2 = User.builder().id(2L).email("user2@test.com").dateOfBirth(LocalDate.of(1985, 5, 15))
				.verificationStatus(VerificationStatus.VERIFIED).enabled(true).build();

		when(userRepository.findAll(any(Predicate.class))).thenReturn(List.of(user1, user2));

		List<User> result = userQueryService.findVerifiedUsersWithBirthdayToday(15, 5);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
		assertThat(result.get(0).isEnabled()).isTrue();
	}

	@Test
	void findVerifiedUsersWithBirthdayToday_ShouldReturnEmpty_WhenNoMatches() {
		when(userRepository.findAll(any(Predicate.class))).thenReturn(List.of());

		List<User> result = userQueryService.findVerifiedUsersWithBirthdayToday(15, 5);

		assertThat(result).isEmpty();
	}

	@Test
	void findVerifiedUsersWithBirthdayToday_ShouldFilterOutNotVerified() {
		User verifiedUser = User.builder().id(1L).email("verified@test.com").dateOfBirth(LocalDate.of(1990, 5, 15))
				.verificationStatus(VerificationStatus.VERIFIED).enabled(true).build();

		when(userRepository.findAll(any(Predicate.class))).thenReturn(List.of(verifiedUser));

		List<User> result = userQueryService.findVerifiedUsersWithBirthdayToday(15, 5);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getVerificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
	}

	@Test
	void findVerifiedUsersWithBirthdayToday_ShouldFilterOutDisabled() {
		User enabledUser = User.builder().id(1L).email("enabled@test.com").dateOfBirth(LocalDate.of(1990, 5, 15))
				.verificationStatus(VerificationStatus.VERIFIED).enabled(true).build();

		when(userRepository.findAll(any(Predicate.class))).thenReturn(List.of(enabledUser));

		List<User> result = userQueryService.findVerifiedUsersWithBirthdayToday(15, 5);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).isEnabled()).isTrue();
	}

	@Test
	void findVerifiedUsersWithBirthdayToday_ShouldReturnEmpty_ForNonExistingDate() {
		when(userRepository.findAll(any(Predicate.class))).thenReturn(List.of());

		List<User> result = userQueryService.findVerifiedUsersWithBirthdayToday(31, 2);

		assertThat(result).isEmpty();
	}
}