package be.ucll.backend2.service;

import be.ucll.backend2.model.UserDetailsImpl;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(long id, String emailAddress, Collection<String> roles) {
        final var now = Instant.now();
        // TODO: set expiresAt via property
        final var expiresAt = now.plus(30L, ChronoUnit.MINUTES);
        final var header = JwsHeader.with(MacAlgorithm.HS256).build();
        final var claims = JwtClaimsSet.builder()
                // TODO: set issuer via property
                .issuer("self")
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(String.valueOf(id))
                .claim("email", emailAddress)
                .claim("scope", String.join(" ", roles))
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String generateToken(UserDetailsImpl userDetails) {
        return generateToken(
                userDetails.user().getId(),
                userDetails.user().getEmailAddress(),
                userDetails.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.toString()).toList());
    }
}
