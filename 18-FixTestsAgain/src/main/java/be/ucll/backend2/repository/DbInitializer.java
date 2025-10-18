package be.ucll.backend2.repository;

import be.ucll.backend2.model.Actor;
import be.ucll.backend2.model.Movie;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
public class DbInitializer {
    private final ActorRepository actorRepository;
    private final MovieRepository movieRepository;

    public DbInitializer(ActorRepository actorRepository, MovieRepository movieRepository) {
        this.actorRepository = actorRepository;
        this.movieRepository = movieRepository;
    }

    @PostConstruct
    @Profile("!test") // Doe enkel de initiële initialize als we niet test profile gebruiken
    public void initialize() {
        var frances = new Actor("Frances McDormand");
        var steve = new Actor("Steve Buscemi");
        var woody = new Actor("Woody Harrelson");

        frances = actorRepository.save(frances);
        steve = actorRepository.save(steve);
        woody = actorRepository.save(woody);

        var fargo = new Movie("Fargo", "Joel & Ethan Coen", 1996);
        var three_billboards = new Movie("Three Billboards Outside Ebbing, Missouri", "Martin McDonagh", 2017);

        fargo.addActor(frances);
        fargo.addActor(steve);

        three_billboards.addActor(frances);
        three_billboards.addActor(woody);

        movieRepository.save(fargo);
        movieRepository.save(three_billboards);
    }
}
