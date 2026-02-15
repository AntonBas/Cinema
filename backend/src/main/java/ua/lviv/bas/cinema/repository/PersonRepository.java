package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.domain.projection.PersonProjection;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

	boolean existsByNameAndRole(String name, PersonRole role);

	boolean existsByNameAndRoleAndIdNot(String name, PersonRole role, Long id);

	Page<Person> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

	Page<Person> findByRole(PersonRole role, Pageable pageable);

	Page<Person> findByRoleAndNameContainingIgnoreCase(PersonRole role, String name, Pageable pageable);

	@Query("SELECT p.id as id, p.name as name, p.role as role, " + "(SELECT COUNT(DISTINCT m.id) FROM Movie m "
			+ " WHERE EXISTS (SELECT 1 FROM m.actors a WHERE a.id = p.id) OR "
			+ "       EXISTS (SELECT 1 FROM m.directors d WHERE d.id = p.id) OR "
			+ "       EXISTS (SELECT 1 FROM m.screenwriters s WHERE s.id = p.id)) as movieCount " + "FROM Person p "
			+ "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND "
			+ "      (:role IS NULL OR p.role = :role) " + "ORDER BY movieCount DESC, p.name ASC")
	Page<PersonProjection> findProjectionsByFilters(@Param("name") String name, @Param("role") PersonRole role,
			Pageable pageable);

	@Query("SELECT COUNT(m) FROM Movie m WHERE " + "EXISTS (SELECT 1 FROM m.actors a WHERE a.id = :personId) OR "
			+ "EXISTS (SELECT 1 FROM m.directors d WHERE d.id = :personId) OR "
			+ "EXISTS (SELECT 1 FROM m.screenwriters s WHERE s.id = :personId)")
	long countMovieUsage(@Param("personId") Long personId);
}