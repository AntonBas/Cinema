package ua.lviv.bas.cinema.ticket.email;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TicketsIssuedEmailListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private TicketsEmailService ticketsEmailService;

    @Test
    void ticketsEmailShouldBeSentOnlyAfterCommit() {
        new TransactionTemplate(transactionManager)
                .executeWithoutResult(status -> eventPublisher.publishEvent(new TicketsIssuedEvent(101L)));

        verify(ticketsEmailService, timeout(5000)).sendAndMarkSent(101L);
    }

    @Test
    void ticketsEmailShouldNotBeSentWhenTransactionRollsBack() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publishEvent(new TicketsIssuedEvent(202L));
            status.setRollbackOnly();
        });

        verify(ticketsEmailService, after(1000).never()).sendAndMarkSent(202L);
    }
}
