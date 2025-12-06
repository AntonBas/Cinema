package ua.lviv.bas.cinema.service.query;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.QUser;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

	private final UserRepository userRepository;

	public Page<User> findFilteredUsers(String search, UserRole role, Boolean enabled, Pageable pageable) {
		log.debug("Finding filtered users: search='{}', role={}, enabled={}", search, role, enabled);

		QUser user = QUser.user;
		BooleanBuilder predicate = new BooleanBuilder();

		if (StringUtils.hasText(search)) {
			String searchTerm = "%" + search.toLowerCase() + "%";
			predicate.andAnyOf(user.email.lower().like(searchTerm), user.firstName.lower().like(searchTerm),
					user.lastName.lower().like(searchTerm));
		}

		if (role != null) {
			predicate.and(user.userRole.eq(role));
		}

		if (enabled != null) {
			predicate.and(user.enabled.eq(enabled));
		}

		return userRepository.findAll(predicate, pageable);
	}

	public Optional<User> findByEmail(String email) {
		if (!StringUtils.hasText(email)) {
			return Optional.empty();
		}

		QUser user = QUser.user;
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(user.email.equalsIgnoreCase(email));

		return userRepository.findOne(predicate);
	}

	public boolean existsByEmail(String email) {
		if (!StringUtils.hasText(email)) {
			return false;
		}

		QUser user = QUser.user;
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(user.email.equalsIgnoreCase(email));

		return userRepository.exists(predicate);
	}

	public long countAdmins() {
		QUser user = QUser.user;
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(user.userRole.eq(UserRole.ROLE_ADMIN));
		predicate.and(user.enabled.isTrue());

		return userRepository.count(predicate);
	}

	public List<User> findAllActiveByRole(UserRole role) {
		QUser user = QUser.user;
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(user.userRole.eq(role));
		predicate.and(user.enabled.isTrue());

		return (List<User>) userRepository.findAll(predicate);
	}

	public List<User> findAllActiveUsers() {
		QUser user = QUser.user;
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(user.enabled.isTrue());

		return (List<User>) userRepository.findAll(predicate, Sort.by(Sort.Direction.ASC, "email"));
	}

}