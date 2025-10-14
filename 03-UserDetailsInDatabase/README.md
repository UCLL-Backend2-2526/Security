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

## Wat zien we nu?

## Conclusies

## Volgende stappen
