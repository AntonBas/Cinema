package ua.lviv.bas.cinema.movie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.movie.domain.Person;
import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;
import ua.lviv.bas.cinema.movie.repository.projection.PersonListProjection;

@Repository
@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection"})
public interface PersonRepository extends JpaRepository<Person, Long> {

	boolean existsByNameAndRole(String name, PersonRole role);

	boolean existsByNameAndRoleAndIdNot(String name, PersonRole role, Long id);

	@Query(value = """
			SELECT
			    p.id as id,
			    p.name as name,
			    p.role as role,
			    COALESCE(pc.movie_count, 0) as movieCount
			FROM persons p
			LEFT JOIN (
			    SELECT person_id, COUNT(DISTINCT movie_id) as movie_count
			    FROM (
			        SELECT movie_id, person_id FROM movie_cast
			        UNION
			        SELECT movie_id, person_id FROM movie_directors
			        UNION
			        SELECT movie_id, person_id FROM movie_screenwriters
			    ) movie_roles
			    GROUP BY person_id
			) pc ON pc.person_id = p.id
			WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')))
			    AND (:role IS NULL OR p.role = CAST(:role AS text))
			ORDER BY COALESCE(pc.movie_count, 0) DESC, p.name ASC
			""", countQuery = """
			SELECT COUNT(*)
			FROM persons p
			WHERE (:query IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:query AS text), '%')))
			    AND (:role IS NULL OR p.role = CAST(:role AS text))
			""", nativeQuery = true)
	Page<PersonListProjection> findPersonsByFilters(@Param("query") String query, @Param("role") String role,
			Pageable pageable);
}