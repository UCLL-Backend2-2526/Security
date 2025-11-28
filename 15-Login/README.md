# Login

## Wat gaan we doen?

Het voornaamste wat we nog missen is de loginfunctionaliteit: het is nog
niet mogelijk om in te loggen en een JSON Web Token terug te krijgen.
Daar gaan we nu voor zorgen.

## Stappen

### 1. `JwtEncoder`

We hadden al een `JwtDecoder` toegevoegd aan [`SecurityConfig`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java) om
JWTs te decoden. Nu we die zelf gaan maken hebben we natuurlijk een `JwtEncoder` nodig. Daar voegen we de volgende *bean* voor toe:

```java
@Bean
public JwtEncoder jwtEncoder(SecretKey secretKey) {
    final JWK jwk = new OctetSequenceKey.Builder(secretKey)
            .algorithm(JWSAlgorithm.HS256)
            .build();
    final var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
    return new NimbusJwtEncoder(jwks);
}
```

Dit ziet er iets complexer uit dan de `JwtDecoder` omdat we eerst een `JWK` (JSON Web Keys) object moeten maken,
ingesteld met onze secret key en het algoritme (HS256), dat we dan aan een JWKS (JSON Web Key Set) moeten toevoegen.
Het komt wel op hetzelfde neer: we maken een encoder met onze secret key en het algoritme HS256.

### 2. `JwtService`

Nu we een `JwtEncoder` hebben, kunnen we een service maken die instaat voor het maken van JWTs, een
[`JwtService`](./src/main/java/be/ucll/backend2/service/JwtService.java) dus.

Hier voegen we dan een `generateToken`-methode toe:

```java
public String generateToken(long id, String emailAddress, Collection<String> roles) {
    final var now = Instant.now();
    // TODO: set expiresAt via property
    final var expiresAt = now.plus(30L, ChronoUnit.MINUTES);
    final var header = JwsHeader
        .with(MacAlgorithm.HS256)
        .type("JWT")
        .build();
    final var claims = JwtClaimsSet.builder()
            // TODO: set issuer via property
            .issuer("self")
            .issuedAt(now)
            .expiresAt(expiresAt)
            .subject(String.valueOf(id))
            .claim("email", emailAddress)
            .claim("scope", String.join(" ", roles))
            .build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
}
```

Deze methode neemt een gebruikers-ID, e-mailadres en een collectie (bv. een lijst) van rollen en maakt de bijhorende JWT.

Eerst bepalen we het huidig tijdstip (wanneer de token wordt uitgegeven) en wanneer de token vervalt:

```java
final var now = Instant.now();
final var expiresAt = now.plus(30L, ChronoUnit.MINUTES);
```

Daarna stellen we in wat er in de header moet staan: het algoritme (HS256) en het type (JWT):

```java
final var header = JwsHeader
    .with(MacAlgorithm.HS256)
    .type("JWT")
    .build();
```

Dan voegen we alle claims toe aan een `JwtClaimsSet`:

```java
final var claims = JwtClaimsSet.builder()
        .issuer("self") // iss
        .issuedAt(now) // iat
        .expiresAt(expiresAt) // exp
        .subject(String.valueOf(id)) // sub
        .claim("email", emailAddress)
        .claim("scope", String.join(" ", roles))
        .build();
```

Voor sommige standaard claims is er een methode voorzien, zoals `issuer`, `issuedAt`, `expiresAt` en `subject`.
Andere extra claims kunnen we toevoegen met de `claim` methode.

Tenslotte maken we de JWT met de `jwtEncoder` en halen we met `getTokenValue` de waarde van de token op als een string:

```java
return jwtEncoder.encode(jwtEncoderParameters.from(header, claims)).getTokenValue();
```

Om gemakkelijk een `UserDetailsImpl` om te zetten naar een JWT voegen we ook de volgende methode toe:

```java
public String generateToken(UserDetailsImpl userDetails) {
    return generateToken(
            userDetails.user().getId(),
            userDetails.user().getEmailAddress(),
            userDetails.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.toString()).toList());
}
```

### 3. `SessionController`

Nu dat we JWTs kunnen maken moeten we enkel nog de nodige functionaliteit voorzien om een gebruiker te laten inloggen.

Vaak wordt voor login een endpoint voorzien als `POST /api/v1/login`,
maar als we strikt willen zijn is dat niet zo RESTful, aangezien dat
niet een bewerking op een resource is. We zouden wel kunnen zeggen
dat bij het inloggen een nieuwe sessie wordt gemaakt (ook al zijn
JWTs stateless). We zullen dus een `POST /api/v1/sessions` endpoint
voorzien in [`SessionController`](./src/main/java/be/ucll/backend2/controller/SessionController.java):

```java
@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {
    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        final var token = sessionService.authenticate(
                authenticationRequest.emailAddress(),
                authenticationRequest.password()
        );
        return new AuthenticationResponse(token);
    }
}
```

We hebben hiervoor twee extra DTOs voorzien.
[`AuthenticationRequest`](./src/main/java/be/ucll/backend2/controller/dto/AuthenticationRequest.java):

