package ca.sheridancollege.odedaaja.Locker.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class RoomAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long coordinatorId;
    private String department;
    
    private String roomNumber;

    // Optional course context and semester window
    private Long courseId;
    private String courseName;
    private java.time.LocalDate semesterStart;
    private java.time.LocalDate semesterEnd;
}