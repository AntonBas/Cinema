package ua.lviv.bas.cinema.refund.controller.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.refund.dto.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.refund.dto.request.RefundRequest;
import ua.lviv.bas.cinema.refund.dto.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.refund.dto.response.RefundResponse;
import ua.lviv.bas.cinema.refund.service.RefundService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefundControllerTest {

    @Mock
    private RefundService refundService;

    @InjectMocks
    private RefundController refundController;

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private RefundResponse createRefundResponse(Long id) {
        return new RefundResponse(id, "RF-2024-0001", "PROCESSING", new BigDecimal("200.00"), 90, "Change of plans",
                "AUTO_SYSTEM", LocalDateTime.now(), LocalDateTime.now().minusMinutes(5), 456L, "CARD", null,
                "Refund processed successfully", "3-5 business days");
    }

    private RefundPreviewResponse createPreviewResponse(Long ticketId) {
        return new RefundPreviewResponse(ticketId, "TK2024000123", "Interstellar", LocalDateTime.now().plusHours(3),
                "Hall 1", "Row 5, Seat 12", new BigDecimal("200.00"), new BigDecimal("200.00"),
                new BigDecimal("180.00"), new BigDecimal("90.00"), new BigDecimal("20.00"), new BigDecimal("10.00"),
                100, 90, "Standard Refund", "90% refund 2-24 hours before session", true, null,
                LocalDateTime.now().plusHours(1), "3 hours", LocalDateTime.now().minusHours(1).toString(),
                "Standard");
    }

    @Test
    void refundShouldProcessSuccessfully() {
        User user = createUser(1L, "user@example.com");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        RefundRequest request = new RefundRequest(100L, "Change of plans");

        RefundResponse refundResponse = createRefundResponse(1L);

        when(refundService.refund(any(RefundRequest.class), eq(user.getId()))).thenReturn(refundResponse);

        RefundResponse response = refundController.refund(request, userDetails);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.refundNumber()).isEqualTo("RF-2024-0001");
        assertThat(response.totalAmount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(response.reason()).isEqualTo("Change of plans");
    }

    @Test
    void refundShouldCallServiceWithCorrectParameters() {
        User user = createUser(2L, "another@example.com");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        RefundRequest request = new RefundRequest(200L, "Wrong ticket");

        RefundResponse refundResponse = createRefundResponse(2L);

        when(refundService.refund(eq(request), eq(2L))).thenReturn(refundResponse);

        RefundResponse response = refundController.refund(request, userDetails);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(2L);
    }

    @Test
    void previewShouldReturnCalculatedPreview() {
        User user = createUser(1L, "user@example.com");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        RefundPreviewRequest request = new RefundPreviewRequest(100L);

        RefundPreviewResponse previewResponse = createPreviewResponse(100L);

        when(refundService.getPreview(any(RefundPreviewRequest.class), eq(user.getId()))).thenReturn(previewResponse);

        RefundPreviewResponse response = refundController.preview(request, userDetails);

        assertThat(response).isNotNull();
        assertThat(response.ticketId()).isEqualTo(100L);
        assertThat(response.refundAmount()).isEqualTo(new BigDecimal("180.00"));
        assertThat(response.isRefundable()).isTrue();
    }

    @Test
    void previewShouldCallServiceWithCorrectParameters() {
        User user = createUser(2L, "another@example.com");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        RefundPreviewRequest request = new RefundPreviewRequest(200L);

        RefundPreviewResponse previewResponse = createPreviewResponse(200L);

        when(refundService.getPreview(eq(request), eq(2L))).thenReturn(previewResponse);

        RefundPreviewResponse response = refundController.preview(request, userDetails);

        assertThat(response).isNotNull();
        assertThat(response.ticketId()).isEqualTo(200L);
    }
}