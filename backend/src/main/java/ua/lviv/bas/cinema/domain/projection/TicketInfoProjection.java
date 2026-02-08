package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Immutable
public class TicketInfoProjection {

	@Id
	private Long id;

	@Column(name = "unique_code")
	private String uniqueCode;

	@Enumerated(EnumType.STRING)
	private TicketStatus status;

	@Column(name = "purchase_time")
	private LocalDateTime purchaseTime;

	@Column(name = "final_price")
	private BigDecimal finalPrice;

	@Column(name = "ticket_type_name")
	private String ticketTypeName;

	@Column(name = "movie_title")
	private String movieTitle;

	@Column(name = "session_start_time")
	private LocalDateTime sessionStartTime;

	@Column(name = "hall_name")
	private String hallName;

	@Column(name = "row")
	private Integer row;

	@Column(name = "seat_number")
	private Integer seatNumber;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "movie_id")
	private Long movieId;
}