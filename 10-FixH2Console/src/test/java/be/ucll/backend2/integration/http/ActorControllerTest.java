package be.ucll.backend2.integration.http;

import be.ucll.backend2.config.SecurityConfig;
import be.ucll.backend2.controller.ActorController;
import be.ucll.backend2.exception.ActorNotFoundException;
import be.ucll.backend2.model.Actor;
import be.ucll.backend2.service.ActorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@WebMvcTest(ActorController.class)
@Import(SecurityConfig.class)
public class ActorControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private ActorService actorService;

    @Test
    @WithMockUser(username = "jos@example.com", roles = {"READER", "EDITOR"})
    public void givenActorWithIdExists_whenDeleteActorIsCalled_thenActorIsDeleted()
            throws ActorNotFoundException {
        client.delete()
                .uri("/api/v1/actors/{id}", 1L)
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(actorService).deleteActor(1L);
    }

    @Test
    @WithMockUser(username = "jos@example.com", roles = {"READER", "EDITOR"})
    public void givenActorWithIdDoesNotExist_whenDeleteActorIsCalled_then404IsReturned() throws ActorNotFoundException {
        Mockito.doThrow(new ActorNotFoundException(1L)).when(actorService).deleteActor(1L);

        client.delete()
                .uri("/api/v1/actors/{id}", 1L)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().json("""
                                   {
                                     "message": "Actor not found for id: 1"
                                   }
                                   """,
                        JsonCompareMode.STRICT);
    }

    @Test
    @WithMockUser(username = "jos@example.com", roles = {"READER"})
    public void givenNoActorsExist_whenGetAllActorsIsCalled_thenEmptyListIsReturned() {
        // Given
        Mockito.when(actorService.getAllActors()).thenReturn(List.of());

        // When
        client.get()
                .uri("/api/v1/actors")
                .exchange()
        // Then
                .expectStatus().isOk()
                .expectBody().json("[]", JsonCompareMode.STRICT);
    }

    @Test
    @WithMockUser(username = "jos@example.com", roles = {"READER"})
    public void givenThereAreActors_whenGetAllActorsIsCalled_thenListOfActorsIsReturned() {
        // Given
        final var brad = new Actor("Brad Pitt");
        brad.setId(1L);
        final var tom = new Actor("Tom Cruise");
        tom.setId(2L);
        Mockito.when(actorService.getAllActors()).thenReturn(List.of(brad, tom));

        // When
        client.get()
                .uri("/api/v1/actors")
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectBody().json("""
                        [
                          {
                            "id": 1,
                            "name": "Brad Pitt"
                          },
                          {
                            "id": 2,
                            "name": "Tom Cruise"
                          }
                        ]
                        """, JsonCompareMode.STRICT);
    }

}
