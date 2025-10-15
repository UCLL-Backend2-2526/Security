# CSRF tokens uitschakelen

## Wat gaan we doen?

We hebben gemerkt dat `POST`, `PUT` en `DELETE` requests nog steeds niet werken in Postman. In deze stap gaan we
uitzoeken hoe dit komt en dit probleem oplossen.

## Stappen

### 1. Meer logging

Je kan in de configuratie aanpassen hoeveel berichten er gelogd worden. Als je kijkt naar de logs zie je dat er bij
de verschillende lijnen steeds `INFO`, `WARN` of soms `ERROR` staat: dit geeft de log level aan waarmee dit bericht
gelogd is. Standaard loggen we alles van niveau `INFO` of hoger.

We zouden graag debug-berichten willen zien van Spring Security om precies te zien wat er gebeurt en waarom onze
`POST`, `PUT` en `DELETE` requests niet doorkomen. Hiervoor kunnen we de log level instellen voor de package
`org.springframework.security`:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

We zien nu alvast één debug bericht verschijnen waar we later nog op terug zullen komen:

```
2025-10-15T20:30:25.406+02:00 DEBUG 22872 --- [Spring-Security] [           main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with filters: DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter, SecurityContextHolderFilter, HeaderWriterFilter, CsrfFilter, LogoutFilter, UsernamePasswordAuthenticationFilter, DefaultResourcesFilter, DefaultLoginPageGeneratingFilter, DefaultLogoutPageGeneratingFilter, BasicAuthenticationFilter, RequestCacheAwareFilter, SecurityContextHolderAwareRequestFilter, AnonymousAuthenticationFilter, ExceptionTranslationFilter, AuthorizationFilter
```

### 2. Een `POST` request proberen

Als we nu in Postman een `POST /api/v1/actors` request proberen uit te voeren zien we het volgende in de logs:

```
2025-10-15T20:32:00.756+02:00 DEBUG 22872 --- [Spring-Security] [nio-8080-exec-2] o.s.security.web.FilterChainProxy        : Securing POST /api/v1/actors
2025-10-15T20:32:00.835+02:00 DEBUG 22872 --- [Spring-Security] [nio-8080-exec-2] o.s.security.web.csrf.CsrfFilter         : Invalid CSRF token found for http://localhost:8080/api/v1/actors
```

De klasse `CsrfFilter` vertelt dus "Invalid CSRF token found". Dit is een speciale soort beveiliging tegen
[CSRF (Cross-Site Request Forgery)](https://owasp.org/www-community/attacks/csrf) aanvallen. We gaan deze
filter niet nodig hebben omdat we uiteindelijk met JWT (JSON Web Tokens) gaan werken. We zullen deze filter dus
uitschakelen.

### 3. De `securityFilterChain` aanpassen

Laten we even terug kijken naar dit logbericht:

```
2025-10-15T20:30:25.406+02:00 DEBUG 22872 --- [Spring-Security] [           main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with filters: DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter, SecurityContextHolderFilter, HeaderWriterFilter, CsrfFilter, LogoutFilter, UsernamePasswordAuthenticationFilter, DefaultResourcesFilter, DefaultLoginPageGeneratingFilter, DefaultLogoutPageGeneratingFilter, BasicAuthenticationFilter, RequestCacheAwareFilter, SecurityContextHolderAwareRequestFilter, AnonymousAuthenticationFilter, ExceptionTranslationFilter, AuthorizationFilter
```

We zien daar een hele lijst van verschillende filters. Zo werkt Spring Security: elke request doorloopt
een hele resem van filters. Dat is standaard bovenstaande lijst. Wat we willen doen is deze lijst van filters
aanpassen om er o.a. de `CsrfFilter` tussenuit te halen.

Net zoals we eerder hebben gedaan voor `UserDetailsService` kunnen met een eigen bean een eigen
`SecurityFilterChain` instellen. We voegen hiervoor een methode toe aan
[`SecurityConfig`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java):

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(csrf -> csrf.disable())
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(
                    authorizeRequests ->
                            authorizeRequests
                                    .anyRequest().authenticated()
            )
            .build();
}
```

Hier kunnen we een eigen ketting van filters maken via een fluent interface. We doen de volgende dingen:

- We schakelen de `CsrfFilter` uit (`.csrf(csrf -> csrf.disable())`). Dit is nodig omdat deze
  standaard altijd in de `SecurityFilterChain` zit.
- We schakelen HTTP Basic authentication in (`.httpBasic(Customizer.withDefaults())`). Dit moeten we nu expliciet doen.
- We stellen de `AuthorizationFilter` in. In dit geval zorgen we ervoor dat alle requests (`anyRequest()`) door
  een authenticated (`authenticated()`) user moeten gebeuren.

## Wat zien we nu?

Het resultaat is dat we nu het volgende zien als we de applicatie starten:

```
2025-10-15T21:03:10.509+02:00 DEBUG 4664 --- [Spring-Security] [           main] o.s.s.web.DefaultSecurityFilterChain     : Will secure any request with filters: DisableEncodeUrlFilter, WebAsyncManagerIntegrationFilter, SecurityContextHolderFilter, HeaderWriterFilter, LogoutFilter, BasicAuthenticationFilter, RequestCacheAwareFilter, SecurityContextHolderAwareRequestFilter, AnonymousAuthenticationFilter, ExceptionTranslationFilter, AuthorizationFilter
```

We zien dat deze ketting van filters nu korter is omdat we onze eigen ketting hebben ingesteld. We zien hier de volgende
filters:

- `BasicAuthenticationFilter`: deze hebben we expliciet ingeschakeld
- `AuthorizationFilter`: deze hebben we geconfigureerd met `authorizeHttpRequests`

We zien nu ook dat `CsrfFilter` ontbreekt, aangezien we deze hebben uitgeschakeld.

Alle andere filters die we nu zien zijn standaard ingesteld.

Probeer zelf ook eens om `authorizeHttpRequests(...)` weg te laten. Wat merk je nu?

## Conclusies

...

## Volgende stappen

- We kunnen vooraf gebruikers toevoegen aan de database, maar nog niet registeren. Hier gaan we in een volgende stap
  naartoe werken.