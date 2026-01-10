package ua.lviv.bas.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.lviv.bas.cinema.domain.Refund;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
}