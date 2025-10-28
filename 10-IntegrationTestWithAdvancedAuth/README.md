# HTTP integratietest met geavanceerde autorisatie

## Wat gaan we doen?

`@WithMockUser` volstaat niet voor alle soorten testen. In
[`UserController`](./src/main/java/be/ucll/backend2/controller/UserController.java)
testen we bijvoorbeeld in de `@PreAuthorize` checks of een
gebruiker een bepaalde id heeft: iets wat we niet kunnen instellen
met `@WithMockUser`.

We gaan nu een test schrijven voor `UserController` die op een andere
manier zal moeten instellen wat nu precies de huidige gebruiker gaat zijn:
een manier waarbij we wel een `UserDetailsImpl` gaan kunnen instellen als
de *principal*.

## Stappen

### 1. Test implementeren

We kunnen beginnen met een test die het updaten van een gebruiker test:

```java
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private WebTestClient client;

    @MockitoBean
    private UserService userService;

    @Test
    public void givenUserWithGivenIdExists_whenUpdateUserIsCalled_thenUserIsUpdated() throws UserNotFoundException, EmailAddressNotUniqueException {
        final var userDto = new UserDto(
                "jos@example.com",
                "password"
        );
        final var updatedUser = new User(
                "jos@example.com",
                "{noop}password"
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
```

In deze test willen we testen dat als `PUT /api/v1/users/1` gebruikt wordt, dat dit dan slaagt en
de geüpdatete gebruiker in de response staat.

`userService.updateUser(...)` wordt gebruikt in de `updateUser`-methode van `UserController` dus
deze moeten we mocken: als deze wordt opgeroepen met id 1 en de user om te updaten (de DTO die
we gaan gebruiken in de request body) dan returnt deze de aangepaste gebruiker.

We kunnen dan de PUT-request uitvoeren en het resultaat nakijken.

Nu zien we dat als we deze test runnen dat we 401 Unauthorized krijgen. Als we het zouden proberen
met `@WithMockUser` krijgen we de volgende error te zien:

```
org.springframework.web.reactive.function.client.WebClientRequestException: Request processing failed: java.lang.IllegalArgumentException: Failed to evaluate expression 'principal.user().id == #id'
```

Deze is uiteindelijk veroorzaakt door:

```
Caused by: org.springframework.expression.spel.SpelEvaluationException: EL1004E: Method call: Method user() cannot be found on type org.springframework.security.core.userdetails.User
```

Onze *principal* is een `org.springframework.security.userdetails.User`, wat geen `user()`-methode heeft. We moeten onze *principal* instellen op
een instantie van `be.ucll.backend2.model.UserDetailsImpl`.

### 2. Principal instellen als een `UserDetailsImpl`

We kunnen een eigen annotatie maken met behulp van
[@WithSecurityContext](https://docs.spring.io/spring-security/reference/servlet/test/method.html#test-method-withsecuritycontext).
We gaan het nu echter nog relatief eenvoudig houden door gewoon een hulpmethode te schrijven die een `UserDetailsImpl` aanmaakt
en deze instelt als de ingelogde gebruiker:

```java
private static void logInAsUser(long id, String emailAddress, Role role) {
    final var user = new User(emailAddress, "{noop}password");
    user.setId(id);
    user.setRole(role);
    final var userDetails = new UserDetailsImpl(user);
    final var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
```

We maken hier een gebruiker (met gegeven `id`, `emailAddress` en `role`) en maken daar
een `UserDetailsImpl` mee aan:

```java
final var user = new User(emailAddress, "{noop}password");
user.setId(id);
user.setRole(role);
final var userDetails = new UserDetailsImpl(user);
```

Het wachtwoord is hier niet echt relevant, vandaar dat we het instellen op `"{noop}password"`.

Daarna maken we een `UsernamePasswordAuthenticationToken`:

```java
final var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
```

Dit is een specialisatie van `Authentication` waarmee we de *principal* (onze `userDetails`),
*credentials* (bijvoorbeeld het wachtwoord, maar dat maakt hier niet uit dus we kunnen dit op
`null` instellen) en de *authorities* kunnen instellen.

Tenslotte stellen we onze `UsernamePasswordAuthenticationToken` in als de token waarmee momenteel
is ingelogd op de huidige `SecurityContext`. We doen dit door de huidige context op te vragen
en dan de `setAuthentication`-methode te gebruiken:

```java
SecurityContextHolder.getContext().setAuthentication(authentication);
```

Deze kunnen we dan gebruiken in onze testmethode:

```java
@Test
public void givenUserWithGivenIdExists_whenUpdateUserIsCalled_thenUserIsUpdated() throws UserNotFoundException, EmailAddressNotUniqueException {
    logInAsUser(1L, "jos@example.com", Role.READER);

    // ...
}
```

## Wat zien we nu?

Nu zal je zien dat de test terug slaagt: we hebben immers nu een `UserDetailsImpl` als
*principal*, net zoals onze `@PreAuthorize` check controleert.

## Conclusies

- `@WithMockUser` is niet voldoende als we extra eigenschappen van de huidige gebruiker willen nakijken.
- Door een eigen `Authentication`-object te maken en dit in te stellen in de huidige `SecurityContext` kunnen
  we ervoor zorgen dat de gewenste gebruiker is ingelogd.

## Volgende stappen

Voorlopig werken de HTTP integratietesten weer correct. In een latere stap gaan we echter
overstappen op JWT (JSON Web Tokens) voor authenticatie en zullen we de HTTP integratietesten
nog moeten updaten.
