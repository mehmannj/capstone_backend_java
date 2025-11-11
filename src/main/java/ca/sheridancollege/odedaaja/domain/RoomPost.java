package ca.sheridancollege.odedaaja.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String room;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String postedBy;
    private String description;
    private String location;
    private int capacity;
    private String resources;
    private String department;
    private String program;
    private String course;
    private Boolean quiet; // true if room is a quiet space
    private String roomType; // LIBRARY/CLASSROOM/etc as string for backward compatibility

    @ManyToOne
    private Users users;
}
