package be.ucll.backend2.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @Email @NotBlank String emailAddress,
        @NotBlank String password
) {
}
