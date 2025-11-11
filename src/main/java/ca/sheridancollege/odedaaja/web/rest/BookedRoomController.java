package ca.sheridancollege.odedaaja.web.rest;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.sheridancollege.odedaaja.domain.BookedRoom;
import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.repo.BookedRoomRepo;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/BookedRoom")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})

public class BookedRoomController {

    private final BookedRoomRepo bookedRoomRepository;
    private final UserRepo userRepo; 

    @PostMapping("/book")
    public ResponseEntity<BookedRoom> bookRoom(@RequestBody BookedRoom bookedRoom, Authentication authentication) {
        try {
            String username;
            Users user;
            
            if (authentication != null && authentication.isAuthenticated()) {
                username = authentication.getName();
                user = userRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                // Fallback: use the bookedBy field from the request
                username = bookedRoom.getBookedBy();
                if (username == null || username.isEmpty()) {
                    return ResponseEntity.badRequest().build();
                }
                user = userRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));
            }

            // Enforce student-specific booking rules
            if (user.getRole() == Users.Role.Student) {
                // Require quiet library rooms
                if (bookedRoom.getQuiet() == null || !bookedRoom.getQuiet()) {
                    return ResponseEntity.badRequest().build();
                }
                if (bookedRoom.getRoomType() == null || !"LIBRARY".equalsIgnoreCase(bookedRoom.getRoomType())) {
                    return ResponseEntity.badRequest().build();
                }

                // Max 3 hours duration
                if (bookedRoom.getStartTime() != null && bookedRoom.getEndTime() != null) {
                    var durationMinutes = java.time.Duration.between(
                        bookedRoom.getStartTime(), bookedRoom.getEndTime()).toMinutes();
                    if (durationMinutes > 180) {
                        return ResponseEntity.badRequest().build();
                    }
                }
            }

            bookedRoom.setBookedBy(username);
            bookedRoom.setUsers(user);
            BookedRoom savedBooking = bookedRoomRepository.save(bookedRoom);
            return ResponseEntity.ok(savedBooking);
        } catch (Exception e) {
            System.err.println("Error booking room: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public List<BookedRoom> getAllBookedRooms() {
        return bookedRoomRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookedRoom> getBookedRoomById(@PathVariable Long id) {
        return bookedRoomRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    
    @GetMapping("/user/{username}")
    public List<BookedRoom> getBookedRoomsByUsername(@PathVariable String username) {
        return bookedRoomRepository.findByBookedBy(username);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBookedRoom(@PathVariable Long id) {
        if (bookedRoomRepository.existsById(id)) {
            bookedRoomRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
