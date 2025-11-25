# InMemoryUserDetailsManager

## Wat gaan we doen?

In de vorige stap zagen we het volgende logbericht:

```
2025-10-12T18:11:12.933+02:00  INFO 1524 --- [Spring-Security] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name inMemoryUserDetailsManager
```

Laten we dit bericht ontleden:

- [AuthenticationManager](https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/authentication/AuthenticationManager.html): dit is een interface met één methode: `authenticate`. De `AuthenticationManager` zorgt dus voor de authenticatie.
- [UserDetailsService](https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/core/userdetails/UserDetailsService.html): dit is een interface met één methode:
  `loadUserByUsername`. De `UserDetailsService` zorgt dus voor het ophalen van
  gebruikersinformatie.
- [inMemoryUserDetailsManager](https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/provisioning/InMemoryUserDetailsManager.html): dit is een concrete implementatie van de
  `UserDetailsService` interface. Gebruikersgegevens worden "in memory" bewaard
  (en dus niet bijvoorbeeld uit de database gehaald).

In deze stap zouden we graag zélf willen bepalen welke `UserDetailsService`
gebruikt wordt, zodat we zelf gebruikers kunnen toevoegen. We zullen beginnen
met deze gebruikers simpelweg "in memory" te zetten. We gaan dus een eigen
`InMemoryUserDetailsManager` aanmaken.

Om een eigen `UserDetailsService` bean te maken zullen we eerst een configuratie
klasse moeten voorzien. Dit is een klasse met een `@Configuration` annotatie.

In de configuratie klasse kunnen we dan beans aanmaken door methodes te annoteren
met `@Bean`.

We kunnen gemakkelijk een lijst van alle `UserDetails` meegeven aan de constructor
van `InMemoryUserDetailsManager`:

```java
@Configuration
public class SecurityConfig {
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
                // User biedt een "builder" aan om UserDetails te maken
                User.withUsername("josb").password("{noop}josb123").build()
        );
    }
}
```

Voorlopig gebruiken we in het wachtwoord `{noop}` om aan te geven dat het wachtwoord niet
gehasht is. We gaan deze hashing in een latere stap toevoegen.

## Wat zien we nu?

We zien dat het eerdere bericht over het automatisch gegenereerd wachtwoord uit de
logs is verdwenen. In de plaats zien we nu:

```
2025-10-13T15:11:51.641+02:00  INFO 19136 --- [Spring-Security] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name userDetailsService
```

We hebben nu dus een bean van het type `UserDetailsService` genaamd `userDetailsService` (afgeleid van de naam van de methode).

Als we nu inloggen, gaat dat met onze eigen gedefinieerde gebruiker.

## Conclusies

- De configuratie van `spring-boot-starter-security` gebeurt aan de hand van beans
- Er zijn default beans voorzien
- Door eigen beans te maken kunnen we de default configuratie aanpassen

## Volgende stappen

Nu hebben we een eigen `UserDetailsService`, maar zijn de gebruikers nog
hardcoded. In de volgende stap gaan we een `UserDetailsService` maken die
gebruikers uit de database haalt.
