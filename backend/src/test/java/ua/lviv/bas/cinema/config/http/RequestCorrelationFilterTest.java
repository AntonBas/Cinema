package ua.lviv.bas.cinema.config.http;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var generatedId = response.getHeader(RequestCorrelationFilter.CORRELATION_ID_HEADER);
        assertThat(generatedId).isNotBlank();
        assertThat(UUID.fromString(generatedId)).isNotNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void reusesIncomingCorrelationIdHeader() throws Exception {
        var incomingId = "existing-correlation-id";
        var request = new MockHttpServletRequest();
        request.addHeader(RequestCorrelationFilter.CORRELATION_ID_HEADER, incomingId);
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestCorrelationFilter.CORRELATION_ID_HEADER)).isEqualTo(incomingId);
    }

    @Test
    void clearsMdcAfterRequestCompletes() throws Exception {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
                assertThat(MDC.get(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY)).isNotBlank();

        filter.doFilter(request, response, chain);

        assertThat(MDC.get(RequestCorrelationFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }
}
