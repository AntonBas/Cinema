package ua.lviv.bas.cinema.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.refund-policy")
public class RefundPolicyConfig {
    private String processingTime;
    private String contactEmail;
}

