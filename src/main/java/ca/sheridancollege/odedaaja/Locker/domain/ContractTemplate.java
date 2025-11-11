package ca.sheridancollege.odedaaja.Locker.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ContractTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int version;
    @Lob
    private String htmlTemplate;
    private boolean active = true;
}
