package be.ucll.backend2.integration.http;

import be.ucll.backend2.config.SecurityConfig;
import be.ucll.backend2.controller.UserController;
import be.ucll.backend2.controller.dto.UserDto;
import be.ucll.backend2.exception.EmailAddressNotUniqueException;
import be.ucll.backend2.exception.UserNotFoundException;
import be.ucll.backend2.model.Role;
import be.ucll.backend2.model.User;
import be.ucll.backend2.model.UserDetailsImpl;
import be.ucll.backend2.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private WebTestClient client;

    @MockitoBean
    private UserService userService;

    private static void logInAsUser(long id, String emailAddress, Role role) {
        final var user = new User(emailAddress, "{noop}password");
        user.setId(id);
        user.setRole(role);
        final var userDetails = new UserDetailsImpl(user);
        final var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void givenUserWithGivenIdExists_whenUpdateUserIsCalled_thenUserIsUpdated() throws UserNotFoundException, EmailAddressNotUniqueException {
        logInAsUser(1L, "jos@example.com", Role.READER);

        final var userDto = new UserDto(
                "jos@example.com",
                "password"
        );
        final var updatedUser = new User(
                "jos@example.com",
                "password"
        );
        updatedUser.setId(1L);

        Mockito.when(userService.updateUser(1L, userDto)).thenReturn(updatedUser);

        client
                .put()
                .uri("/api/v1/users/{id}", 1L)
                .bodyValue(userDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("""
                    {
                        "id": 1,
                        "emailAddress": "jos@example.com",
                        "role": "READER"
                    }
                    """, JsonCompareMode.STRICT);
    }
}
