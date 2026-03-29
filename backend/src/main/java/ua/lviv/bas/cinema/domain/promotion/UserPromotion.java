package ua.lviv.bas.cinema.domain.promotion;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.user.User;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "user", "promotion" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "user_promotions", indexes = { @Index(name = "idx_user_promotion_user", columnList = "user_id"),
		@Index(name = "idx_user_promotion_promotion", columnList = "promotion_id"),
		@Index(name = "idx_user_promotion_redeemed", columnList = "redeemed_at") }, uniqueConstraints = @UniqueConstraint(columnNames = {
				"user_id", "promotion_id" }, name = "uk_user_promotion"))

public class UserPromotion {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id", nullable = false)
	private Promotion promotion;

	@Column(name = "redeemed_at", nullable = false)
	private LocalDateTime redeemedAt;

	@Column(name = "points_awarded", nullable = false)
	private Integer pointsAwarded;
}
