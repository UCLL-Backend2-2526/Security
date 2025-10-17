package be.ucll.backend2.controller;

import be.ucll.backend2.exception.ActorNotFoundException;
import be.ucll.backend2.model.Actor;
import be.ucll.backend2.service.ActorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/actors")
public class ActorController {
    private final ActorService actorService;

    public ActorController(ActorService actorService) {
        this.actorService = actorService;
    }

    @GetMapping
    public List<Actor> getAllActors() {
        return actorService.getAllActors();
    }

    @GetMapping("/{id}")
    public Actor getActorById(@PathVariable long id) throws ActorNotFoundException {
        return actorService.getActorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Actor createActor(@RequestBody Actor actor) {
        return actorService.createActor(actor);
    }

    @PutMapping("/{id}")
    public Actor updateActor(@PathVariable long id, @RequestBody Actor actor) throws ActorNotFoundException {
        return actorService.updateActor(id, actor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActor(@PathVariable long id) throws ActorNotFoundException {
        actorService.deleteActor(id);
    }

    @ExceptionHandler({ActorNotFoundException.class})
    public ResponseEntity<Map<String,String>> handleActorNotFoundException(ActorNotFoundException e) {
        Map<String, String> map = new HashMap<>();
        map.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
    }
}
