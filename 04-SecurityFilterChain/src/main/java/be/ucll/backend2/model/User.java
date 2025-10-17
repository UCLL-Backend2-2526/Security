package be.ucll.backend2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String emailAddress;

    @NotBlank
    @Column(nullable = false)
    private String hashedPassword;

    protected User() {}

    public User(String emailAddress, String hashedPassword) {
        setEmailAddress(emailAddress);
        setHashedPassword(hashedPassword);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress.toLowerCase(Locale.ROOT);
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }
}
