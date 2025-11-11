package ca.sheridancollege.odedaaja.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;

    private LocalDate birthdate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String address;
    private String city;
    private String country;
    private String bio;
    private String status;
    private String socialLinks;
    private String profilePicture;
    private String department;

    public enum Gender {
        Male, Female, Other
    }

    public enum Role {
        Student, Professor, Admin, Coordinator, Other,
    }
}
