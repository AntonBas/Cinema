package ua.lviv.bas.cinema.repository.projection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.projection.MovieCardProjection;

@Repository
public interface MovieCardProjectionRepository
		extends JpaRepository<MovieCardProjection, Long>, JpaSpecificationExecutor<MovieCardProjection> {
}