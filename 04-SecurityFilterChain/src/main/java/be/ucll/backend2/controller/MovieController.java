package be.ucll.backend2.controller;

import be.ucll.backend2.controller.dto.CreateMovieDto;
import be.ucll.backend2.model.Movie;
import be.ucll.backend2.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getMovies(@RequestParam Optional<Integer> startYear,
                                 @RequestParam Optional<Integer> endYear) {
        if (startYear.isPresent() || endYear.isPresent()) {
            return movieService.getMoviesBetween(startYear, endYear);
        } else {
            return movieService.getAllMovies();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie createMovie(@RequestBody CreateMovieDto movie) {
        return movieService.createMovie(movie);
    }
}
