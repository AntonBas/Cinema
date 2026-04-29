package ua.lviv.bas.cinema.service.ticket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeRequest;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketTypeMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketTypeRepository;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeProjection;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TicketTypeServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketTypeMapper ticketTypeMapper;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TicketTypeService ticketTypeService;

    private final Long TICKET_TYPE_ID = 1L;
    private final String DISPLAY_NAME = "Adult Ticket";
    private final BigDecimal PRICE_MULTIPLIER = new BigDecimal("1.0");

    @BeforeEach
    void setUp() {
        lenient().doNothing().when(auditService).logChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void createTicketTypeShouldSucceed() {
        TicketTypeRequest request = createTicketTypeRequest();
        TicketType ticketType = createTicketType();
        TicketTypeResponse response = createTicketTypeResponse();

        when(ticketTypeRepository.existsByDisplayName(DISPLAY_NAME)).thenReturn(false);
        when(ticketTypeMapper.toTicketType(request)).thenReturn(ticketType);
        when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

        TicketTypeResponse result = ticketTypeService.createTicketType(request);

        assertThat(result).isEqualTo(response);
        verify(ticketTypeRepository).save(ticketType);
    }

    @Test
    void createTicketTypeWithInvalidAgeRangeShouldThrowValidationException() {
        TicketTypeRequest request = new TicketTypeRequest(DISPLAY_NAME, PRICE_MULTIPLIER, 65, 18, false, null, true,
                TicketTypeCategory.STANDARD);

        assertThatThrownBy(() -> ticketTypeService.createTicketType(request))
                .isInstanceOf(TicketTypeValidationException.class).hasMessageContaining("Invalid age range");
    }

    @Test
    void createTicketTypeWithDuplicateDisplayNameShouldThrowException() {
        TicketTypeRequest request = createTicketTypeRequest();

        when(ticketTypeRepository.existsByDisplayName(DISPLAY_NAME)).thenReturn(true);

        assertThatThrownBy(() -> ticketTypeService.createTicketType(request))
                .isInstanceOf(TicketTypeDuplicateException.class);
    }

    @Test
    void getTicketTypesShouldSucceed() {
        Pageable pageable = PageRequest.of(0, 10);
        TicketTypeProjection projection = createProjection();
        Page<TicketTypeProjection> page = new PageImpl<>(java.util.List.of(projection), pageable, 1);
        TicketTypeResponse response = createTicketTypeResponse();

        when(ticketTypeRepository.findProjectionsByFilters(true, TicketTypeCategory.STANDARD, "search", pageable))
                .thenReturn(page);
        when(ticketTypeMapper.toTicketTypeResponse(projection)).thenReturn(response);

        Page<TicketTypeResponse> result = ticketTypeService.getTicketTypes(true, TicketTypeCategory.STANDARD, "search",
                pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateTicketTypeShouldSucceed() {
        TicketType ticketType = createTicketType();
        TicketTypeRequest request = new TicketTypeRequest("Updated Name", null, 21, 70, false, null, true, null);
        TicketTypeResponse response = createTicketTypeResponse();

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

        TicketTypeResponse result = ticketTypeService.updateTicketType(TICKET_TYPE_ID, request);

        assertThat(result).isEqualTo(response);
        verify(ticketTypeMapper).updateTicketTypeFromRequest(request, ticketType);
    }

    @Test
    void updateTicketTypeWithInvalidAgeRangeShouldThrowValidationException() {
        TicketType ticketType = createTicketType();
        TicketTypeRequest request = new TicketTypeRequest(null, null, 65, 21, false, null, true, null);

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(TICKET_TYPE_ID, request))
                .isInstanceOf(TicketTypeValidationException.class).hasMessageContaining("Invalid age range");
    }

    @Test
    void updateTicketTypeWithDuplicateDisplayNameShouldThrowException() {
        TicketType ticketType = createTicketType();
        TicketTypeRequest request = new TicketTypeRequest("Duplicate Name", null, null, null, false, null, true, null);

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketTypeRepository.existsByDisplayNameAndIdNot("Duplicate Name", TICKET_TYPE_ID)).thenReturn(true);

        assertThatThrownBy(() -> ticketTypeService.updateTicketType(TICKET_TYPE_ID, request))
                .isInstanceOf(TicketTypeDuplicateException.class);
    }

    @Test
    void deleteTicketTypeShouldSucceed() {
        TicketType ticketType = createTicketType();

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID),
                anyList(), any(LocalDateTime.class))).thenReturn(0L);

        ticketTypeService.deleteTicketType(TICKET_TYPE_ID);

        verify(ticketTypeRepository).delete(ticketType);
    }

    @Test
    void deleteTicketTypeWithFutureTicketsShouldThrowException() {
        TicketType ticketType = createTicketType();

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID),
                anyList(), any(LocalDateTime.class))).thenReturn(3L);

        assertThatThrownBy(() -> ticketTypeService.deleteTicketType(TICKET_TYPE_ID))
                .isInstanceOf(TicketTypeInUseException.class);
    }

    @Test
    void toggleActiveStatusActivateShouldSucceed() {
        TicketType ticketType = createTicketType();
        ticketType.setActive(false);
        TicketTypeResponse response = createTicketTypeResponse();

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

        TicketTypeResponse result = ticketTypeService.toggleActiveStatus(TICKET_TYPE_ID);

        assertThat(result).isEqualTo(response);
        assertThat(ticketType.isActive()).isTrue();
        verify(ticketRepository, never()).countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(any(),
                anyList(), any());
    }

    @Test
    void toggleActiveStatusDeactivateShouldSucceed() {
        TicketType ticketType = createTicketType();
        ticketType.setActive(true);
        TicketTypeResponse response = createTicketTypeResponse();

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID),
                anyList(), any(LocalDateTime.class))).thenReturn(0L);
        when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
        when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

        TicketTypeResponse result = ticketTypeService.toggleActiveStatus(TICKET_TYPE_ID);

        assertThat(result).isEqualTo(response);
        assertThat(ticketType.isActive()).isFalse();
    }

    @Test
    void toggleActiveStatusDeactivateWithFutureTicketsShouldThrowException() {
        TicketType ticketType = createTicketType();
        ticketType.setActive(true);

        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
        when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID),
                anyList(), any(LocalDateTime.class))).thenReturn(2L);

        assertThatThrownBy(() -> ticketTypeService.toggleActiveStatus(TICKET_TYPE_ID))
                .isInstanceOf(TicketTypeInUseException.class);
    }

    @Test
    void toggleActiveStatusWhenNotFoundShouldThrowException() {
        when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketTypeService.toggleActiveStatus(TICKET_TYPE_ID))
                .isInstanceOf(TicketTypeNotFoundException.class);
    }

    private TicketTypeRequest createTicketTypeRequest() {
        return new TicketTypeRequest(DISPLAY_NAME, PRICE_MULTIPLIER, 18, 65, false, null, true,
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

    private TicketTypeProjection createProjection() {
        return new TicketTypeProjection() {
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
            public Integer getMinAge() {
                return 18;
            }

            @Override
            public Integer getMaxAge() {
                return 65;
            }

            @Override
            public boolean isRequiresDocument() {
                return false;
            }

            @Override
            public String getDocumentType() {
                return null;
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public TicketTypeCategory getCategory() {
                return TicketTypeCategory.STANDARD;
            }
        };
    }
}