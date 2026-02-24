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

	@Query("""
			SELECT
			    p.id as id,
			    p.name as name,
			    p.role as role,
			    (SELECT COUNT(DISTINCT m.id) FROM Movie m
			     WHERE m.id IN (SELECT a.id FROM m.actors a WHERE a.id = p.id)
			        OR m.id IN (SELECT d.id FROM m.directors d WHERE d.id = p.id)
			        OR m.id IN (SELECT s.id FROM m.screenwriters s WHERE s.id = p.id)) as movieCount
			FROM Person p
			WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
			    AND (:role IS NULL OR p.role = :role)
			ORDER BY movieCount DESC, p.name ASC
			""")
	Page<PersonProjection> findProjectionsByFilters(@Param("name") String name, @Param("role") PersonRole role,
			Pageable pageable);
}