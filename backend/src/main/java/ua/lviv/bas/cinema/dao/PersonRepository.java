package ua.lviv.bas.cinema.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
	boolean existsByNameAndRole(String name, PersonRole role);

	Page<Person> searchPersons(String query, PersonRole role, Pageable pageable);
}