```java
public record AuthenticationRequest(
        @Email @NotBlank String emailAddress,
        @NotBlank String password
) {
}
```

En [`AuthenticationResponse`](./src/main/java/be/ucll/backend2/controller/dto/AuthenticationResponse.java):

```java
public record AuthenticationResponse(
        String token
) {
}
```

Veel doet deze methode niet: gewoon het e-mailadres en wachtwoord van de gebruiker
doorgeven aan de `SessionService` en dan de token terug geven aan de gebruiker.

### 4. `SessionService`

De eigenlijke authenticatie gebeurt in de
[`SessionService`](./src/main/java/be/ucll/backend2/service/SessionService.java):

```java
@Service
public class SessionService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public SessionService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public String authenticate(String emailAddress, String password) {
        final var usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(emailAddress, password);
        final var authentication = authenticationManager.authenticate(usernamePasswordAuthentication);
        final var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }
}
```

Uiteindelijk zijn dit slechts 4 lijnen code, aangezien alle onderdelen eigenlijk
al aanwezig zijn. Wat we nu slechts moeten doen is een nieuwe `UsernamePasswordAuthenticationToken` aanmaken:

```java
final var usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(emailAddress, password);
```

Vervolgens gebruiken we gewoon de
[`AuthenticationManager`](https://docs.spring.io/spring-security/reference/6.5/api/java/org/springframework/security/authentication/AuthenticationManager.html)
die door Spring Security al standaard voorzien wordt om de authenticatie uit te voeren:

```java
final var authentication = authenticationManager.authenticate(usernamePasswordAuthentication);
```

De `authenticationManager` zal vanzelf een exception gooien als de gebruikersgegevens niet kloppen. Deze exception geeft al automatisch
de juiste error code (401 Unauthorized) dus we hoeven hier verder niets speciaals mee te doen.

Aangezien we weten dat we een
[`UserDetailsImpl`](./src/main/java/be/ucll/backend2/model/UserDetailsImpl.java) teruggeven vanuit [`UserDetailsServiceImpl`](./src/main/java/be/ucll/backend2/service/UserDetailsServiceImpl.java)
kunnen we de `principal` van het object dat we hebben teruggekregen casten naar `UserDetailsImpl`:

```java
final var userDetails = (UserDetailsImpl) authentication.getPrincipal();
```

Nu we de `UserDetailsImpl` hebben moeten we enkel nog de token aanmaken:

```java
return jwtService.generateToken(userDetails);
```

#### Waar komt de `AuthenticationManager` vandaan?

De `AuthenticationManager` wordt in Spring Security pas aangemaakt wanneer deze voor het eerst gebruikt wordt en kan ook een `Exception`
gooien. Om het wat handiger te maken voegen we een extra *bean* toe
aan [`SecurityConfig`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java):

```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
}
```

Dit zorgt ervoor dat de `AuthenticationManager` al vroeger wordt aangemaakt en moeten we in `SessionService` er geen rekening mee
houden dat dit mogelijk een `Exception` gooit.

### 4. Inloggen toestaan

We zullen ook `POST /api/v1/sessions` moeten toestaan voor iedereen, dus we passen dit aan in de `SecurityFilterChain`:

```java
@Bean
@Order(1)
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                    sessionManagement ->
                            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .authorizeHttpRequests(
                    authorizeRequests ->
                            authorizeRequests
                                    .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/sessions").permitAll()
                                    .requestMatchers("/error").permitAll()
                                    .anyRequest().authenticated()
            )
            .build();
}
```

## Wat zien we nu?

Nu kunnen we eens proberen of de loginfunctionaliteit correct werkt. Laten we een `POST`-request sturen naar `/api/v1/sessions` met de
volgende body:

```json
{
  "emailAddress": "editor@example.com",
  "password": "editor"
}
```

We zullen dan een response zoals de volgende terug krijgen:

```json
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzZWxmIiwic3ViIjoiMSIsImV4cCI6MTc2NDMyODEwNiwiaWF0IjoxNzY0MzI2MzA2LCJlbWFpbCI6ImVkaXRvckBleGFtcGxlLmNvbSIsInNjb3BlIjoiUk9MRV9SRUFERVIgUk9MRV9FRElUT1IifQ.T59whEfCXCUysXL37Q9EfeM_s53nVqmHDzlsok7H4mA"
}
```

Als we deze token decoden op [jwt.io](https://jwt.io) zien we:

![](./doc/images/jwt_decoder.png)

Als je in de secret de secret key invult die in
[`SecurityConfig`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java)
staat zal je ook zien "Valid secret".

Deze key kunnen we nu gebruiken als `Bearer` token als we requests uitvoeren.

## Conclusies

- Om JWTs te maken moeten we een `JwtEncoder` aanmaken
- De `AuthenticationManager` laat ons toe om in code authenticatie te doen

## Volgende stappen

- De component testen werken nog niet, maar nu hebben we alles wat we nodig hebben om deze terug te doen werken.
- Bepaalde instellingen, zoals de geldigheidsduur van een JWT en wie de issuer is zouden we liever instellen via een property
zodat dit kan geconfigureerd worden als we deployen.
