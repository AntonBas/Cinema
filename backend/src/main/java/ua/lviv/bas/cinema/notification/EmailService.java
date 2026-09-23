package ua.lviv.bas.cinema.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import ua.lviv.bas.cinema.config.async.AsyncConfig;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;
import ua.lviv.bas.cinema.common.CinemaTime;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 200;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final RestClient restClient;
    private final String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.company.name:Cinema}")
    private String companyName;

    public EmailService(@Value("${app.mail.api-key:}") String apiKey,
                         @Value("${app.email.from:noreply@bascinema.com}") String fromEmail) {
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.brevo.com/v3/smtp/email")
                .defaultHeader("api-key", apiKey)
                .build();
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendUrl + "/verify-email/" + token;
        String html = buildActionEmail("Confirm Your Email Address",
                "Thank you for registering with " + companyName + "! To activate your account, click the button below.",
                "Confirm Email", link,
                "This link will expire in 10 minutes. If you didn't create an account, please ignore this email.");

        sendCriticalEmail(toEmail, "Confirm Your Email Address", html);
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = frontendUrl + "/reset-password/" + token;
        String html = buildActionEmail("Password Reset Request",
                "You have requested to reset your password for your " + companyName + " account.",
                "Reset Password", link,
                "This link will expire in 10 minutes. If you didn't request a password reset, please ignore this email.");

        sendCriticalEmail(toEmail, "Password Reset Request", html);
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendTicketsEmail(String toEmail, String bookingNumber, String movieTitle, String sessionTime,
                                 String hallName, BigDecimal amountPaid, String paymentMethod, String seatInfo) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Booking Number", bookingNumber);
        details.put("Movie", movieTitle);
        details.put("Time", sessionTime);
        details.put("Hall", hallName);
        details.put("Seats", seatInfo);
        details.put("Amount Paid", amountPaid + " UAH");
        details.put("Payment Method", paymentMethod);

        String html = buildDetailsEmail("Your Tickets - " + movieTitle, details,
                "Your tickets are available in your account: " + frontendUrl + "/account/tickets<br><br>"
                        + "Please present the QR codes at the cinema entrance. Arrive 10-15 minutes before the session.<br><br>"
                        + "<strong>Important:</strong><ul style=\"margin:8px 0;padding-left:20px\">"
                        + "<li>Have your ID ready if required</li>"
                        + "<li>No refunds 30 minutes before session</li>"
                        + "<li>QR codes available in your account</li></ul>",
                "This is an automated email. Please do not reply.");

        sendNonCriticalEmail(toEmail, "Your Tickets: " + movieTitle, html);
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendPaymentFailedEmail(String toEmail, String bookingNumber, String movieTitle, String sessionTime,
                                       String errorMessage) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Booking Number", bookingNumber);
        details.put("Movie", movieTitle);
        details.put("Time", sessionTime);
        details.put("Error", errorMessage);

        String html = buildDetailsEmail("Payment Failed", details, "Please try again or contact support.", null);

        sendNonCriticalEmail(toEmail, "Payment Failed: " + movieTitle, html);
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendRefundEmail(String toEmail, String bookingNumber, String movieTitle, String sessionTime,
                                String hallName, BigDecimal refundAmount, String seatInfo, String refundReason) {
        Map<String, String> details = new LinkedHashMap<>();
        details.put("Booking Number", bookingNumber);
        details.put("Movie", movieTitle);
        details.put("Time", sessionTime);
        details.put("Hall", hallName);
        details.put("Seats", seatInfo);
        details.put("Refund Amount", refundAmount + " UAH");
        details.put("Reason", refundReason);

        String processedAt = CinemaTime.now().format(DATE_TIME_FORMATTER);
        String html = buildDetailsEmail("Refund Confirmation - " + movieTitle, details,
                "Your refund request has been successfully processed. The refunded amount will be returned to your "
                        + "original payment method within 3-5 business days.<br><br>"
                        + "<strong>Refund Summary:</strong><ul style=\"margin:8px 0;padding-left:20px\">"
                        + "<li>Ticket price refunded: " + refundAmount + " UAH</li>"
                        + "<li>Refund processed at: " + processedAt + "</li></ul>"
                        + "If you have any questions about your refund, please contact our support team.",
                "This is an automated email. Please do not reply.");

        sendNonCriticalEmail(toEmail, "Refund Confirmation: " + movieTitle, html);
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendEmailChangeConfirmation(String toEmail, String token) {
        String link = frontendUrl + "/confirm-email-change/" + token;
        String html = buildActionEmail("Confirm Your Email Change",
                "You have requested to change your " + companyName + " account email address.",
                "Confirm Email Change", link,
                "This link will expire in 24 hours. If you didn't request this change, please ignore this email.");

        sendCriticalEmail(toEmail, "Confirm Your Email Change", html);
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendEmailChangeNotification(String oldEmail, String newEmail) {
        String html = buildPlainEmail("Email Address Changed",
                "Your " + companyName + " account email address has been successfully changed:<br><br>"
                        + "Old email: " + oldEmail + "<br>New email: " + newEmail + "<br><br>"
                        + "If you didn't make this change, please contact our support team immediately.");

        sendNonCriticalEmail(oldEmail, "Email Address Changed", html);
    }

    @Async(AsyncConfig.EMAIL_EXECUTOR)
    public void sendPasswordChangedNotification(String toEmail) {
        String html = buildPlainEmail("Password Changed",
                "Your " + companyName + " account password was just changed. If you didn't make this change, "
                        + "please contact our support team immediately.");

        sendNonCriticalEmail(toEmail, "Your Password Was Changed", html);
    }

    public void sendSafely(String action, Long referenceId, Runnable emailAction) {
        try {
            emailAction.run();
        } catch (Exception e) {
            log.error("Failed to {} for id {}", action, referenceId, e);
        }
    }

    private void sendCriticalEmail(String toEmail, String subject, String html) {
        try {
            deliver(toEmail, subject, html);
            log.info("Critical email sent to: {}", toEmail);
        } catch (RestClientException e) {
            log.error("Failed to send critical email to: {}", toEmail, e);
            throw new ExternalServiceException("Email Service", e);
        }
    }

    private void sendNonCriticalEmail(String toEmail, String subject, String html) {
        try {
            deliver(toEmail, subject, html);
            log.info("Non-critical email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send non-critical email to: {}", toEmail, e);
        }
    }

    private void deliver(String toEmail, String subject, String html) {
        var payload = Map.of(
                "sender", Map.of("name", companyName, "email", fromEmail),
                "to", List.of(Map.of("email", toEmail)),
                "subject", subject,
                "htmlContent", html
        );

        RestClientException lastException = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                restClient.post().body(payload).retrieve().toBodilessEntity();
                return;
            } catch (RestClientException e) {
                lastException = e;
                if (attempt < MAX_ATTEMPTS) {
                    log.warn("Attempt {}/{} failed to send email to {}, retrying", attempt, MAX_ATTEMPTS, toEmail);
                    sleepBeforeRetry();
                }
            }
        }
        throw lastException;
    }

    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String buildActionEmail(String title, String intro, String buttonText, String buttonUrl, String footnote) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                  <div style="background:#fd5f00;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                    <h1 style="color:#fff;margin:0;font-size:24px">%s</h1>
                  </div>
                  <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                    <h2 style="margin:0 0 12px;font-size:20px">%s</h2>
                    <p style="margin:0 0 24px;color:#6b7280;font-size:15px;line-height:1.5">%s</p>
                    <a href="%s" style="display:block;background:#fd5f00;color:#fff;text-align:center;padding:14px;border-radius:8px;text-decoration:none;font-weight:600;font-size:16px">%s</a>
                    <p style="margin:24px 0 0;color:#9ca3af;font-size:13px">%s</p>
                  </div>
                </div>
                """.formatted(companyName, title, intro, buttonUrl, buttonText, footnote);
    }

    private String buildDetailsEmail(String title, Map<String, String> details, String bodyHtml, String footerNote) {
        StringBuilder rows = new StringBuilder();
        details.forEach((key, value) -> rows.append("""
                <tr>
                  <td style="padding:6px 0;color:#6b7280;font-size:14px">%s</td>
                  <td style="padding:6px 0;color:#1a1a1a;font-size:14px;font-weight:600;text-align:right">%s</td>
                </tr>
                """.formatted(key, value)));
        String footer = footerNote != null
                ? "<p style=\"margin:24px 0 0;color:#9ca3af;font-size:13px\">" + footerNote + "</p>"
                : "";

        return """
                <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                  <div style="background:#fd5f00;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                    <h1 style="color:#fff;margin:0;font-size:24px">%s</h1>
                  </div>
                  <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                    <h2 style="margin:0 0 16px;font-size:18px">%s</h2>
                    <table style="width:100%%;border-collapse:collapse;margin-bottom:20px">%s</table>
                    <div style="color:#6b7280;font-size:14px;line-height:1.6">%s</div>
                    %s
                  </div>
                </div>
                """.formatted(companyName, title, rows, bodyHtml, footer);
    }

    private String buildPlainEmail(String title, String bodyHtml) {
        return """
                <div style="max-width:480px;margin:0 auto;font-family:Arial,sans-serif;color:#1a1a1a">
                  <div style="background:#fd5f00;padding:24px;text-align:center;border-radius:12px 12px 0 0">
                    <h1 style="color:#fff;margin:0;font-size:24px">%s</h1>
                  </div>
                  <div style="background:#fff;padding:32px 24px;border:1px solid #e5e7eb;border-top:none;border-radius:0 0 12px 12px">
                    <h2 style="margin:0 0 12px;font-size:18px">%s</h2>
                    <div style="color:#6b7280;font-size:15px;line-height:1.6">%s</div>
                  </div>
                </div>
                """.formatted(companyName, title, bodyHtml);
    }
}
