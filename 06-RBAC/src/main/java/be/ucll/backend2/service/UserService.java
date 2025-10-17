package be.ucll.backend2.service;

import be.ucll.backend2.controller.dto.CreateUserDto;
import be.ucll.backend2.exception.EmailAddressNotUniqueException;
import be.ucll.backend2.model.User;
import be.ucll.backend2.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User registerUser(CreateUserDto createUserDto) throws EmailAddressNotUniqueException {
        final var hashedPassword = passwordEncoder.encode(createUserDto.password());
        final var user = new User(
                createUserDto.emailAddress(),
                hashedPassword
        );
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAddressNotUniqueException(createUserDto.emailAddress());
        }
    }
}
