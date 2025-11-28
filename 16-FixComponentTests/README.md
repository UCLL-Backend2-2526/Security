# Fix component tests

## Wat gaan we doen?

De component testen falen momenteel nog, omdat we geen authenticatie doen.

## Stappen

We zouden voor elke component test eerst een request kunnen uitvoeren om in te loggen.
Dat zou ook de meest "black box" manier zijn om te testen. Om onze testen echter iets
efficiënter te maken gaan we hier de black box doorbreken. We kunnen rechtstreeks
JWTs aanmaken via de `JwtService` en deze doorgeven als `Bearer` token in de testen:

```java
// Genereer token
final var token = jwtService.generateToken(1L, "jos@example.com", List.of("ROLE_EDITOR", "ROLE_READER"));

client.delete()
        .uri("/api/v1/actors/{id}", 1L)
        // Geef token mee met request
        .header("Authorization", "Bearer " + token)
        .exchange()
        .expectStatus().isNoContent();
```

Het resultaat zie je in [`ActorComponentTest`](./src/test/java/be/ucll/backend2/component/ActorComponentTest.java).

## Wat zien we nu?

Nu werken eindelijk alle testen!

## Conclusies

Door JWTs mee te geven als `Bearer` token kunnen we de component testen laten slagen.

## Volgende stappen

In de volgende stappen gaan we nog eigen properties instellen om de instellingen van
de JWTs aan te passen en een Swagger UI toevoegen.
