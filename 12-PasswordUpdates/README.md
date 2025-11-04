# Automatische wachtwoord updates

## Wat gaan we doen?

In [data.sql](./src/main/resources/data.sql) voegen we een gebruiker
toe met een uitermate onveilige hash voor het wachtwoord: `{noop}`, of:
helemaal niet gehasht.

We zouden er graag voor willen zorgen dat als gebruikers inloggen met
hun wachtwoord, dat er automatisch een veiligere hash wordt opgeslagen
in de database.

## Stappen

Net zoals we eerder een eigen `UserDetailsService` implementatie hebben gemaakt
om aan Spring Security duidelijk te maken hoe we gebruikers uit de database
moeten halen, gaan we nu `UserDetailsPasswordService` implementeren. Voor het
gemak doen we dit in
[`UserDetailsServiceImpl`](./src/main/java/be/ucll/backend2/service/UserDetailsServiceImpl.java):

```java
@Override
public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
    if (!(userDetails instanceof UserDetailsImpl)) {
        // Don't know how to update this
        return userDetails;
    }
    final var oldUser = ((UserDetailsImpl) userDetails).user();
    oldUser.setHashedPassword(newPassword);
    final var updatedUser = userRepository.save(oldUser);
    return new UserDetailsImpl(updatedUser);
}
```

`updatePassword` is een methode met twee argumenten: een `UserDetails` en een `String`.
De `UserDetails` is het object dat gereturnd werd in `loadUserByUsername`, de `String`
is de nieuwe hash. We moeten het wachtwoord dus niet opnieuw hashen: dat doet Spring
Security al achter de schermen voor ons.

Omdat we weten dat onze `UserDetails` een instantie is van `UserDetailsImpl` kunnen
we dit casten en de `User` eruit halen. Nu we het `user`-object hebben kunnen we
de hash updaten naar de nieuwe hash en dit opslaan in de database. Tenslotte kunnen
we een nieuw `UserDetailsImpl` object returnen met onze geüpdatete gebruiker.

## Wat zien we nu?

Start nu de applicatie op en kijk in de H2 Console. Je zal zien dat het wachtwoord
van `editor@example.com` in plain text in de database zit:

![](./doc/images/plaintext.png)

Als je nu eender welke endpoint gebruikt
(bijvoorbeeld GET http://localhost:8080/api/v1/actors)
zal je daarna zien dat het wachtwoord geüpdatet is:

![](./doc/images/hashed.png)

Als je nu dus authenticeert met een wachtwoord zal Spring
Security vanzelf het wachtwoord opnieuw hashen als de manier
waarop het in de database zat niet veilig genoeg was.

## Conclusies

- Door een `UserDetailsPasswordService` te implementeren
  kunnen we ervoor zorgen dat wachtwoorden vanzelf opnieuw
  gehasht worden met een veiligere hash-functie als dat nodig
  is.

## Volgende stappen

Tot nu toe hebben we altijd HTTP Basic authentication gebruikt.
We willen echter liever overstappen op JSON Web Tokens (JWTs).
Dat gaan we doen in de volgende stap.
