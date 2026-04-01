package ua.lviv.bas.cinema.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.service.booking.RefundService;

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

	@Test
	void refundTicket_ShouldProcessSuccessfully() {
		User user = createUser(1L, "user@example.com");
		CustomUserDetails userDetails = new CustomUserDetails(user);
		RefundRequest request = new RefundRequest(100L, "Change of plans");

		RefundResponse refundResponse = createRefundResponse(1L);

		when(refundService.processRefund(any(RefundRequest.class), eq(user.getId()))).thenReturn(refundResponse);

		ResponseEntity<RefundResponse> response = refundController.refundTicket(request, userDetails);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().id()).isEqualTo(1L);
		assertThat(response.getBody().refundNumber()).isEqualTo("RF-2024-0001");
	}
}