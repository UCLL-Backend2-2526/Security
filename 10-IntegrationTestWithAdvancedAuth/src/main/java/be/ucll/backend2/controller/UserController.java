package be.ucll.backend2.controller;

import be.ucll.backend2.controller.dto.UserDto;
import be.ucll.backend2.exception.EmailAddressNotUniqueException;
import be.ucll.backend2.exception.UserNotFoundException;
import be.ucll.backend2.model.User;
import be.ucll.backend2.model.UserDetailsImpl;
import be.ucll.backend2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("#userDetails.user().id == #id")
    public User getUser(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable long id) throws UserNotFoundException {
        return userService.getUser(id);
    }

    @PostMapping
    public User registerUser(@Valid @RequestBody UserDto userDto) throws EmailAddressNotUniqueException {
        return userService.registerUser(userDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("principal.user().id == #id")
    public User updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) throws UserNotFoundException, EmailAddressNotUniqueException {
        return userService.updateUser(id, userDto);
    }

    @ExceptionHandler(EmailAddressNotUniqueException.class)
    public ResponseEntity<Map<String,String>> handleEmailAddressNotUniqueException(EmailAddressNotUniqueException e) {
        Map<String, String> map = new HashMap<>();
        map.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(map);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String,String>> handleUserNotFoundException(UserNotFoundException e) {
        Map<String, String> map = new HashMap<>();
        map.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
    }
}
