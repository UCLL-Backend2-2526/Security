package be.ucll.backend2.unit;

import be.ucll.backend2.exception.ActorNotFoundException;
import be.ucll.backend2.model.Actor;
import be.ucll.backend2.repository.ActorRepository;
import be.ucll.backend2.service.ActorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

// MockitoExtension: enables @Mock en @InjectMocks
@ExtendWith(MockitoExtension.class)
public class ActorServiceTest {
    // @Mock: zet mock op voor elke test
    @Mock
    private ActorRepository actorRepository;

    // @InjectMocks: maak een actorService voor elke test, voorzie een mock alle argumenten
    @InjectMocks
    private ActorService actorService;

    @Test
    public void givenNoActorExistsWithId_whenActorIsDeleted_thenActorNotFoundExceptionIsThrown() {
        // Mock existsById methode
        Mockito.when(actorRepository.existsById(1L)).thenReturn(false);
        // of:
        // Mockito.doReturn(false).when(actorRepository).existsById(1L);

        // Call de methode onder test (moet throwen)
        final var exception = Assertions.assertThrows(ActorNotFoundException.class, () -> actorService.deleteActor(1L));

        // Check of exception klopt
        Assertions.assertEquals("Actor not found for id: 1", exception.getMessage());
    }

    @Test
    public void givenActorExistsWithId_whenActorIsDeleted_thenActorIsDeleted() throws ActorNotFoundException {
        // Mock existsById methode
        Mockito.when(actorRepository.existsById(1L)).thenReturn(true);
        // of:
        // Mockito.doReturn(true).when(actorRepository).existsById(1L);

        // Call de methode onder test
        actorService.deleteActor(1L);

        // Kijk na of de deleteById methode gecalld is
        Mockito.verify(actorRepository).deleteById(1L);
    }

    @Test
    public void givenActorDoesNotExistYet_whenCreateActorIsCalled_thenActorIsSaved() {
        // Mock save methode
        Mockito
                .when(actorRepository.save(Mockito.any(Actor.class)))
                .thenAnswer(invocation -> {
                    var actor = (Actor) invocation.getArguments()[0];
                    actor.setId(1L);
                    return actor;
                });

        var actor = new Actor("Tom Hanks");

        // When
        var returnedActor = actorService.createActor(actor);

        // Kijk na of de acteur die we terug hebben gekregen de juiste data bevat
        Assertions.assertEquals(1L, returnedActor.getId());
        Assertions.assertEquals("Tom Hanks", returnedActor.getName());

        // Kijk na of de save methode gecalld is
        Mockito.verify(actorRepository).save(Mockito.any(Actor.class));
    }
}
