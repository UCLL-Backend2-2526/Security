# Fix HTTP integration tests

## Wat gaan we doen?

We hebben eerder al gemerkt dat twee soorten testen niet meer werken:

- HTTP integratietesten
- Backend component testen

Het fixen van de backend component testen gaan we voor later
houden ([stap 16](../16-FixComponentTests)), maar we zullen alvast
bekijken hoe de HTTP integratietesten terug kunnen werken.

In de HTTP integratietesten mocken we de requests: deze worden niet
écht uitgevoerd. We kunnen hiervoor gebruik maken door ook onze gebruikers
te mocken.

## Stappen

### 1. Logs inspecteren

Als we de HTTP integratietesten runnen zien we dit in de logs staan:

```
2025-10-28T09:13:40.637+01:00  WARN 5604 --- [Spring-Security] [           main] .s.s.UserDetailsServiceAutoConfiguration : 

Using generated security password: e81d5367-f204-4f88-8a70-348a004a7184

This generated password is for development use only. Your security configuration must be updated before running your application in production.

2025-10-28T09:13:40.645+01:00  INFO 5604 --- [Spring-Security] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name inMemoryUserDetailsManager
2025-10-28T09:13:40.726+01:00 DEBUG 5604 --- [Spring-Security] [           main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with filters: DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter, SecurityContextHolderFilter, HeaderWriterFilter, CsrfFilter, LogoutFilter, UsernamePasswordAuthenticationFilter, DefaultResourcesFilter, DefaultLoginPageGeneratingFilter, DefaultLogoutPageGeneratingFilter, BasicAuthenticationFilter, RequestCacheAwareFilter, SecurityContextHolderAwareRequestFilter, AnonymousAuthenticationFilter, ExceptionTranslationFilter, AuthorizationFilter
```

- Het wachtwoord wordt opnieuw automatisch gegenereerd
- De `UserDetailsService` bean is weer de `inMemoryUserDetailsManager`
- De `SecurityFilterChain` is weer dezelfde als voor stap 4

We zijn dus teruggevallen op de default configuratie. Dit gebeurt omdat `@WebMvcTest`
enkel die componenten (of *beans*) inschakelt om de controller die getest wordt
te doen werken.
Onze [`SecurityConfig`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java)
en [`UserDetailsServiceImpl`](./src/main/java/be/ucll/backend2/service/UserDetailsServiceImpl.java)
worden dus niet gebruikt.

### 2. `SecurityConfig` inschakelen

`UserDetailsServiceImpl` gaan we niet nodig hebben: deze gebruikt de database en
die hebben we niet. We gaan gewoon doen alsof een bepaalde gebruiker is ingelogd.

We gaan we de `SecurityConfig` inschakelen. Dit kunnen we doen met de
[`@Import`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/annotation/Import.html) annotatie. We voegen het volgende
toe bovenaan
[`ActorControllerTest`](./src/test/java/be/ucll/backend2/integration/http/ActorControllerTest.java):

```java
@WebMvcTest(ActorController.class)
@Import(SecurityConfig.class)
public class ActorControllerTest {
    // ...
}
```

En [`MovieControllerTest`](./src/test/java/be/ucll/backend2/integration/http/MovieControllerTest.java):

```java
@WebMvcTest(MovieController.class)
@Import(SecurityConfig.class)
public class MovieControllerTest {
    // ...
}
```

Als we de tests nu starten zien we nog altijd dat er een wachtwoord gegenereerd
wordt en een `inMemoryUserDetailsManager` gebruikt. We zien echter ook dat de
`SecurityFilterChain` er nu anders uitziet:

```
2025-10-28T09:28:13.257+01:00 DEBUG 5772 --- [Spring-Security] [           main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with filters: DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter, SecurityContextHolderFilter, HeaderWriterFilter, LogoutFilter, BasicAuthenticationFilter, RequestCacheAwareFilter, SecurityContextHolderAwareRequestFilter, AnonymousAuthenticationFilter, ExceptionTranslationFilter, AuthorizationFilter
```

Dit wil zeggen dat onze eigen `SecurityFilterChain` wordt ingeladen in de plaats van
de default.

### 3. Gebruikers mocken

Nu we onze eigen `SecurityConfig` gebruiken kunnen we "inloggen". We gaan nu
geen HTTP basic authenticatie doen. Het volstaat om te doen alsof een bepaalde
gebruiker ingelogd is. We gaan dus voor we een test runnen de context aanpassen
met een bepaalde ingelogde gebruiker.

Dit kunnen we doen met de
[`@WithMockUser`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/test/context/support/WithMockUser.html) annotatie.
Hiermee kunnen we verschillende eigenschappen instellen van de huidig ingelogde
gebruiker, o.a. `username` en `roles`. Bijvoorbeeld:

```java
@Test
@WithMockUser(username = "jos@example.com", roles = {"READER", "EDITOR"})
public void givenActorWithIdExists_whenDeleteActorIsCalled_thenActorIsDeleted() throws ActorNotFoundException {
    // ...
}
```

Als deze test runt zal deze dus uitgevoerd worden alsof de gebruiker met `username`
"jos@example.com" en rollen "READER" en "EDITOR" authenticated is.

We kunnen nu al de HTTP integratietesten updaten met deze `@WithMockUser` annotatie:
testen die enkel een reader nodig hebben kunnen `roles = {"READER"}` gebruiken.

- [`ActorControllerTest`](./src/test/java/be/ucll/backend2/integration/http/ActorControllerTest.java)
- [`MovieControllerTest`](./src/test/java/be/ucll/backend2/integration/http/MovieControllerTest.java)

## Wat zien we nu?

Nu zal je moeten merken dat de HTTP integratietesten terug werken.

Helaas kunnen we niet op dezelfde manier onze component tests fixen.
In de component tests testen we de backend als een *black box*: er is
dus een scheiding tussen onze testen en de backend die getest wordt.
Met andere woorden: de testen en de backend draaien in een andere context.
Als we een gebruiker mocken in de testcontext heeft dit dus geen effect op
de backend context.

## Conclusies

- Met `@Import(SecurityConfig.class)` kunnen we ervoor zorgen dat onze security
  configuratie wordt ingeladen in de plaats van de default.
- Met `@WithMockUser` kunnen we doen alsof een bepaalde gebruiker is ingelogd.
- De component testen draaien in een andere, gescheiden, context, dus hier werkt
  `@WithMockUser` niet.

## Volgende stappen

Dit lost het probleem op voor de eenvoudige endpoints waar een gebruiker enkel een
gebruikersnaam en rollen heeft, maar nog niet de endpoints waar we andere eigenschappen
van een gebruiker nakijken, zoals de id in
[`UserController`](./src/main/java/be/ucll/backend2/controller/UserController.java).
Dit gaan we aanpakken in de volgende stap.
