package be.ucll.backend2.service;

import be.ucll.backend2.controller.dto.CreateMovieDto;
import be.ucll.backend2.model.Movie;
import be.ucll.backend2.repository.MovieRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> getMoviesBetween(Optional<Integer> startYear, Optional<Integer> endYear) {
        if (startYear.isPresent() && endYear.isPresent()) {
            return movieRepository.findByYearBetween(startYear.get(), endYear.get());
        } else if (startYear.isPresent()) {
            return movieRepository.findByYearAfter(startYear.get());
        } else if (endYear.isPresent()) {
            return movieRepository.findByYearBefore(endYear.get());
        } else {
            return movieRepository.findAll();
        }
    }

    public Movie createMovie(CreateMovieDto movie) {
        var movieToSave = new Movie(
                movie.title(),
                movie.director(),
                movie.year()
        );
        return movieRepository.save(movieToSave);
    }
}
