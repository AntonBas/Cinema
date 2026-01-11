package ua.lviv.bas.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

	List<Refund> findByUserIdOrderByCreatedAtDesc(Long userId);

	Optional<Refund> findByIdAndUserId(Long id, Long userId);
}