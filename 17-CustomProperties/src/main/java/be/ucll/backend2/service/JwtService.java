package be.ucll.backend2.service;

import be.ucll.backend2.config.JwtProperties;
import be.ucll.backend2.model.UserDetailsImpl;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(long id, String emailAddress, Collection<String> roles) {
        final var now = Instant.now();
        final var expiresAt = now.plus(jwtProperties.token().lifetime());
        final var header = JwsHeader.with(MacAlgorithm.HS256).build();
        final var claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.token().issuer())
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
