# `SecurityFilterChain`

## Wat gaan we doen?

We willen het mogelijk maken voor gebruikers om zich te registreren. We zitten echter
met een kip-ei probleem: momenteel mogen enkel geregistreerde gebruikers requests uitvoeren.
We gaan er dus voor moeten zorgen dat de registratie endpoint bereikbaar is voor álle
gebruikers, ook al zijn gebruikers niet ingelogd.

## Stappen

### 1. PasswordEncoder

Als we wachtwoorden willen gaan opslaan in de database moeten
we ervoor zorgen dat deze gehasht zijn. Dit kunnen we doen
aan de hand van een `PasswordEncoder`.

We kunnen hiervoor in [`SecurityConfig`](./src/main/java/be/ucll/backend2/config/SecurityConfig.java)
een nieuwe bean aanmaken:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Dit creëert een `PasswordEncoder` die meerdere soorten hashes ondersteunt en gebruikt standaard
een voldoende sterke hashing methode. In dit geval gaat het over `bcrypt`. In een latere stap
gaan we zien hoe deze `PasswordEncoder` ook gebruikt wordt om oudere, minder veilige, hashes
te updaten naar een nieuwere hash.

### 2. `POST /api/v1/users` implementeren

Het implementeren van de `POST /api/v1/users` gaat redelijk hard zoals gekend:

- We maken een [`CreateUserDto`](./src/main/java/be/ucll/backend2/controller/dto/CreateUserDto.java)
- We maken een [`UserController`](./src/main/java/be/ucll/backend2/controller/UserController.java)
- We maken een exception voor wanneer een e-mailadres dubbel gebruikt wordt:
  [`EmailAddressNotUniqueException`](./src/main/java/be/ucll/backend2/exception/EmailAddressNotUniqueException.java).
  We zorgen ervoor dat dit een gepaste error terug geeft aan de gebruiker.
- We maken een [`UserService`](./src/main/java/be/ucll/backend2): deze gebruikt de
  `PasswordEncoder` om het wachtwoord te hashen. De `DataIntegrityViolationException` die
  zal optreden als een e-mailadres al in de database zit zullen we opvangen en we zullen dan een
  `EmailAddressNotUniqueException` gooien.
- We voegen een `@JsonIgnore` toe bij [`hashedPassword`](./src/main/java/be/ucll/backend2/model/User.java). We willen immers niet dat
  de password hash terug naar de gebruiker gestuurd wordt.

### 3. Registratie toestaan voor iedereen

We moeten er nu voor zorgen dat iedereen zich kan registreren: `POST /api/v1/users` moet dus
voor iedereen toegankelijk zijn. Hiervoor moeten we onze `SecurityFilterChain` aanpassen:
we veranderen de `authorizeHttpRequests` naar:

```
.authorizeHttpRequests(
        authorizeRequests ->
            authorizeRequests
                // Geef iedereen toegang tot POST /api/v1/users
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                // Voor elke andere requests moeten gebruikers authenticated zijn
                .anyRequest().authenticated()
        )
```

### 4. `data.sql` verwijderen

Nu we gebruikers kunnen aanmaken, zouden we `data.sql` kunnen verwijderen.

## Wat zien we nu?

Nu zou registratie moeten lukken.

## Conclusies

- Met de `PasswordEncoder` kunnen we onze wachtwoorden hashen
- Met `requestMatchers` kunnen we toegang regelen tot specifieke endpoints

## Volgende stappen

- Momenteel kan elke ingelogde gebruiker alles doen. In de volgende stap gaan we ervoor zorgen dat gebruikers andere
  dingen kunnen doen afhankelijk van hun rol.
