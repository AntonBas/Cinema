package ua.lviv.bas.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Session;

@Repository
public interface SessionRepository
		extends JpaRepository<Session, Long>, QuerydslPredicateExecutor<Session>, JpaSpecificationExecutor<Session> {

}