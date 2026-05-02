package ua.lviv.bas.cinema.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.refund-policy")
public class RefundPolicyConfig {
    private List<RefundRules> rules = new ArrayList<>();
    private String processingTime;
    private String contactEmail;

    @Data
    public static class RefundRules {
        private String name;
        private String description;
        private int percentage;
        private String condition;
    }
}

