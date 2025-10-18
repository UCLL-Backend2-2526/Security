package be.ucll.backend2.service;

import be.ucll.backend2.model.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public SessionService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String authenticate(String emailAddress, String password) {
        final var usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(emailAddress, password);
        final var authentication = authenticationManager.authenticate(usernamePasswordAuthentication);
        final var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }
}
