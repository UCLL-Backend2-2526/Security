package be.ucll.backend2.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserDetailsImpl(User user) implements UserDetails {
    @Override
    public String getUsername() {
        return user.getEmailAddress();
    }

    @Override
    public String getPassword() {
        return user.getHashedPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final List<String> roles = switch (user.getRole()) {
            case Role.READER -> List.of("ROLE_READER");
            case Role.EDITOR -> List.of("ROLE_READER", "ROLE_EDITOR");
        };
        return roles.stream().map(role -> new SimpleGrantedAuthority(role)).toList();
    }
}
