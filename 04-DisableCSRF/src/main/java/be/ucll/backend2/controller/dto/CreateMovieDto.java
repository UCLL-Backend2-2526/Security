package be.ucll.backend2.controller.dto;

public record CreateMovieDto(
        String title,
        String director,
        int year
) {
}
