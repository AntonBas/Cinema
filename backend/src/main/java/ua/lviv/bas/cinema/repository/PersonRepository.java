package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

	boolean existsByNameAndRole(String name, PersonRole role);

	@Query("SELECT p FROM Person p WHERE "
			+ "(:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND "
			+ "(:role IS NULL OR p.role = :role) " + "ORDER BY p.name")
	Page<Person> searchPersons(@Param("query") String query, @Param("role") PersonRole role, Pageable pageable);
}