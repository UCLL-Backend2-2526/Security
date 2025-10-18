package be.ucll.backend2.integration.http;

import be.ucll.backend2.config.SecurityConfig;
import be.ucll.backend2.controller.UserController;
import be.ucll.backend2.controller.dto.UserDto;
import be.ucll.backend2.exception.EmailAddressNotUniqueException;
import be.ucll.backend2.exception.UserNotFoundException;
import be.ucll.backend2.model.User;
import be.ucll.backend2.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private WebTestClient client;

    @MockitoBean
    private UserService userService;

    private static JwtAuthenticationToken createToken(long id, String emailAddress, Collection<String> roles) {
        final var jwt = new Jwt(
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(3600L),
                Map.of("alg", "HS256"),
                Map.of(
                        "sub", String.valueOf(id),
                        "email", emailAddress,
                        "scope", String.join(" ", roles)
                )
        );
        final var authorities = roles.stream().map(role -> new SimpleGrantedAuthority(role)).toList();
        return new JwtAuthenticationToken(jwt, authorities);
    }

    @Test
    public void givenUserWithGivenIdExists_whenUpdateUserIsCalled_thenUserIsUpdated() throws UserNotFoundException, EmailAddressNotUniqueException {
        final var token = createToken(1L, "jef@example.com", List.of("READER"));
        SecurityContextHolder.getContext().setAuthentication(token);

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
