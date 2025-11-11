package ca.sheridancollege.odedaaja.Locker.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "lockerlocation") // 👈 explicitly name table (prevents case mismatch)
public class LockerLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String building;

    @Column(nullable = false)
    private String floor;
}
