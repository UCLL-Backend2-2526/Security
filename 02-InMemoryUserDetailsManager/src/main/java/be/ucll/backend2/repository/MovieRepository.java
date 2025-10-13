package be.ucll.backend2.repository;

import be.ucll.backend2.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByYearAfter(int year);
    List<Movie> findByYearBefore(int year);
    List<Movie> findByYearBetween(int yearStart, int yearEnd);
}
