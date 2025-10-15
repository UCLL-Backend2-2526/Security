# UserDetails in database opslaan

## Wat gaan we doen?

In de vorige stap hebben we de `UserDetails` in de code gezet. Nu willen we
gebruikers uit de database halen zodat we er in een latere stap voor kunnen
zorgen dat gebruikers zich kunnen registreren.

We zullen dus het volgende moeten voorzien:

- Een user entity die we in de database kunnen opslaan, met:
  - Gebruikersnaam, we zullen hier een e-mailadres voor gebruiken
  - Wachtwoord (gehasht)
- Een eigen service die de `UserDetailsService` interface implementeert
  - `loadUserByUsername` moet een `UserDetails` returnen. Laten we hiervoor
    een eigen klasse (of record) maken die de `UserDetails` interface implementeert en
    alle gegevens uit de user entity haalt.

## Stappen

### 1. Maak een `User` entity

We voegen eerst een [`User`](./src/main/java/be/ucll/backend2/model/User.java)-klasse toe. Deze
entity heeft een e-mailadres die we als username gebruiken. Dit e-mailadres veranderen we naar lowercase
omdat we willen dat bijvoorbeeld `user@example.com` en `USER@EXAMPLE.COM` niet worden gezien als andere
gebruikers. We kunnen afdwingen dat e-mailadressen uniek zijn door `unique = true` mee te geven als optie
aan de `@Column` annotatie.

We voegen het wachtwoord toe aan de gebruiker (in gehashte vorm). We noemen dit veldje `hashedPassword` om
duidelijk te maken dat dit over een gehasht wachtwoord gaat.

We voegen ook de bijhorende SQL-code toe aan [`schema.sql`](./src/main/resources/schema.sql) en
voegen een testgebruiker toe via [`data.sql`](./src/main/resources/data.sql).

Ook voegen we gepaste validatie toe: `@Email` voor het e-mailadres en `@NotBlank` voor `hashedPassword`.

De volgende optie zorgt ervoor dat we geen problemen krijgen met de tabelnaam `user`, wat normaal een *reserved keyword*
is:

```properties
spring.jpa.properties.hibernate.auto_quote_keyword=true
```

### 2. Maak een `UserRepository` aan

Om onze users uit de database te kunnen halen, hebben we een
[`UserRepository`](./src/main/java/be/ucll/backend2/repository/UserRepository.java) nodig. Hier voegen we
de methode `findByEmailAddress` toe om gebruikers te kunnen opzoeken op basis van e-mailadres:

```java
Optional<User> findByEmailAddress(String emailAddress);
```

### 3. Verwijder de `userDetailsService` bean

We zullen eerst de `userDetailsService` bean die we in de vorige stap hadden toegevoegd aan
[`SecurityConfig.java`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java) moeten verwijderen.
We gaan nu onze eigen implementatie voorzien.

### 4. Maak een eigen `UserDetailsImpl`

Laten we een eigen implementatie van `UserDetails` maken. Zo kunnen we gemakkelijk de `User` hieruit halen.

We voegen de record [`UserDetailsImpl`](./src/main/java/be/ucll/backend2/model/UserDetailsImpl.java) toe. Deze
implementeert `getUsername()` door `user.getEmailAddress()` te gebruiken en `getPassword()` door
`user.getHashedPassword()` te gebruiken. Voorlopig returnt `getAuthorities()` nog een lege set: hier gaan we
in een latere stap nog op terugkomen.

### 5. Voeg eigen `UserDetailsServiceImpl` toe

Nu kunnen we een klasse toevoegen die `UserDetailsService` implementeert:
[`UserDetailsServiceImpl`](./src/main/java/be/ucll/backend2/service/UserDetailsServiceImpl.java).

We gebruiken een e-mailadres (in lowercase) als gebruikersnaam, dus de `loadUserByUsername` methode zoekt
het e-mailadres, geconverteerd naar lowercase, op met `findByEmailAddress`.

Als we de gebruiker niet vinden (`findByEmailAddress` returnt een lege `Optional`), kunnen we een
`UsernameNotFoundException` gooien.

Als we de `User` wel gevonden hebben, moeten we dit enkel nog verpakken in onze eigen `UserDetailsImpl` en returnen.

## Wat zien we nu?

Als we nu de applicatie starten zullen we het volgende logbericht zien:

```
2025-10-15T20:09:46.930+02:00  INFO 8136 --- [Spring-Security] [           main] r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name userDetailsServiceImpl
```

We gebruiken nu dus onze eigen implementatie. Dat zien we aan de naam: `userDetailsServiceImpl`, afgeleid van de naam
van de klasse: `UserDetailsServiceImpl`.

Je zou nu in je requests de gegevens van de user(s) in de database moeten kunnen gebruiken.

## Conclusies

- Om gebruikers uit de database te halen moeten we onze eigen implementatie maken van `UserDetailsService`: Spring
  Security weet immers niet vanzelf hoe gebruikers in de database zijn opgeslagen.

## Volgende stappen

- We kunnen vooraf gebruikers toevoegen aan de database, maar nog niet registeren. Hier gaan we in een volgende stap
  naartoe werken.
- Als we via Postman POST, PUT of DELETE requests willen uitvoeren in de plaats van GET requests, zullen we merken
  dat deze requests nog altijd falen. Dit gaan we proberen oplossen in de volgende stap.