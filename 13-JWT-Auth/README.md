# Authenticatie met JWT (JSON Web Tokens)

## Wat gaan we doen?

Tot nu toe hebben we altijd onze gebruikersnaam en wachtwoord gestuurd naar de server bij elke request, en wordt
via de `UserDetailsManager` de gebruiker uit de database gehaald en het wachtwoord nagekeken. Dit is geen efficiënte
manier van werken. Met JSON Web Tokens (JWTs) wordt het mogelijk om bij elke request te bewijzen dat de gebruiker toegang
heeft zonder dat daar de database voor nodig is.

Een JWT bestaat steeds uit drie delen:

1. Header
2. Payload
3. Signature

Deze drie delen zijn telkens base64url-encoded: dit wil zeggen dat ze omgezet worden in een voorstelling die gebruik maakt
van slechts 64 tekens, die veilig zijn voor gebruik in URLs. De drie delen worden achter elkaar geplakt, met telkens een
punt ertussen. Het resultaat ziet eruit als volgt:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30
```

Dit ziet er niet leesbaar uit, maar als je naar [jwt.io](https://jwt.io) gaat zal je zien dat je heel gemakkelijk de informatie
die in deze token zit er terug uit kan halen. Een JWT is dus standaard **niet** geëncrypteerd: sla hier dus geen gevoelige informatie die
de gebruiker niet mag weten in op! Met JWE (JSON Web Encryption) kan je wel encryptie toevoegen, maar dat gaan wij niet doen.

### 1.Header

De header bevat informatie die de JWT beschrijft. Deze informatie wordt voorgesteld als een JSON object:

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

`typ` (het type) is `JWT`, om aan te geven dat dit een JSON Web Token is. `alg` geeft aan welk algoritme is gebruikt
voor het genereren van de signature. Veelvoorkomende varianten zijn `HS256` (waarbij een secret key wordt gebruikt)
of `RS256` (waarbij een private key wordt gebruikt om te signen en een public key om de signature te controleren).

### 2. Payload

JWTs kunnen een hele hoop informatie bevatten over de gebruiker. Deze informatie kan zijn: hun naam, gebruikers-ID, e-mailadres, welke
toegangsrechten, enzovoort.

Hieronder vind je een voorbeeld:

```json
{
  "sub": "1234567890",
  "email": "jos@example.com",
  "iss": "https://example.com",
  "iat": 1764059441,
  "exp": 1764060341
}
```

Deze paren van sleutel en waarde worden *claims* genoemd. Enkele veelvoorkomende claims zijn:

- `sub` (Subject): dit geeft het onderwerp van de token aan (over wie of wat gaat het).
  Typisch bevat dit bijvoorbeeld een gebruikers-ID.
- `iat` (Issued At): dit geeft aan wanneer de token werd uitgegeven. Dit wordt uitgedrukt als een UNIX timestamp: het aantal seconden sinds 1 januari 1970.
- `exp` (Expiration Time): dit geeft aan wanneer de token vervalt. Na dit tijdstip is de token niet meer geldig. Dit is ook een UNIX timestamp.
- `iss` (Issuer): dit geeft aan wie de uitgever van de token is. Dit is een partij
  die vertrouwd wordt om deze tokens te maken. In stap 15 (Login) zullen we zelf de
  issuer worden.

Er kunnen nog verschillende andere claims in voorkomen. Sommige van die claims zijn
geregistreerd: op de site van [IANA](Internet Assigned Numbers Authority) kan je een
[lijst van deze claims](https://www.iana.org/assignments/jwt/jwt.xhtml) vinden.

### 3. Signature

De signature wordt toegevoegd om ervoor te zorgen dat tokens niet vervalst kunnen worden en dus enkel door een vertrouwde issuer kunnen worden gemaakt. De issuer gebruikt een secret key (in het geval van bijvoorbeeld HS256) of een private key
(in het geval van bijvoorbeeld RS256) om deze signature te genereren op basis van
de header en payload.

## Stappen

### 1. Secret key genereren

We zullen voor onze implementatie gebruik maken van HS256 (een afkorting van HMAC-SHA256), dus hebben we een secret key nodig. Voor HMAC-SHA256 gebruik je
best een key van minstens 32 bytes. Hier zullen we een kort programma voor
schrijven in Java:

```java
package be.ucll.backend2.util;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Genereer 32 random bytes
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        // Converteer naar Base64 url encoding
        final var base64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(key);
        // Output het resultaat
        System.out.println(base64Key);
    }
}
```

Dit programma kan je vinden in [KeyGenerator.java](./src/main/java/be/ucll/backend2/util/KeyGenerator.java). Het genereert eerst 32 willekeurige bytes. We gebruiken hier `SecureRandom` voor om te voorkomen
dat de key voorspeld kan worden. Als we deze 32 willekeurige bytes hebben converteren we dit naar een
leesbaar formaat, namelijk base64url-encoding.

Als we dit programma nu uitvoeren krijgen we een secret key. We zullen vanaf nu als voorbeeld de volgende secret key gebruiken:

```
idrnjzRAHEdRcoaENH6KjPL4Z1oQFKeCMiL2snW0QRE
```

Het spreekt voor zich dat je zelf een andere secret key aanmaakt als je dit in production code wil gebruiken.

### 2. Secret key instellen

Nu we een secret key hebben kunnen we deze instellen. Voorlopig zullen we hier nog een hardcoded string
voor gebruiken, maar we zullen dit in een latere stap veranderen. We voegen de volgende code toe
aan [SecurityConfig.java](./src/main/java/be/ucll/backend2/config/SecurityConfig.java):

```java
@Bean
public SecretKey secretKey() {
    // TODO: set secret key via property
    final var secretBytes = Base64.getUrlDecoder().decode("idrnjzRAHEdRcoaENH6KjPL4Z1oQFKeCMiL2snW0QRE");
    return new SecretKeySpec(secretBytes, "HmacSHA256");
}
```

### 3. Extra dependency

Nu willen we Spring Security gaan instellen om JWTs te accepteren. Hiervoor gaan we gebruik maken
van [Spring OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html). Daarvoor voegen we het volgende toe aan onze dependencies in [pom.xml](pom.xml):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### 4. Van Basic authentication naar JWT

Nu kunnen we onze `SecurityFilterChain` aanpassen. We verwijderen basic authentication en stellen in
dat we Spring OAuth2 Resource Server met JWTs willen gebruiken. We kunnen ook instellen dat we stateless
sessies gaan gebruiken: er wordt op de server geen informatie bijgehouden over de huidige sessie van de
gebruiker. Dit geeft ons de volgende [`SecurityFilterChain`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java):

```java
@Bean
@Order(1)
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(csrf -> csrf.disable())
            // Gebruik stateless sessies
            .sessionManagement(
                    sessionManagement ->
                            sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Gebruik JWTs
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .authorizeHttpRequests(
                    authorizeRequests ->
                            authorizeRequests
                                    .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                                    .requestMatchers("/error").permitAll()
                                    .anyRequest().authenticated()
            )
            .build();
}
```

### 5. JwtDecoder instellen

Als we nu onze backend proberen te starten zullen we de volgende error te zien krijgen:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Method securityFilterChain in be.ucll.backend2.config.SecurityConfig required a bean of type 'org.springframework.security.oauth2.jwt.JwtDecoder' that could not be found.


Action:

Consider defining a bean of type 'org.springframework.security.oauth2.jwt.JwtDecoder' in your configuration.
```

