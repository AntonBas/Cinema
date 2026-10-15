package ua.lviv.bas.cinema.notification;

import java.math.BigDecimal;
import java.util.List;

public record TicketsEmail(String toEmail, String bookingNumber, String movieTitle, String sessionTime,
                           String hallName, String seatInfo, BigDecimal totalPrice, int bonusPointsUsed,
                           BigDecimal bonusDiscount, BigDecimal amountPaid, List<String> ticketCodes,
                           List<EmailAttachment> qrCodes) {
}
