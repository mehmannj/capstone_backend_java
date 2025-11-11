package ca.sheridancollege.odedaaja.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;
    private String building;
    private int capacity;

    @ManyToOne
    private Department department;

    @ManyToOne
    private Program program;

    @ManyToOne
    private Course course;

    private String resources; // comma-separated for simplicity

    private boolean quiet; // true for quiet study/library rooms

    @Enumerated(EnumType.STRING)
    private RoomType type; // LIBRARY, CLASSROOM, LAB, OTHER

    public enum RoomType { LIBRARY, CLASSROOM, LAB, OTHER }
}


