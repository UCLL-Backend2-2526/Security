# `EnableMethodSecurity`

## Wat gaan we doen?

In deze stap gaan we de autorisatie checks verplaatsen van onze `SecurityFilterChain` naar
naast de endpoints waar we de checks op toepassen. Dit kan ons helpen met in één oogopslag te
zien welke gebruiker toegang heeft tot een bepaalde endpoint.

## Stappen

### 1. Verwijderen van instellingen in `SecurityFilterChain`

We passen onze [`SecurityFilterChain`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java) aan zodat
deze terug enkel checkt of een gebruiker authenticated is:

```java
.authorizeHttpRequests(
        authorizeRequests ->
                authorizeRequests
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
)
```

### 2. `@EnableMethodSecurity`

De annotaties die we nu gaan gebruiken worden standaard niet ingeschakeld. Door `@EnableMethodSecurity` toe te voegen
aan de `SecurityConfig` klasse kunnen we ervoor zorgen dat Spring Security onze extra voorwaarden gaat nakijken:

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    // ...
}
```

### 3. `@PreAuthorize` annotaties toevoegen

We kunnen nu met behulp van de `@PreAuthorize` annotatie checks toevoegen voordat een methode wordt uitgevoerd.
`@PostAuthorize` kan je gebruiken in de zeldzame gevallen waarbij je achteraf deze check wil uitvoeren.

[Meer informatie over deze annotaties vind je hier.](https://docs.spring.io/spring-security/reference/6.5/servlet/authorization/method-security.html)

De checks die je hierin uitvoert moeten worden geschreven in de
[Spring Expression Language (SpEL)](https://docs.spring.io/spring-framework/reference/6.2/core/expressions.html).

Voorlopig houden we het op de volgende simpele expressies:

- `hasRole('READER')`
- `hasRole('EDITOR')`

`hasRole(...)` kijkt na of een gebruiker een bepaalde rol heeft.

Net zoals bijvoorbeeld JavaScript kan je in SpEL single quotes gebruiken voor strings. Dit is handig omdat je
deze typisch in een Java string gebruikt. Zo moet je geen escapes doen:

- `@PreAuthorize("hasRole('READER')")`
- `@PreAuthorize("hasRole('EDITOR')")`

Deze kunnen we toevoegen op de gepaste plaatsen in
[`ActorController`](./src/main/java/be/ucll/backend2/controller/ActorController.java) en
[`MovieController`](./src/main/java/be/ucll/backend2/controller/MovieController.java).

De checks die we toepassen met `@PreAuthorize` worden uitgevoerd nadat de checks in de `SecurityFilterChain` zijn
uitgevoerd. We kunnen deze annotaties dus gebruiken om de regels te verstrengen, maar niet om ze te verzwakken.

## Wat zien we nu?

We zien nu dat het gedrag niet veranderd is: `@PreAuthorize` gebruiken met `hasRole(...)` in SpEL (Spring Expression Language)
heeft hetzelfde effect als deze instellen in de `SecurityFilterChain`.

## Conclusies

- Met `@EnableMethodSecurity` kunnen we annotaties inschakelen om rechten rechtstreeks bij de methode
  in de stellen.
- Met `@PreAuthorize` kunnen we rechten nakijken voor we de methode uitvoeren.
- De extra annotaties worden nagekeken na de voorwaarden geconfigureerd in de `SecurityFilterChain`: je kan deze dus
  gebruiken om toegangscontrole te verstrengen, maar niet om deze te verzwakken.

## Volgende stappen

We kunnen nu op basis van rollen bepalen wat gebruikers mogen doen, maar sommige rechten kan je niet zo
eenvoudig uitdrukken: hoe kunnen we bijvoorbeeld ervoor zorgen dat een gebruiker enkel hun eigen gegevens
kan aanpassen? In de volgende stap gaan we zien hoe we daarvoor kunnen zorgen.
