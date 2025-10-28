package be.ucll.backend2.integration.database;

import be.ucll.backend2.model.Movie;
import be.ucll.backend2.repository.MovieRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql("classpath:schema.sql") // Voer schema.sql uit
public class MovieRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void givenThereIsAMovie_whenFindAllIsCalled_thenMovieIsReturned() {
        // Given
        entityManager.persistAndFlush(
                new Movie("The Shawshank Redemption",
                        "Frank Darabont",
                        1994));

        // When
        final var movies = movieRepository.findAll();

        // Then
        Assertions.assertEquals(1, movies.size());
        Assertions.assertEquals(1L, movies.getFirst().getId());
        Assertions.assertEquals("The Shawshank Redemption", movies.getFirst().getTitle());
        Assertions.assertEquals("Frank Darabont", movies.getFirst().getDirector());
        Assertions.assertEquals(1994, movies.getFirst().getYear());
    }

    @Test
    public void givenThereAreMoviesAfter2000_whenFindByYearAfterIsCalled_thenMoviesAfter2000AreReturned() {
        // Given
        entityManager.persist(
                new Movie("The Shawshank Redemption",
                        "Frank Darabont",
                        1994));
        entityManager.persist(
                new Movie("The Godfather",
                        "Francis Ford Coppola",
                        1972));
        entityManager.persist(
                new Movie("The Dark Knight",
                        "Christopher Nolan",
                        2008));
        entityManager.flush();

        // When
        final var movies = movieRepository.findByYearAfter(2000);

        // Then
        Assertions.assertEquals(1, movies.size());
        Assertions.assertEquals(3L, movies.getFirst().getId());
        Assertions.assertEquals("The Dark Knight", movies.getFirst().getTitle());
        Assertions.assertEquals("Christopher Nolan", movies.getFirst().getDirector());
        Assertions.assertEquals(2008, movies.getFirst().getYear());
    }

    @Test
    public void givenThereAreNoMoviesInDb_whenFindByYearAfterIsCalled_thenEmptyListIsReturned() {
        final var movies = movieRepository.findByYearAfter(2000);

        Assertions.assertTrue(movies.isEmpty());
    }

    // findByYearBefore

    @Test
    public void givenThereAreNoMoviesBefore2000_whenFindByYearBefore2000IsCalled_thenEmptyListIsReturned() {
        // Given
        entityManager.persist(
                new Movie("The Dark Knight",
                        "Christopher Nolan",
                        2008));

        // When
        final var movies = movieRepository.findByYearBefore(2000);

        // Then
        Assertions.assertTrue(movies.isEmpty());
    }

    @Test
    public void givenThereAreMoviesBefore2000_whenFindByYearBefore2000IsCalled_thenMoviesBefore2000Returned() {
        // Given
        entityManager.persist(
                new Movie("The Shawshank Redemption",
                        "Frank Darabont",
                        1994));
        entityManager.persist(
                new Movie("The Godfather",
                        "Francis Ford Coppola",
                        1972));
        entityManager.persist(
                new Movie("The Dark Knight",
                        "Christopher Nolan",
                        2008));

        // When
        final var movies = movieRepository.findByYearBefore(2000);

        // Then
        Assertions.assertEquals(2, movies.size());
        Assertions.assertEquals(1L, movies.getFirst().getId());
        Assertions.assertEquals("The Shawshank Redemption", movies.getFirst().getTitle());
        Assertions.assertEquals("Frank Darabont", movies.getFirst().getDirector());
        Assertions.assertEquals(1994, movies.getFirst().getYear());
        Assertions.assertEquals(2L, movies.get(1).getId());
        Assertions.assertEquals("The Godfather", movies.get(1).getTitle());
        Assertions.assertEquals("Francis Ford Coppola", movies.get(1).getDirector());
        Assertions.assertEquals(1972, movies.get(1).getYear());
    }
}
