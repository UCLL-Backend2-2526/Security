package be.ucll.backend2.service;

import be.ucll.backend2.model.UserDetailsImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtService jwtService;

    public SessionService(AuthenticationConfiguration authenticationConfiguration, JwtService jwtService) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtService = jwtService;
    }

    public String authenticate(String emailAddress, String password) {
        final var usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(emailAddress, password);
        final var authenticationManager = authenticationConfiguration.getAuthenticationManager();
        final var authentication = authenticationManager.authenticate(usernamePasswordAuthentication);
        final var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }
}
