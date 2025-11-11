package ca.sheridancollege.odedaaja.Locker.domain;

import java.time.OffsetDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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
public class NotificationLog {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;

    @Enumerated(EnumType.STRING)   
    private EmailType type;

    @Enumerated(EnumType.STRING)  
    private EmailStatus status;

    private String providerMessageId;
    private String errorMessage;
    private OffsetDateTime createdAt;
}