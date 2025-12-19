package ua.lviv.bas.cinema.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "user", "transactions" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "bonus_cards")
public class BonusCard {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true, nullable = false)
	private User user;

	@Column(name = "points_balance", nullable = false)
	@Builder.Default
	private Integer pointsBalance = 0;

	@Column(name = "last_birthday_bonus_date")
	private LocalDate lastBirthdayBonusDate;

	@Column(name = "welcome_bonus_received", nullable = false)
	@Builder.Default
	private Boolean welcomeBonusReceived = false;

	@OneToMany(mappedBy = "bonusCard", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderBy("createdAt DESC")
	@Builder.Default
	private List<BonusTransaction> transactions = new ArrayList<>();

	public boolean hasEnoughPoints(Integer points) {
		return points != null && pointsBalance >= points;
	}
}