package be.ucll.backend2.integration.http;

import be.ucll.backend2.config.SecurityConfig;
import be.ucll.backend2.controller.UserController;
import be.ucll.backend2.controller.dto.UserDto;
import be.ucll.backend2.exception.EmailAddressNotUniqueException;
import be.ucll.backend2.exception.UserNotFoundException;
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
import org.springframework.security.core.userdetails.UserDetails;
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

    private static UserDetails createUserDetails(long id, String emailAddress, String hashedPassword) {
        final var user = new User(emailAddress, hashedPassword);
        user.setId(id);
        return new UserDetailsImpl(user);
    }

    @Test
    public void givenUserWithGivenIdExists_whenUpdateUserIsCalled_thenUserIsUpdated() throws UserNotFoundException, EmailAddressNotUniqueException {
        final var userDetails = createUserDetails(1L, "jef@example.com", "{noop}pass");
        final var auth = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

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
