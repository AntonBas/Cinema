package ua.lviv.bas.cinema.repository.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.lviv.bas.cinema.domain.booking.Refund;

import java.util.List;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByUserIdOrderByCreatedDateDesc(Long userId);
}