# Spring Security

In deze reeks voorbeeldprojecten vertrekken we vanaf een onbeveiligde Spring Boot applicatie
en voegen we stap voor stap security toe.

- [Stap 0: Basis](./00-Basis): deze versie van het project bevat nog geen Spring Security.
- [Stap 1: Enable Spring Security](./01-EnableSpringSecurity): in deze stap voegen we de Spring Security dependency toe en zien we hoe alle endpoints automatisch beveiligd worden.
- [Stap 2: InMemoryUserDetailsManager](./02-InMemoryUserDetailsManager): in deze stap vervangen we de gegenereerde gebruiker door onze eigen hardcoded gebruikers via een custom UserDetailsService bean.
- [Stap 3: UserDetails in database opslaan](./03-UserDetailsInDatabase): in deze stap implementeren we een `UserDetailsService` die gebruikers uit de database haalt in plaats van hardcoded waarden.
- [Stap 4: SecurityFilterChain](./04-SecurityFilterChain): in deze stap configureren we een custom `SecurityFilterChain` om CSRF protection uit te schakelen.
- [Stap 5: Registratie](./05-Registratie): in deze stap laten we toe om gebruikers te registreren.
- [Stap 6: Role-Based Access Control](./06-RBAC): in deze stap introduceren we rollen (`READER` en `EDITOR`) en implementeren we autorisatie op basis van deze rollen in de `SecurityFilterChain`.
- [Stap 7: Method Security](./07-EnableMethodSecurity): in deze stap verplaatsen we autorisatie regels van `SecurityFilterChain` naar `@PreAuthorize` annotaties op controller methodes.
- [Stap 8: Geavanceerde autorisatie](./08-AdvancedAuthorization): in deze stap implementeren we complexe autorisatie regels die toegang controleren op basis van de ingelogde gebruiker.
- [Stap 9: HTTP integratietests fixen](./09-FixIntegrationTests): in deze stap passen we HTTP integratietests aan door gebruikers te mocken met `@WithMockUser` annotaties.
- [Stap 10: HTTP integratietest met geavanceerde autorisatie](./10-IntegrationTestWithAdvancedAuth): in deze stap demonstreren we hoe complexe autorisatie scenarios getest kunnen worden wanneer `@WithMockUser` niet voldoende is.
- [Stap 11: Fix H2 Console](./11-FixH2Console): in deze stap configureren we een aparte `SecurityFilterChain` om toegang tot de H2 console mogelijk te maken.
- [Stap 12: Automatische wachtwoord updates](./12-PasswordUpdates): in deze stap implementeren we automatische wachtwoord updates door de `UserDetailsPasswordService` interface te implementeren.
- [Stap 13: JWT authenticatie](./13-JWT-Auth): in deze stap vervangen we HTTP Basic Authentication door JWT (JSON Web Token) authenticatie.
- [Stap 14: Geavanceerde autorisatie fixen](./14-FixAdvancedAuth): in deze stap passen we de geavanceerde autorisatie aan om te werken met JWT in plaats van HTTP Basic Authentication.
- [Stap 15: Login](./15-Login): in deze stap voegen we een login endpoint toe die JWT tokens genereert na succesvolle authenticatie.
- [Stap 16: Component tests fixen](./16-FixComponentTests): in deze stap passen we de component tests aan om te werken met JWT authenticatie.
- [Stap 17: Eigen properties toevoegen](./17-CustomProperties): in deze stap voegen we custom properties toe voor JWT configuratie en maken we de secret key configureerbaar.
- [Stap 18: Tests één laatste keer fixen](./18-FixTestsAgain): in deze stap zorgen we ervoor dat de tests blijven werken, ook al is er vooraf geen secret key geconfigureerd.
- [Stap 19: OpenAPI en Swagger UI toevoegen](./19-Swagger): in deze stap integreren we OpenAPI en Swagger UI met JWT security voor API documentatie.
