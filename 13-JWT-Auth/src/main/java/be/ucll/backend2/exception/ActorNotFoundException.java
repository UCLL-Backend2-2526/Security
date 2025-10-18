package be.ucll.backend2.exception;

public class ActorNotFoundException extends Exception {
    public ActorNotFoundException(long id) {
        super("Actor not found for id: " + id);
    }

    public ActorNotFoundException(String name) {
        super("Actor not found with name: " + name);
    }
}
