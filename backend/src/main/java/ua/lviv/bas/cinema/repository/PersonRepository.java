package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, QuerydslPredicateExecutor<Person> {

	boolean existsByNameAndRole(String name, PersonRole role);

	Page<Person> searchPersons(String query, PersonRole role, Pageable pageable);
}