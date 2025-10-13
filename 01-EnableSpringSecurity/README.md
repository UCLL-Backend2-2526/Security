# Enable Spring Security

## Wat doen we in deze stap?

We schakelen Spring Security in door de nodige dependencies toe te voegen aan `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Wat merken we?

### Falende testen

De component testen en HTTP-integratietesten falen: we krijgen telkens een `401 Unauthorized` of een `403 Forbidden` error.

### Logberichten

Als we de applicatie starten zien we in de logs berichten die lijken op de volgende:

```
2025-10-12T18:11:12.922+02:00  WARN 1524 --- [Spring-Security] [           main] .s.s.UserDetailsServiceAutoConfiguration : 

Using generated security password: 0bd6e09e-870e-4e1a-80e9-be43481a64bd

This generated password is for development use only. Your security configuration must be updated before running your application in production.

2025-10-12T18:11:12.933+02:00  INFO 1524 --- [Spring-Security] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name inMemoryUserDetailsManager
```

Het eerste bericht zegt dat er een wachtwoord automatisch is gegenereerd. Het tweede bericht
zegt dat de globale `AuthenticationManager` geconfigureerd is met een `UserDetailsService` bean
met naam `inMemoryUserDetailsManager`. Het tweede logbericht zullen we nog op terugkomen in de volgende stap.

### Browser

Probeer in je browser naar http://localhost:8080/api/v1/actors te gaan. Je zal zien dat je een login-venster krijgt.
Log jezelf in met username `user` en het wachtwoord dat je in de logs ziet. Nu zal je zien dat je wel toegang krijgt.

### Postman

We zien dat elke request die we proberen sturen met Postman ook resulteert in een `401 Unauthorized`. Hoe kunnen we
zonder login formulier toch onze inloggegevens sturen?

In de headers die we terug krijgen zien we het volgende:

```
WWW-Authenticate: Basic realm="Realm"
```

We kunnen dus inloggen met [HTTP Basic authentication](https://en.wikipedia.org/wiki/Basic_access_authentication). Dit
kunnen we instellen in Postman. We kunnen dit voor de volledige collection doen door naar de collection te gaan en de
Auth tab te kiezen. Stel als Auth Type `Basic Auth` in en geef als username `user` en als wachtwoord het automatisch
gegenereerd wachtwoord.

## Conclusies

- `spring-boot-starter-security` toevoegen als dependency zorgt er vanzelf voor dat security zo wordt geconfigureerd dat
  alle endpoints authenticatie vereisen.
- Er wordt vanzelf één gebruiker aangemaakt met username `user` en een automatisch gegenereerd wachtwoord dat in de logs
  getoond wordt.
- We kunnen met deze gebruiker inloggen in de browser, of via HTTP Basic Authentication.

## Volgende stappen

- We willen zelf gebruikersaccounts kunnen instellen in de plaats van de automatisch gegenereerde gebruiker. Dit doen
  we in de volgende stap.
- Onze testen falen: we gaan ervoor moeten zorgen dat we de nodige authenticatie doen in de testen.