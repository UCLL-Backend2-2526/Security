package be.ucll.backend2.integration.http;

import be.ucll.backend2.config.SecurityConfig;
import be.ucll.backend2.controller.MovieController;
import be.ucll.backend2.controller.dto.CreateMovieDto;
import be.ucll.backend2.model.Movie;
import be.ucll.backend2.service.MovieService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
@AutoConfigureRestTestClient
public class MovieControllerTest {
    @Autowired
    private RestTestClient client;

    @MockitoBean
    private MovieService movieService;

    // Happy: film wordt aangemaakt

    @Test
    @WithMockUser(username = "jos@example.com", roles = {"READER", "EDITOR"})
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
            .body(createMovieDto)
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
