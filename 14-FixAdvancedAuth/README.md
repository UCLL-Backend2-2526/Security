# Geavanceerde autorisatie aanpassen aan JWT

## Wat gaan we doen?

Op het einde van de vorige stap zagen we dat alle endpoints waar we meer geavanceerde autorisatieregels toepasten (waarbij het complexer was dan enkel de rol nakijken) faalden met een 500 error. In deze stap gaan we onderzoeken hoe dat komt, en deze endpoints en de bijhorende testen aanpassen.

## Stappen

### 1. Error inspecteren

Laten we eerst een `GET /api/v1/users/1` request uitvoeren met de JWT die we ook in de vorige stap hebben gebruikt.
We zullen zien dat dit een 500 Internal Server Error geeft.

Als we een 500 error krijgen dan is het eerste wat we best doen in de logs van de backend kijken. Daar zien we deze error:

```
2025-11-25T16:51:24.391+01:00 ERROR 37428 --- [Spring-Security] [nio-8080-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.IllegalArgumentException: Failed to evaluate expression '#userDetails.user().id == #id'] with root cause

org.springframework.expression.spel.SpelEvaluationException: EL1011E: Method call: Attempted to call method user() on null context object
	at org.springframework.expression.spel.ast.MethodReference.throwIfNotNullSafe(MethodReference.java:167) ~[spring-expression-6.2.11.jar:6.2.11]
	at org.springframework.expression.spel.ast.MethodReference.getValueInternal(MethodReference.java:118) ~[spring-expression-6.2.11.jar:6.2.11]
	at org.springframework.expression.spel.ast.MethodReference.getValueInternal(MethodReference.java:108) ~[spring-expression-6.2.11.jar:6.2.11]
	at org.springframework.expression.spel.ast.CompoundExpression.getValueRef(CompoundExpression.java:66) ~[spring-expression-6.2.11.jar:6.2.11]
	at org.springframework.expression.spel.ast.CompoundExpression.getValueInternal(CompoundExpression.java:96) ~[spring-expression-6.2.11.jar:6.2.11]
	at org.springframework.expression.spel.ast.OpEQ.getValueInternal(OpEQ.java:42) ~[spring-expression-6.2.11.jar:6.2.11]
	at org.springframework.expression.spel.ast.OpEQ.getValueInternal(OpEQ.java:32) ~[spring-expression-6.2.11.jar:6.2.11]
	at org.springframework.expression.spel.ast.SpelNodeImpl.getValue(SpelNodeImpl.java:116) ~[spring-expression-6.2.11.jar:6.2.11]
    ...
```

We proberen `user()` op te roepen op `null`. De `userDetails` parameter is dus `null`. Er is hier helemaal geen
[`UserDetailsImpl`](./src/main/java/be/ucll/backend2/model/UserDetailsImpl.java) object dat dienst doet als *principal*:
we gebruiken namelijk JWTs en moeten niet meer de gebruikers uit de database halen via de
[`UserDetailsServiceImpl`](./src/main/java/be/ucll/backend2/service/UserDetailsServiceImpl.java).

Wat is dan wél de *principal*? Dat zien we ook in de logs:

```
2025-11-25T16:51:24.341+01:00 DEBUG 37428 --- [Spring-Security] [nio-8080-exec-1] .s.r.w.a.BearerTokenAuthenticationFilter : Set SecurityContextHolder to JwtAuthenticationToken [Principal=org.springframework.security.oauth2.jwt.Jwt@cf4b5f48, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=null], Granted Authorities=[ROLE_READER, ROLE_EDITOR]]
```

De *principal* is nu dus een [`Jwt`](https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/oauth2/jwt/Jwt.html) object. De `id` zit dus niet meer in het bijhorende `User`-object, maar zit nu in de `sub` (subject) claim. We zullen dus het type van de principal moeten aanpassen naar `Jwt` en de expressie in de `@PreAuthorize`.

### 2. `getUser` en `updateUser` aanpassen

Om `getUser` aan te passen moeten we de `AuthenticationPrincipal` veranderen naar `Jwt` en gebruiken we nu de subject:

```java
@GetMapping("/{id}")
@PreAuthorize("#jwt.subject == '' + #id")
public User getUser(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) throws UserNotFoundException {
    return userService.getUser(id);
}
```

Omdat `subject` een string is moeten we `id` ook converteren naar een string. Door de lege string te nemen (`''`) en
de id eraan te hangen kunnen we deze conversie gemakkelijk doen.

Voor `updateUser` moeten we een gelijkaardige aanpassing doen:

```java
@PutMapping("/{id}")
@PreAuthorize("principal.subject == '' + #id")
public User updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) throws UserNotFoundException, EmailAddressNotUniqueException {
    return userService.updateUser(id, userDto);
}
```

De veranderingen vind je in [`UserController.java`](./src/main/java/be/ucll/backend2/controller/UserController.java).

Deze endpoints zouden nu terug correct moeten werken.

### 3. Testen aanpassen

Je zal nu echter merken dat [`UserControllerTest`](./src/test/java/be/ucll/backend2/integration/http/UserControllerTest.java)
niet meer correct werkt. We kunnen dat oplossen door `loginAsUser` aan te passen zodat deze een `Jwt`-object gebruikt in de
plaats van `UserDetailsImpl`:

```java
private static void logInAsUser(long id, String emailAddress, Collection<String> roles) {
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
    final var token = new JwtAuthenticationToken(jwt, authorities);
    SecurityContextHolder.getContext().setAuthentication(token);
}

@Test
public void givenUserWithGivenIdExists_whenUpdateUserIsCalled_thenUserIsUpdated() throws UserNotFoundException, EmailAddressNotUniqueException {
    logInAsUser(1L, "jos@example.com", List.of("ROLE_READER"));
    // ...
}
```

## Wat zien we nu?

Nu zouden alle endpoints terug correct moeten werken.

## Conclusies

- De `principal` is veranderd van `UserDetailsImpl` naar `Jwt` als we JSON Web Tokens gebruiken

## Volgende stappen

We kunnen nu een JWT maken via [jwt.io](https://jwt.io), maar liefst willen we dit aan onze
backend overlaten. In de volgende stap voegen we loginfunctionaliteit toe.
