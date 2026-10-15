package ua.lviv.bas.cinema.config.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class LogEmailMaskingTest {

    private static final Pattern MASKED_MSG_PROPERTY = Pattern.compile("name=\"MASKED_MSG\" value=\"([^\"]+)\"");

    @Test
    void logMessagesShouldMaskEmailLocalPart() throws IOException {
        String formatted = format("register - email: antonbastest@gmail.com, backup: a@b.ua");

        assertThat(formatted)
                .isEqualTo("register - email: a***@gmail.com, backup: a***@b.ua")
                .doesNotContain("antonbastest");
    }

    @Test
    void logMessagesWithoutEmailShouldStayUnchanged() throws IOException {
        assertThat(format("Payment 16 SUCCESS")).isEqualTo("Payment 16 SUCCESS");
    }

    private String format(String message) throws IOException {
        LoggerContext context = new LoggerContext();
        PatternLayout layout = new PatternLayout();
        layout.setContext(context);
        layout.setPattern(readMaskedMessagePattern());
        layout.start();

        Logger logger = context.getLogger(LogEmailMaskingTest.class);
        return layout.doLayout(new LoggingEvent(Logger.class.getName(), logger, Level.INFO, message, null, null));
    }

    private String readMaskedMessagePattern() throws IOException {
        String config = new ClassPathResource("logback-spring.xml").getContentAsString(StandardCharsets.UTF_8);
        Matcher matcher = MASKED_MSG_PROPERTY.matcher(config);
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }
}
