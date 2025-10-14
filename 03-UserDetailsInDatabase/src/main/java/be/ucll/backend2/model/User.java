package be.ucll.backend2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    private String emailAddress;

    @NotBlank
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
