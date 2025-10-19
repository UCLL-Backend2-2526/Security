# Geavanceerde autorisatie

## Wat gaan we doen?

We hebben tot nu toe vrij simplistische autorisatieregels ingesteld op basis van rollen. Vaak
is een regel echter niet zo simpel. Wat als we bijvoorbeeld willen instellen dat een gebruiker
enkel de eigen informatie mag aanpassen?

In deze stap gaan we twee endpoints toevoegen:

- `GET /api/v1/users/{id}` om gebruikersinformatie op te halen
- `PUT /api/v1/users/{id}` om gebruikersinformatie aan te passen

We willen dat beide endpoints enkel gebruikt kunnen worden door de gebruiker zélf, m.a.w.:
de `id` parameter in de URL moet overeenkomen met de `id` van de huidige gebruiker.

## Stappen

### 1. Nieuwe endpoints implementeren

We beginnen met het implementeren van de nieuwe endpoints.

We zullen eerst een [`UserNotFoundException`](./src/main/java/be/ucll/backend2/exception/UserNotFoundException.java) toe voor
het geval dat de gebruiker met de gegeven `id` niet bestaat:

```java
public class UserNotFoundException extends Exception {
    public UserNotFoundException(long id) {
        super("Could not find user with id " + id);
    }
}
```

De `CreateUserDto` hernoemen we naar [`UserDto`](./src/main/java/be/ucll/backend2/controller/dto/UserDto.java), aangezien we die
ook kunnen gebruiken voor `PUT /api/v1/users/{id}`.

Voor `GET /api/v1/users/{id}` implementeren we de `getUser`-methode in [`UserController`](./src/main/java/be/ucll/backend2/controller/UserController.java):

```java
@GetMapping("/{id}")
public User getUser(@PathVariable long id) throws UserNotFoundException {
    return userService.getUser(id);
}
```

We implementeren dan ook de `getUser`-methode in [`UserService`](./src/main/java/be/ucll/backend2/service/UserService.java):

```java
public User getUser(long id) throws UserNotFoundException {
    return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
}
```

We proberen dus de gebruiker op te zoeken op basis van `id` en als deze niet gevonden wordt gooien we een `UserNotFoundException`.

Voor `PUT /api/v1/users/{id}` implementeren we de `updateUser`-methode in [`UserController`](./src/main/java/be/ucll/backend2/controller/UserController.java):

```java
@PutMapping("/{id}")
public User updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) throws UserNotFoundException, EmailAddressNotUniqueException {
    return userService.updateUser(id, userDto);
}
```

We implementeren dan ook de `updateUser`-methode in [`UserService`](./src/main/java/be/ucll/backend2/service/UserService.java):

```java
public User updateUser(long id, UserDto userDto) throws UserNotFoundException, EmailAddressNotUniqueException {
    final var user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    user.setEmailAddress(userDto.emailAddress());
    final var hashedPassword = passwordEncoder.encode(userDto.password());
    user.setHashedPassword(hashedPassword);
    try {
        return userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
        throw new EmailAddressNotUniqueException(userDto.emailAddress());
    }
}
```

Deze methode is iets complexer:

1. We zoeken eerst de gebruiker op en gooien een `UserNotFoundException` als deze niet wordt gevonden.
2. We updaten het e-mailadres (`user.setEmailAddress(...)`) en wachtwoord (`user.setHashedPassword(...)`). Het nieuwe wachtwoord wordt eerst gehasht (`passwordEncoder.encode(...)`).
3. We slaan de gebruiker op in de database (`userRepository.save(user)`). Als we hier echter hadden geprobeerd om het wachtwoord te veranderen naar een reeds gebruikt wachtwoord
   zal dit resulteren in een `DataIntegrityViolationException`. Vandaar dat we in dit geval een `EmailAddressNotUniqueException` gooien.

### 2. De "authentication principal" ophalen met `@AuthenticationPrincipal`

Met de `@AuthenticationPrincipal` annotatie kunnen we de "authentication principal" ophalen. In dit geval is dat onze `UserDetailsImpl`.
We zullen dit toepassen op de `getUser`-methode in [`UserController`](./src/main/java/be/ucll/backend2/controller/UserController.java):

```java
@GetMapping("/{id}")
public User getUser(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable long id) throws UserNotFoundException {
    return userService.getUser(id);
}
```

We kunnen deze `userDetails` nu gebruiken in de implementatie van `getUser`. Wat we ook kunnen doen is naar onze argumenten refereren
met het hekje (`#`) in `@PreAuthorize`:

```java
@GetMapping("/{id}")
@PreAuthorize("#userDetails.user().id == #id")
public User getUser(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable long id) throws UserNotFoundException {
    return userService.getUser(id);
}
```

Deze `@PreAuthorize(...)` check kijkt nu na of de `id` in onze `userDetails` overeenkomt met de `id` in het pad.

### 3. Rechtstreeks naar de authentication principal verwijzen met `principal`

We kunnen de vorige stap ook op een kortere manier doen. Laten we dat doen met de `updateUser`-methode van [`UserController`](./src/main/java/be/ucll/backend2/controller/UserController.java).

Met `principal` kunnen we in `@PreAuthorize(...)` rechtreeks naar de authentication principal verwijzen:

```java
@PutMapping("/{id}")
@PreAuthorize("principal.user().id == #id")
public User updateUser(@PathVariable long id, @Valid @RequestBody UserDto userDto) throws UserNotFoundException, EmailAddressNotUniqueException {
    return userService.updateUser(id, userDto);
}
```

Het gebruik van `@AuthenticationPrincipal` is hier dus niet nodig, wat het aantal argumenten van onze methode wat beperkt.

Het nadeel is wel dat IDEs met speciale Spring ondersteuning, zoals `IntelliJ IDEA Ultimate`, nu niet weten wat het type is van de `principal` en
kan deze ons dus niet helpen met autocompletion.

## Wat zien we nu?

Probeer nu eens een extra gebruiker aan te maken. Observeer hoe gebruiker `1` (onze `editor@example.com` gebruiker) enkel en alleen
de eigen gegevens kan zien of aanpassen en niet die van gebruiker `2`. Je zal andersom zien dat gebruiker `2` de gegevens van gebruiker
`1` niet kan zien of aanpassen.

## Conclusies

- Met `@AuthenticationPrincipal` kunnen we refereren naar de "authentication principal" van de ingelogde gebruiker.
- Met het hekje (`#`) kunnen we in `@PreAuthorize(...)` verwijzen naar de argumenten van de methode.
- Met `principal` kunnen we in `@PreAuthorize(...)` rechtstreeks verwijzen naar de "authentication principal".
- Op basis van de gegevens van de "authentication principal" en de argumenten van de methode kunnen we toegang
  beperken.

## Volgende stappen

Onze integratietesten werken nu nog steeds niet. In de volgende stap zullen we ervoor zorgen dat de integratietesten slagen
met behulp van een gemockte gebruiker.
