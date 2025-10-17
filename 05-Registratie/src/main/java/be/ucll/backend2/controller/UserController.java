package be.ucll.backend2.controller;

import be.ucll.backend2.controller.dto.CreateUserDto;
import be.ucll.backend2.exception.EmailAddressNotUniqueException;
import be.ucll.backend2.model.User;
import be.ucll.backend2.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public User registerUser(@Valid @RequestBody CreateUserDto createUserDto) throws EmailAddressNotUniqueException {
        return userService.registerUser(createUserDto);
    }

    @ExceptionHandler(EmailAddressNotUniqueException.class)
    public ResponseEntity<Map<String,String>> handleEmailAddressNotUniqueException(EmailAddressNotUniqueException e) {
        Map<String, String> map = new HashMap<>();
        map.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
    }
}
