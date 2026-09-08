package ua.lviv.bas.cinema.common;

import org.springframework.stereotype.Component;

@Component
public class SeatInfoFormatter {

    public String format(int row, int number) {
        return String.format("Row %d, Seat %d", row, number);
    }
}
