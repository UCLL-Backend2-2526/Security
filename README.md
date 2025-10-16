# Spring Security

In deze reeks voorbeeldprojecten vertrekken we vanaf een onbeveiligde Spring Boot applicatie
en voegen we stap voor stap security toe.

- [Stap 0: Basis](./00-Basis): deze versie van het project bevat nog geen Spring Security
- [Stap 1: Enable Spring Security](./01-EnableSpringSecurity): in deze stap schakelen we Spring Security in en kijken we naar wat er allemaal veranderd is.
- [Stap 2: InMemoryUserDetailsManager](./02-InMemoryUserDetailsManager): in deze stap voegen we onze eigen gebruikers toe (in-memory).
- [Stap 3: UserDetails in database opslaan](./03-UserDetailsInDatabase): in deze stap slaan we onze gebruikers op in de database.
- [Stap 4: SecurityFilterChain](./04-SecurityFilterChain): in deze stap zorgen we ervoor dat `POST`, `PUT` en `DELETE` requests terug werken.
- [Stap 5: Registratie](./05-Registratie): in deze stap gaan we het mogelijk maken voor users om zich te registreren.
- [Stap 6: Role-Based Access Control](./06-RBAC): in deze stap gaan we gebruikers rollen toekennen en rechten toekennen op
  basis van rollen.
- [Stap 7: Method Security](./07-EnableMethodSecurity): in deze stap brengen we de autorisatie regels dichter bij de
  endpoints in controllers in de plaats van die te specificeren in een `SecurityFilterChain`.
- [Stap 8: Geavanceerde autorisatie](./08-AdvancedAuthorization): in deze stap zien we hoe we meer complexe autorisatie
  kunnen doen, o.a. op basis van `principal` of `@AuthenticationPrincipal`.
- [Stap 9: HTTP integratietests fixen](./09-FixIntegrationTests): we kunnen onze HTTP integratietests fixen door
  onze gebruikers te mocken.
