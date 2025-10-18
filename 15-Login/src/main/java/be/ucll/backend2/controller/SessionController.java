package be.ucll.backend2.controller;

import be.ucll.backend2.controller.dto.AuthenticationRequest;
import be.ucll.backend2.controller.dto.AuthenticationResponse;
import be.ucll.backend2.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        final var token = sessionService.authenticate(
                authenticationRequest.emailAddress(),
                authenticationRequest.password()
        );
        return new AuthenticationResponse(token);
    }
}
