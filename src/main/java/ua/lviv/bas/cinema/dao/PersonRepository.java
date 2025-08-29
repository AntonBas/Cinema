package ua.lviv.bas.cinema.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.lviv.bas.cinema.domain.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {

}
