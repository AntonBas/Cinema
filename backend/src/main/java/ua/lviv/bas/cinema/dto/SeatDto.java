package ua.lviv.bas.cinema.dto;

import lombok.Builder;
import lombok.Data;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Data
@Builder
public class SeatDto {
	private Long id;
	private int row;
	private int number;
	private SeatType seatType;
	private boolean available;
	private double price;

	public String getSeatLabel() {
		if (row < 1 || number < 1) {
			return "N/A";
		}
		char rowChar = (char) ('A' + row - 1);
		return String.format("%c%d", rowChar, number);
	}
}