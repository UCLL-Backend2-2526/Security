package be.ucll.backend2.service;

import be.ucll.backend2.controller.dto.UserDto;
import be.ucll.backend2.exception.EmailAddressNotUniqueException;
import be.ucll.backend2.exception.UserNotFoundException;
import be.ucll.backend2.model.User;
import be.ucll.backend2.model.UserDetailsImpl;
import be.ucll.backend2.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User getUser(long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public User registerUser(@RequestBody UserDto userDto) throws EmailAddressNotUniqueException {
        final var hashedPassword = passwordEncoder.encode(userDto.password());
        final var user = new User(
                userDto.emailAddress(),
                hashedPassword
        );
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAddressNotUniqueException(userDto.emailAddress());
        }
    }

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
}
