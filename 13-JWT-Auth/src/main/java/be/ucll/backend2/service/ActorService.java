package be.ucll.backend2.service;

import be.ucll.backend2.exception.ActorNotFoundException;
import be.ucll.backend2.model.Actor;
import be.ucll.backend2.repository.ActorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActorService {
    private final ActorRepository actorRepository;

    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public List<Actor> getAllActors() {
        return actorRepository.findAll();
    }

    public Actor getActorById(long id) throws ActorNotFoundException {
        return actorRepository.findById(id).orElseThrow(() -> new ActorNotFoundException(id));
    }

    public Actor createActor(Actor actor) {
        return actorRepository.save(actor);
    }

    public Actor updateActor(long id, Actor actor) throws ActorNotFoundException {
        final var oldActor = actorRepository.findById(id).orElseThrow(() -> new ActorNotFoundException(id));
        oldActor.update(actor);
        return actorRepository.save(oldActor);
    }

    public void deleteActor(long id) throws ActorNotFoundException {
        if (!actorRepository.existsById(id)) {
            throw new ActorNotFoundException(id);
        }
        actorRepository.deleteById(id);
    }
}
