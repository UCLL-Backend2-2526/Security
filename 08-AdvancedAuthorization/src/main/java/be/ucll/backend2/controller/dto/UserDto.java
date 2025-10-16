package be.ucll.backend2.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(
        @Email String emailAddress,
        @NotBlank String password) {
}
