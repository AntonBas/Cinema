package ua.lviv.bas.cinema.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.domain.projection.PersonProjection;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {

	boolean existsByNameAndRole(String name, PersonRole role);

	boolean existsByNameAndRoleAndIdNot(String name, PersonRole role, Long id);

	Page<Person> findByRole(PersonRole role, Pageable pageable);

	long countByRole(PersonRole role);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.actors a WHERE a.id = :personId")
	long countByActorsId(@Param("personId") Long personId);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.directors d WHERE d.id = :personId")
	long countByDirectorsId(@Param("personId") Long personId);

	@Query("SELECT COUNT(m) FROM Movie m JOIN m.screenwriters s WHERE s.id = :personId")
	long countByScreenwritersId(@Param("personId") Long personId);

	@Query("SELECT p.id as id, p.name as name, p.role as role, " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m1.id) FROM Movie m1 " + "   LEFT JOIN m1.actors a1 ON a1.id = p.id "
			+ "   WHERE a1.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m2.id) FROM Movie m2 " + "   LEFT JOIN m2.directors d2 ON d2.id = p.id "
			+ "   WHERE d2.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m3.id) FROM Movie m3 " + "   LEFT JOIN m3.screenwriters s3 ON s3.id = p.id "
			+ "   WHERE s3.id IS NOT NULL" + "), 0) as movieCount " + "FROM Person p " + "WHERE p.id = :id")
	PersonProjection findProjectionById(@Param("id") Long id);

	@Query("SELECT p.id as id, p.name as name, p.role as role, " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m1.id) FROM Movie m1 " + "   LEFT JOIN m1.actors a1 ON a1.id = p.id "
			+ "   WHERE a1.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m2.id) FROM Movie m2 " + "   LEFT JOIN m2.directors d2 ON d2.id = p.id "
			+ "   WHERE d2.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m3.id) FROM Movie m3 " + "   LEFT JOIN m3.screenwriters s3 ON s3.id = p.id "
			+ "   WHERE s3.id IS NOT NULL" + "), 0) as movieCount " + "FROM Person p")
	Page<PersonProjection> findAllProjections(Pageable pageable);

	@Query("SELECT p.id as id, p.name as name, p.role as role, " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m1.id) FROM Movie m1 " + "   LEFT JOIN m1.actors a1 ON a1.id = p.id "
			+ "   WHERE a1.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m2.id) FROM Movie m2 " + "   LEFT JOIN m2.directors d2 ON d2.id = p.id "
			+ "   WHERE d2.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m3.id) FROM Movie m3 " + "   LEFT JOIN m3.screenwriters s3 ON s3.id = p.id "
			+ "   WHERE s3.id IS NOT NULL" + "), 0) as movieCount " + "FROM Person p " + "WHERE p.role = :role")
	Page<PersonProjection> findProjectionsByRole(@Param("role") PersonRole role, Pageable pageable);

	@Query("SELECT p.id as id, p.name as name, p.role as role, " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m1.id) FROM Movie m1 " + "   LEFT JOIN m1.actors a1 ON a1.id = p.id "
			+ "   WHERE a1.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m2.id) FROM Movie m2 " + "   LEFT JOIN m2.directors d2 ON d2.id = p.id "
			+ "   WHERE d2.id IS NOT NULL" + "), 0) + " + "COALESCE(("
			+ "   SELECT COUNT(DISTINCT m3.id) FROM Movie m3 " + "   LEFT JOIN m3.screenwriters s3 ON s3.id = p.id "
			+ "   WHERE s3.id IS NOT NULL" + "), 0) as movieCount " + "FROM Person p "
			+ "WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
			+ "AND (:role IS NULL OR p.role = :role)")
	Page<PersonProjection> findProjectionsByFilters(@Param("name") String name, @Param("role") PersonRole role,
			Pageable pageable);
}