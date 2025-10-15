package be.ucll.backend2.integration.http;

import be.ucll.backend2.controller.MovieController;
import be.ucll.backend2.controller.dto.CreateMovieDto;
import be.ucll.backend2.model.Movie;
import be.ucll.backend2.service.MovieService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebMvcTest(MovieController.class)
public class MovieControllerTest {
    @Autowired
    private WebTestClient client;

    @MockitoBean
    private MovieService movieService;

    // Happy: film wordt aangemaakt

    @Test
    public void givenValidMovie_whenCreateMovieIsCalled_thenMovieIsCreated() {
        final var createMovieDto = new CreateMovieDto(
                "Cars",
                "John Lasseter",
                2006
        );
        final var movie = new Movie(
                "Cars",
                "John Lasseter",
                2006
        );
        movie.setId(1L);
        Mockito.when(movieService.createMovie(createMovieDto)).thenReturn(movie);

        client.post()
                .uri("/api/v1/movies")
//                .header("Content-Type", "application/json")
                .bodyValue(createMovieDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().json("""
                        {
                          "id": 1,
                          "title": "Cars",
                          "director": "John Lasseter",
                          "year": 2006,
                          "actors": []
                        }
                        """, JsonCompareMode.STRICT);
    }
}
