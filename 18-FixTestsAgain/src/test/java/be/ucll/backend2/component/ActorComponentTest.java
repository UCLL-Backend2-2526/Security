package be.ucll.backend2.component;

import be.ucll.backend2.model.Actor;
import be.ucll.backend2.repository.DbInitializer;
import be.ucll.backend2.service.JwtService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("classpath:schema.sql") // schema.sql reset database bij elke test
@ActiveProfiles("test") // gebruik test profile
public class ActorComponentTest {

    @Autowired
    private WebTestClient client; // Client om requests uit te voeren

    @Autowired
    private EntityManager em; // Gebruik een échte EntityManager

    @Autowired
    private DbInitializer dbInitializer;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    public void addTestData() {
        // Zet voor elke test testdata in de DB
        dbInitializer.initialize();
    }

    @Test
    public void givenActorWithIdExists_whenDeleteActorIsCalled_thenActorIsDeleted() {
        // Kijk na dat we vooraf wél een acteur in de DB hebben
        final var actorBefore = em.find(Actor.class, 1L);
        Assertions.assertNotNull(actorBefore);

        // Genereer token
        final var token = jwtService.generateToken(1L, "jos@example.com", List.of("ROLE_EDITOR", "ROLE_READER"));

        client.delete()
                .uri("/api/v1/actors/{id}", 1L)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNoContent();

        // Kijk na dat we nu geen acteur met ID 1 meer kunnen vinden
        final var actorAfter = em.find(Actor.class, 1L);
        Assertions.assertNull(actorAfter);
    }

    @Test
    public void givenActorWithIdDoesNotExist_whenDeleteActorIsCalled_then404IsReturned() {
        // Genereer token
        final var token = jwtService.generateToken(1L, "jos@example.com", List.of("ROLE_EDITOR", "ROLE_READER"));

        client.delete()
                .uri("/api/v1/actors/{id}", 100L)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().json("""
                                   {
                                     "message": "Actor not found for id: 100"
                                   }
                                   """,
                        JsonCompareMode.STRICT);
    }

}
