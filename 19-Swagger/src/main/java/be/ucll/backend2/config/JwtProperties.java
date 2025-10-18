package be.ucll.backend2.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secretKey,
                            @DefaultValue Token token) {
    public record Token(@DefaultValue("self") String issuer,
                        @DefaultValue("30m") Duration lifetime) {}
}
