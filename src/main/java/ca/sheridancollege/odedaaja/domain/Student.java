package ca.sheridancollege.odedaaja.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Users user;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Program program;
}


