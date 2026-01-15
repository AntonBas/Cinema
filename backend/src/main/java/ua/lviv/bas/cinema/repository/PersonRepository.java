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

	boolean existsByNameAndRoleAndIdNot(String name, PersonRole role, Long id);

	Page<Person> findByRole(PersonRole role, Pageable pageable);

	long countByRole(PersonRole role);

	@Query(value = """
			SELECT p.* FROM persons p
			WHERE (:query IS NULL OR :query = '' OR
			       p.name ILIKE '%' || :query || '%')
			AND (:role IS NULL OR p.role = :role)
			""", countQuery = """
			SELECT COUNT(*) FROM persons p
			WHERE (:query IS NULL OR :query = '' OR
			       p.name ILIKE '%' || :query || '%')
			AND (:role IS NULL OR p.role = :role)
			""", nativeQuery = true)
	Page<Person> searchByNameAndRole(@Param("query") String query, @Param("role") String role, Pageable pageable);
}