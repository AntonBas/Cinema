package ua.lviv.bas.cinema.ticket.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TicketTypeForeignKeyIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void ticketTypeForeignKeysShouldRestrictDeletionInsteadOfCascading() {
        List<String> deleteRules = jdbcTemplate.queryForList("""
                SELECT rel.relname::text || ':' || con.confdeltype::text
                FROM pg_constraint con
                         JOIN pg_class rel ON rel.oid = con.conrelid
                         JOIN pg_class ref ON ref.oid = con.confrelid
                WHERE con.contype = 'f' AND ref.relname = 'ticket_types'
                """, String.class);

        assertThat(deleteRules).containsExactlyInAnyOrder("tickets:r", "seat_reservations:r");
    }
}
