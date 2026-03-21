package ua.lviv.bas.cinema.service.booking.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.domain.projection.TicketTypeUserProjection;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

@ExtendWith(MockitoExtension.class)
public class TicketTypeServiceTest {

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketTypeMapper ticketTypeMapper;

	@Mock
	private TicketTypeValidationService validationService;

	@InjectMocks
	private TicketTypeService ticketTypeService;

	private final Long TICKET_TYPE_ID = 1L;
	private final String DISPLAY_NAME = "Adult Ticket";
	private final BigDecimal PRICE_MULTIPLIER = new BigDecimal("1.0");

	@Test
	void createTicketType_Success() {
		TicketTypeCreateRequest request = createTicketTypeRequest();
		TicketType ticketType = createTicketType();
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.existsByDisplayName(DISPLAY_NAME)).thenReturn(false);
		when(ticketTypeMapper.toTicketType(request)).thenReturn(ticketType);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.createTicketType(request);

		assertThat(result).isEqualTo(response);
		verify(validationService).validateAgeRange(18, 65);
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void createTicketType_DuplicateDisplayName_ThrowsException() {
		TicketTypeCreateRequest request = createTicketTypeRequest();

		when(ticketTypeRepository.existsByDisplayName(DISPLAY_NAME)).thenReturn(true);

		assertThatThrownBy(() -> ticketTypeService.createTicketType(request))
				.isInstanceOf(TicketTypeDuplicateException.class);
	}

	@Test
	void getTicketTypeById_Success() {
		TicketType ticketType = createTicketType();
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.getTicketTypeById(TICKET_TYPE_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getTicketTypeById_NotFound_ThrowsException() {
		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketTypeService.getTicketTypeById(TICKET_TYPE_ID))
				.isInstanceOf(TicketTypeNotFoundException.class);
	}

	@Test
	void getTicketTypesForAdmin_Success() {
		Pageable pageable = Pageable.ofSize(10);

		when(ticketTypeRepository.findAdminProjections(true, TicketTypeCategory.STANDARD, "search", pageable))
				.thenReturn(Page.empty());

		var result = ticketTypeService.getTicketTypesForAdmin(true, TicketTypeCategory.STANDARD, "search", pageable);

		assertThat(result).isNotNull();
	}

	@Test
	void getActiveTicketTypesForUser_Success() {
		TicketTypeUserProjection projection = createUserProjection();
		TicketTypeUserResponse response = createTicketTypeUserResponse();

		when(ticketTypeRepository.findUserProjections()).thenReturn(List.of(projection));
		when(ticketTypeMapper.toTicketTypeUserResponse(projection)).thenReturn(response);

		List<TicketTypeUserResponse> result = ticketTypeService.getActiveTicketTypesForUser();

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void updateTicketType_Success() {
		TicketType ticketType = createTicketType();
		TicketTypeUpdateRequest request = new TicketTypeUpdateRequest("Updated Name", null, 21, 70, null, null, null,
				null);
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.updateTicketType(TICKET_TYPE_ID, request);

		assertThat(result).isEqualTo(response);
		verify(validationService).validateAgeRange(21, 70);
		verify(ticketTypeMapper).updateTicketTypeFromRequest(ticketType, request);
	}

	@Test
	void updateTicketType_DuplicateDisplayName_ThrowsException() {
		TicketType ticketType = createTicketType();
		TicketTypeUpdateRequest request = new TicketTypeUpdateRequest("Duplicate Name", null, null, null, null, null,
				null, null);

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.existsByDisplayNameAndIdNot("Duplicate Name", TICKET_TYPE_ID)).thenReturn(true);

		assertThatThrownBy(() -> ticketTypeService.updateTicketType(TICKET_TYPE_ID, request))
				.isInstanceOf(TicketTypeDuplicateException.class);
	}

	@Test
	void deleteTicketType_Success() {
		TicketType ticketType = createTicketType();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(0L);

		ticketTypeService.deleteTicketType(TICKET_TYPE_ID);

		verify(ticketTypeRepository).delete(ticketType);
	}

	@Test
	void deleteTicketType_WithFutureTickets_ThrowsException() {
		TicketType ticketType = createTicketType();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(3L);

		assertThatThrownBy(() -> ticketTypeService.deleteTicketType(TICKET_TYPE_ID))
				.isInstanceOf(TicketTypeInUseException.class);
	}

	@Test
	void toggleTicketTypeActiveStatus_ActivateSuccess() {
		TicketType ticketType = createTicketType();
		ticketType.setActive(false);
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.toggleTicketTypeActiveStatus(TICKET_TYPE_ID);

		assertThat(result).isEqualTo(response);
		assertThat(ticketType.isActive()).isTrue();
		verify(ticketRepository, never()).countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(any(), any(),
				any());
	}

	@Test
	void toggleTicketTypeActiveStatus_DeactivateSuccess() {
		TicketType ticketType = createTicketType();
		ticketType.setActive(true);
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(0L);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.toggleTicketTypeActiveStatus(TICKET_TYPE_ID);

		assertThat(result).isEqualTo(response);
		assertThat(ticketType.isActive()).isFalse();
	}

	@Test
	void toggleTicketTypeActiveStatus_DeactivateWithFutureTickets_ThrowsException() {
		TicketType ticketType = createTicketType();
		ticketType.setActive(true);

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(2L);

		assertThatThrownBy(() -> ticketTypeService.toggleTicketTypeActiveStatus(TICKET_TYPE_ID))
				.isInstanceOf(TicketTypeInUseException.class);
	}

	private TicketTypeCreateRequest createTicketTypeRequest() {
		return new TicketTypeCreateRequest(DISPLAY_NAME, PRICE_MULTIPLIER, 18, 65, false, null, true,
				TicketTypeCategory.STANDARD);
	}

	private TicketType createTicketType() {
		return TicketType.builder().id(TICKET_TYPE_ID).displayName(DISPLAY_NAME).priceMultiplier(PRICE_MULTIPLIER)
				.minAge(18).maxAge(65).category(TicketTypeCategory.STANDARD).active(true).build();
	}

	private TicketTypeResponse createTicketTypeResponse() {
		return new TicketTypeResponse(TICKET_TYPE_ID, DISPLAY_NAME, PRICE_MULTIPLIER, 18, 65, false, null, true,
				TicketTypeCategory.STANDARD);
	}

	private TicketTypeUserResponse createTicketTypeUserResponse() {
		return new TicketTypeUserResponse(TICKET_TYPE_ID, DISPLAY_NAME, PRICE_MULTIPLIER, false, null);
	}

	private TicketTypeUserProjection createUserProjection() {
		return new TicketTypeUserProjection() {
			@Override
			public Long getId() {
				return TICKET_TYPE_ID;
			}

			@Override
			public String getDisplayName() {
				return DISPLAY_NAME;
			}

			@Override
			public BigDecimal getPriceMultiplier() {
				return PRICE_MULTIPLIER;
			}

			@Override
			public boolean isRequiresDocument() {
				return false;
			}

			@Override
			public String getDocumentType() {
				return null;
			}
		};
	}
}