package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.service.booking.ControllerFacade;

@ExtendWith(MockitoExtension.class)
public class RefundControllerTest {

	@Mock
	private ControllerFacade controllerFacade;

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

		when(controllerFacade.processRefund(request, user.getId())).thenReturn(refundResponse);

		ResponseEntity<RefundResponse> response = refundController.refundTicket(request, userDetails);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().id());
		assertEquals("RF-2024-0001", response.getBody().refundNumber());
	}
}