We hebben nog niet ingesteld op welke manier tokens moeten decoded worden. Daarvoor moeten we een
`JwtDecoder` aanmaken. Hiervoor maken een extra bean aan in
[`SecurityConfig.java`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java):

```java
@Bean
public JwtDecoder jwtDecoder(SecretKey secretKey) {
    return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
}
```

Dit gebruikt onze `secretKey` bean die we eerder hebben gemaakt en stelt in dat we HS256 willen
gebruiken.

### 6. JWT genereren

We hebben nog geen code gemaakt om JWTs te genereren. We kunnen dit voorlopig wel doen via
[jwt.io](https://jwt.io). Let op: deze site belooft weliswaar dat de secret keys die je invoert niet
naar de server zullen worden gestuurd, maar het is in het algemeen een slecht idee om je secret
keys die je in production gebruikt zomaar te pasten op websites.

Ga naar het JWT Encoder tabblad:
![](./doc/images/jwt_encoder_tab.png)

Het algoritme is standaard al ingesteld op HS256 dus de header moeten we niet meer aanpassen:
![](./doc/images/jwt_header.png)

De secret key gaan we wel aanpassen naar onze eigen secret key. Let op dat je Encoding
Format op base64url instelt:
![](./doc/images/jwt_secret.png)

Nu kunnen we de payload instellen. Bijvoorbeeld:

```json
{
  "sub": "1",
  "email": "editor@example.com",
  "iat": 1516239022
}
```

Let op dat hoewel het subject hier een ID is, dat dit nog steeds een string moet zijn, vandaar de dubbele
aanhalingstekens.

Je zal nu aan de rechterkant de JSON Web Token moeten zien staan:

![](./doc/images/jwt.png)

Deze kan je nu instellen als Bearer token als je requests uitvoert.

### 7. Rollen toevoegen aan JWT

Probeer nu bijvoorbeeld `GET /api/v1/actors` te requesten met de JWT die we hebben gegenereerd.
We krijgen nu nog altijd 403 Forbidden. Als we in de logs kijken zien we het volgende:

```
2025-11-25T12:57:24.955+01:00 DEBUG 33508 --- [Spring-Security] [nio-8080-exec-2] .s.r.w.a.BearerTokenAuthenticationFilter : Set SecurityContextHolder to JwtAuthenticationToken [Principal=org.springframework.security.oauth2.jwt.Jwt@86ddd679, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=null], Granted Authorities=[]]
2025-11-25T12:57:24.964+01:00 DEBUG 33508 --- [Spring-Security] [nio-8080-exec-2] o.s.security.web.FilterChainProxy        : Secured GET /api/v1/actors
2025-11-25T12:57:24.992+01:00 DEBUG 33508 --- [Spring-Security] [nio-8080-exec-2] horizationManagerBeforeMethodInterceptor : Authorizing method invocation ReflectiveMethodInvocation: public java.util.List be.ucll.backend2.controller.ActorController.getAllActors(); target is of class [be.ucll.backend2.controller.ActorController]
2025-11-25T12:57:25.055+01:00 DEBUG 33508 --- [Spring-Security] [nio-8080-exec-2] horizationManagerBeforeMethodInterceptor : Failed to authorize ReflectiveMethodInvocation: public java.util.List be.ucll.backend2.controller.ActorController.getAllActors(); target is of class [be.ucll.backend2.controller.ActorController] with authorization manager org.springframework.security.authorization.method.PreAuthorizeAuthorizationManager@76550580 and result ExpressionAuthorizationDecision [granted=false, expressionAttribute=hasRole('READER')]
```

De eerste en de laatste lijn zijn hier vooral interessant. De eerste lijn zegt dat we authenticated
zijn met een `JwtAuthenticationToken` met als principal een `Jwt`-object. Er staat ook dat er geen granted
authorities zijn. In de laatste lijn staat er dat de check `hasRole('READER')` niet slaagt. We moeten dus
nog rollen toevoegen aan onze token.

De granted authorities worden automatisch aangevuld op basis van wat er in de `scope`-claim zit. Laten
we nu onze JWT genereren met de volgende payload:

```json
{
  "sub": "1",
  "email": "editor@example.com",
  "iat": 1516239022,
  "scope": "ROLE_READER ROLE_EDITOR"
}
```

Je zal nu zien dat de eerste lijn in de logs is aangepast naar:

```
2025-11-25T13:04:59.399+01:00 DEBUG 33508 --- [Spring-Security] [nio-8080-exec-5] .s.r.w.a.BearerTokenAuthenticationFilter : Set SecurityContextHolder to JwtAuthenticationToken [Principal=org.springframework.security.oauth2.jwt.Jwt@4ab315b9, Credentials=[PROTECTED], Authenticated=true, Details=WebAuthenticationDetails [RemoteIpAddress=0:0:0:0:0:0:0:1, SessionId=null], Granted Authorities=[SCOPE_ROLE_READER, SCOPE_ROLE_EDITOR]]
```

De rollen worden nu wel uit de token gehaald, maar er wordt een extra `SCOPE_` prefix toegevoegd. We
kunnen deze verwijderen met de volgende instelling in [`application.yaml`](./src/main/resources/application.yaml):

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          authority-prefix: ""
```

## Wat zien we nu?

Als we nu een request uitvoeren met een token die we hebben gegenereerd via [jwt.io](https://jwt.io)
zal deze slagen. De rollen worden uit de `scope`-claim gehaald en doordat we de `authority-prefix` hebben
aangepast naar de lege string slagen de `hasRole(...)` checks.

Als we echter proberen om bijvoorbeeld `GET /api/v1/users/1` uit te voeren krijgen we een 500 error.
Dit gaan we in de volgende stap proberen te fixen.

## Conclusies

- Met JSON Web Tokens kunnen we toegang krijgen zonder telkens opnieuw het wachtwoord
  mee te sturen en dit te controleren in de database.
- JWTs zijn stateless: er moet op de server niets bijgehouden worden.
- Spring OAuth2 Resource Server heeft JWT-ondersteuning ingebouwd.
- Met `SecureRandom` kunnen we een secret key genereren.
- Met een `JwtDecoder` kunnen we instellen hoe JWTs decoded moeten worden (welk algoritme, welke key).

## Volgende stappen

In de volgende stap gaan we de 500 error bij sommige requests onderzoeken en zien hoe we dit kunnen
oplossen. In een latere stap gaan we ook een endpoint voorzien om in te loggen en dus om een nieuwe
token te genereren.
