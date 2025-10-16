# Spring Security

In deze reeks voorbeeldprojecten vertrekken we vanaf een onbeveiligde Spring Boot applicatie
en voegen we stap voor stap security toe.

- [Stap 0: Basis](./00-Basis): deze versie van het project bevat nog geen Spring Security
- [Stap 1: Enable Spring Security](./01-EnableSpringSecurity): in deze stap schakelen we Spring Security in en kijken we naar wat er allemaal veranderd is.
- [Stap 2: InMemoryUserDetailsManager](./02-InMemoryUserDetailsManager): in deze stap voegen we onze eigen gebruikers toe (in-memory).
- [Stap 3: UserDetails in database opslaan](./03-UserDetailsInDatabase): in deze stap slaan we onze gebruikers op in de database.
- [Stap 4: UserDetailsPasswordService](./04-UserDetailsPasswordService): in deze stap gaan we ervoor zorgen dat password hashes automatisch geüpdatet worden.
- [Stap 5: SecurityFilterChain](./05-SecurityFilterChain): in deze stap zorgen we ervoor dat `POST`, `PUT` en `DELETE` requests terug werken.
- Registratie (met heel simpele request matching voor authz)
- RBAC
- Method security (`@PreAuthorize`)
- Meer complexe authz (eigen wachtwoord aanpassen)