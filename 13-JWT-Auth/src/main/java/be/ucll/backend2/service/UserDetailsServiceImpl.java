package be.ucll.backend2.service;

import be.ucll.backend2.model.UserDetailsImpl;
import be.ucll.backend2.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsPasswordService {
    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final var user = userRepository
            .findByEmailAddress(username.toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException(username));
        return new UserDetailsImpl(user);
    }

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
}
