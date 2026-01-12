package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
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
		user.setFirstName("John");
		user.setLastName("Doe");
		return user;
	}

	private RefundPreviewResponse createRefundPreviewResponse(Long ticketId, BigDecimal originalPrice,
			BigDecimal refundAmount, BigDecimal feeAmount, Boolean isRefundable, String nonRefundableReason) {
		return RefundPreviewResponse.builder().ticketId(ticketId).ticketCode("TK2024000" + ticketId)
				.movieTitle("Interstellar").sessionTime(LocalDateTime.now().plusDays(1)).hallName("Hall 1")
				.seatInfo("Row 5, Seat 12").originalPrice(originalPrice).finalPrice(originalPrice)
				.refundAmount(refundAmount)
				.refundPercentage(refundAmount.divide(originalPrice).multiply(BigDecimal.valueOf(100)))
				.feeAmount(feeAmount).feePercentage(feeAmount.divide(originalPrice).multiply(BigDecimal.valueOf(100)))
				.bonusPointsUsed(100).bonusPointsToRefund(90).policyName("Standard Refund")
				.policyDescription("90% refund 2-24 hours before session").isRefundable(isRefundable)
				.nonRefundableReason(nonRefundableReason).refundDeadline(LocalDateTime.now().plusHours(2))
				.remainingTime("2 hours").purchaseTime("15.12.2024 10:15").ticketType("Standard").build();
	}

	private RefundResponse createRefundResponse(Long id, String refundNumber, BigDecimal totalAmount, String status,
			LocalDateTime processedAt) {
		return RefundResponse.builder().id(id).refundNumber(refundNumber).status(status).totalAmount(totalAmount)
				.totalBonusPointsToDeduct(90).reason("Change of plans").processedBy("AUTO_SYSTEM")
				.processedAt(processedAt).createdAt(LocalDateTime.now().minusMinutes(5)).paymentId(456L)
				.paymentMethod("CARD").items(Arrays.asList()).message("Refund processed successfully")
				.estimatedRefundTime("3-5 business days").build();
	}

	@Test
	void getRefundPreview_ShouldReturnPreviewSuccessfully() {
		User user = createUser(1L, "user@example.com");
		RefundPreviewRequest request = new RefundPreviewRequest();
		request.setTicketId(100L);

		RefundPreviewResponse previewResponse = createRefundPreviewResponse(100L, new BigDecimal("250.00"),
				new BigDecimal("200.00"), new BigDecimal("50.00"), true, null);

		when(refundService.getRefundPreview(request, user.getId())).thenReturn(previewResponse);

		ResponseEntity<RefundPreviewResponse> response = refundController.getRefundPreview(request, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(100L, response.getBody().getTicketId());
		assertEquals(new BigDecimal("200.00"), response.getBody().getRefundAmount());
		assertEquals(true, response.getBody().getIsRefundable());
		verify(refundService).getRefundPreview(request, user.getId());
	}

	@Test
	void getRefundPreview_ShouldThrowException_WhenTicketNotFound() {
		User user = createUser(1L, "user@example.com");
		RefundPreviewRequest request = new RefundPreviewRequest();
		request.setTicketId(999L);

		when(refundService.getRefundPreview(request, user.getId())).thenThrow(new RuntimeException("Ticket not found"));

		assertThrows(RuntimeException.class, () -> refundController.getRefundPreview(request, user));
		verify(refundService).getRefundPreview(request, user.getId());
	}

	@Test
	void getRefundPreview_ShouldThrowException_WhenAccessDenied() {
		User user = createUser(1L, "user@example.com");
		RefundPreviewRequest request = new RefundPreviewRequest();
		request.setTicketId(100L);

		when(refundService.getRefundPreview(request, user.getId())).thenThrow(new SecurityException("Access denied"));

		assertThrows(SecurityException.class, () -> refundController.getRefundPreview(request, user));
		verify(refundService).getRefundPreview(request, user.getId());
	}

	@Test
	void getRefundPreview_ShouldReturnNonRefundablePreview() {
		User user = createUser(1L, "user@example.com");
		RefundPreviewRequest request = new RefundPreviewRequest();
		request.setTicketId(200L);

		RefundPreviewResponse previewResponse = createRefundPreviewResponse(200L, new BigDecimal("250.00"),
				BigDecimal.ZERO, new BigDecimal("250.00"), false, "Session has already started");

		when(refundService.getRefundPreview(request, user.getId())).thenReturn(previewResponse);

		ResponseEntity<RefundPreviewResponse> response = refundController.getRefundPreview(request, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(false, response.getBody().getIsRefundable());
		assertEquals("Session has already started", response.getBody().getNonRefundableReason());
		verify(refundService).getRefundPreview(request, user.getId());
	}

	@Test
	void processRefund_ShouldProcessSuccessfully() {
		User user = createUser(1L, "user@example.com");
		RefundRequest request = new RefundRequest();
		request.setTicketId(100L);
		request.setReason("Change of plans");

		RefundResponse refundResponse = createRefundResponse(1L, "RF-2024-0001", new BigDecimal("200.00"), "PROCESSING",
				LocalDateTime.now());

		when(refundService.processRefund(request, user.getId())).thenReturn(refundResponse);

		ResponseEntity<RefundResponse> response = refundController.processRefund(request, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().getId());
		assertEquals("RF-2024-0001", response.getBody().getRefundNumber());
		assertEquals("PROCESSING", response.getBody().getStatus());
		verify(refundService).processRefund(request, user.getId());
	}

	@Test
	void processRefund_ShouldThrowException_WhenTicketNotRefundable() {
		User user = createUser(1L, "user@example.com");
		RefundRequest request = new RefundRequest();
		request.setTicketId(200L);
		request.setReason("Too late");

		when(refundService.processRefund(request, user.getId()))
				.thenThrow(new RuntimeException("Ticket is not refundable"));

		assertThrows(RuntimeException.class, () -> refundController.processRefund(request, user));
		verify(refundService).processRefund(request, user.getId());
	}

	@Test
	void processRefund_ShouldThrowException_WhenAccessDenied() {
		User user = createUser(1L, "user@example.com");
		RefundRequest request = new RefundRequest();
		request.setTicketId(100L);

		when(refundService.processRefund(request, user.getId())).thenThrow(new SecurityException("Access denied"));

		assertThrows(SecurityException.class, () -> refundController.processRefund(request, user));
		verify(refundService).processRefund(request, user.getId());
	}

	@Test
	void processRefund_ShouldThrowException_WhenProcessingFailed() {
		User user = createUser(1L, "user@example.com");
		RefundRequest request = new RefundRequest();
		request.setTicketId(100L);

		when(refundService.processRefund(request, user.getId()))
				.thenThrow(new RuntimeException("Refund processing failed"));

		assertThrows(RuntimeException.class, () -> refundController.processRefund(request, user));
		verify(refundService).processRefund(request, user.getId());
	}

	@Test
	void getUserRefunds_ShouldReturnRefundsList() {
		User user = createUser(1L, "user@example.com");

		RefundResponse refund1 = createRefundResponse(1L, "RF-2024-0001", new BigDecimal("200.00"), "COMPLETED",
				LocalDateTime.now().minusDays(2));
		RefundResponse refund2 = createRefundResponse(2L, "RF-2024-0002", new BigDecimal("150.00"), "PROCESSING",
				LocalDateTime.now().minusDays(1));
		List<RefundResponse> refunds = Arrays.asList(refund1, refund2);

		when(refundService.getUserRefunds(user.getId())).thenReturn(refunds);

		ResponseEntity<List<RefundResponse>> response = refundController.getUserRefunds(user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		assertEquals("RF-2024-0001", response.getBody().get(0).getRefundNumber());
		assertEquals("RF-2024-0002", response.getBody().get(1).getRefundNumber());
		verify(refundService).getUserRefunds(user.getId());
	}

	@Test
	void getUserRefunds_ShouldReturnEmptyList() {
		User user = createUser(1L, "user@example.com");

		List<RefundResponse> emptyList = Arrays.asList();

		when(refundService.getUserRefunds(user.getId())).thenReturn(emptyList);

		ResponseEntity<List<RefundResponse>> response = refundController.getUserRefunds(user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().size());
		verify(refundService).getUserRefunds(user.getId());
	}

	@Test
	void getUserRefunds_ShouldThrowException_WhenUnauthorized() {
		User user = createUser(null, null);

		when(refundService.getUserRefunds(user.getId())).thenThrow(new SecurityException("Unauthorized"));

		assertThrows(SecurityException.class, () -> refundController.getUserRefunds(user));
		verify(refundService).getUserRefunds(user.getId());
	}

	@Test
	void processRefund_ShouldHandleCompletedRefund() {
		User user = createUser(1L, "user@example.com");
		RefundRequest request = new RefundRequest();
		request.setTicketId(100L);
		request.setReason("Successful refund");

		RefundResponse refundResponse = createRefundResponse(1L, "RF-2024-0001", new BigDecimal("200.00"), "COMPLETED",
				LocalDateTime.now());

		when(refundService.processRefund(request, user.getId())).thenReturn(refundResponse);

		ResponseEntity<RefundResponse> response = refundController.processRefund(request, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("COMPLETED", response.getBody().getStatus());
		verify(refundService).processRefund(request, user.getId());
	}

	@Test
	void getRefundPreview_ShouldHandleFullRefund() {
		User user = createUser(1L, "user@example.com");
		RefundPreviewRequest request = new RefundPreviewRequest();
		request.setTicketId(300L);

		RefundPreviewResponse previewResponse = createRefundPreviewResponse(300L, new BigDecimal("250.00"),
				new BigDecimal("250.00"), BigDecimal.ZERO, true, null);

		when(refundService.getRefundPreview(request, user.getId())).thenReturn(previewResponse);

		ResponseEntity<RefundPreviewResponse> response = refundController.getRefundPreview(request, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(new BigDecimal("250.00"), response.getBody().getRefundAmount());
		assertEquals(BigDecimal.ZERO, response.getBody().getFeeAmount());
		verify(refundService).getRefundPreview(request, user.getId());
	}

	@Test
	void processRefund_ShouldThrowException_WhenTicketNotFound() {
		User user = createUser(1L, "user@example.com");
		RefundRequest request = new RefundRequest();
		request.setTicketId(999L);

		when(refundService.processRefund(request, user.getId())).thenThrow(new RuntimeException("Ticket not found"));

		assertThrows(RuntimeException.class, () -> refundController.processRefund(request, user));
		verify(refundService).processRefund(request, user.getId());
	}

	@Test
	void getRefundPreview_ShouldHandleZeroRefund() {
		User user = createUser(1L, "user@example.com");
		RefundPreviewRequest request = new RefundPreviewRequest();
		request.setTicketId(400L);

		RefundPreviewResponse previewResponse = createRefundPreviewResponse(400L, new BigDecimal("250.00"),
				BigDecimal.ZERO, new BigDecimal("250.00"), false, "Session has already ended");

		when(refundService.getRefundPreview(request, user.getId())).thenReturn(previewResponse);

		ResponseEntity<RefundPreviewResponse> response = refundController.getRefundPreview(request, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(BigDecimal.ZERO, response.getBody().getRefundAmount());
		assertEquals(false, response.getBody().getIsRefundable());
		verify(refundService).getRefundPreview(request, user.getId());
	}

	@Test
	void processRefund_ShouldHandleRefundWithBonusPoints() {
		User user = createUser(1L, "user@example.com");
		RefundRequest request = new RefundRequest();
		request.setTicketId(100L);
		request.setReason("With bonus points");

		RefundResponse refundResponse = RefundResponse.builder().id(1L).refundNumber("RF-2024-0003").status("COMPLETED")
				.totalAmount(new BigDecimal("180.00")).totalBonusPointsToDeduct(100).reason("With bonus points")
				.processedBy("AUTO_SYSTEM").processedAt(LocalDateTime.now())
				.createdAt(LocalDateTime.now().minusMinutes(5)).paymentId(456L).paymentMethod("CARD")
				.items(Arrays.asList()).message("Refund processed successfully with bonus points deduction")
				.estimatedRefundTime("3-5 business days").build();

		when(refundService.processRefund(request, user.getId())).thenReturn(refundResponse);

		ResponseEntity<RefundResponse> response = refundController.processRefund(request, user);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(100, response.getBody().getTotalBonusPointsToDeduct());
		verify(refundService).processRefund(request, user.getId());
	}
}