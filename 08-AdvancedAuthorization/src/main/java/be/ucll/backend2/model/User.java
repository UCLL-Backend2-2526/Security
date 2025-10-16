package be.ucll.backend2.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Column(nullable = false, unique = true)
    private String emailAddress;

    @NotBlank
    @Column(nullable = false)
    @JsonIgnore
    private String hashedPassword;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    protected User() {}

    public User(String emailAddress, String hashedPassword) {
        setEmailAddress(emailAddress);
        setHashedPassword(hashedPassword);
        setRole(Role.READER);
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

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }
}
