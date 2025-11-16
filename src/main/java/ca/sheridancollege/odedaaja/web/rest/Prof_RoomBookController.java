package ca.sheridancollege.odedaaja.web.rest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // For getting current user
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.sheridancollege.odedaaja.domain.RoomPost;
import ca.sheridancollege.odedaaja.domain.RoomRequest;
import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import ca.sheridancollege.odedaaja.service.Prof_RoomBook_Service;
import ca.sheridancollege.odedaaja.service.NotificationService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/profRoomBook")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://instimanage.netlify.app" })

public class Prof_RoomBookController {
    private final Prof_RoomBook_Service profRoomBookService;
    private final UserRepo userRepo;
    private final NotificationService notificationService; 

    @PostMapping("/roomPost")
    public RoomPost createRoomPost(@RequestBody RoomPost roomPost, 
                                     Authentication authentication) {
        // Handle both authenticated and unauthenticated requests
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Users user = userRepo.findByUsername(username)
                                   .orElseThrow(() -> new RuntimeException("User not found"));
            roomPost.setUsers(user);
            roomPost.setPostedBy(username);
        } else {
            // If unauthenticated, try to use the postedBy from payload to link user
            String providedUsername = roomPost.getPostedBy();
            if (providedUsername != null && !providedUsername.isBlank()) {
                userRepo.findByUsername(providedUsername).ifPresent(user -> roomPost.setUsers(user));
            } else {
            roomPost.setPostedBy("anonymous");
            }
        }
        RoomPost savedPost = profRoomBookService.saveRoom(roomPost);
        
        // Create notification for admin
        notificationService.createRoomPostNotification(roomPost.getRoom(), roomPost.getPostedBy());
        
        return savedPost;
    }

    
    @GetMapping("/roomPost")
    public List<RoomPost> getAllRoomPosts() {
        return profRoomBookService.getAllPostedRooms();
    }


    @DeleteMapping("/roomPost/{id}")
    public ResponseEntity<?> deleteRoomPost(@PathVariable Long id) {
        profRoomBookService.deleteRoomPost(id);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/roomRequest")
    public RoomRequest createRoomRequest(@RequestBody RoomRequest roomRequest, 
                                         Authentication authentication) {
        // Handle both authenticated and unauthenticated requests
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Users user = userRepo.findByUsername(username)
                                 .orElseThrow(() -> new RuntimeException("User not found"));
            roomRequest.setUsers(user);
            roomRequest.setPostedBy(username);
        } else {
            // If unauthenticated, try to use the postedBy from payload to link user
            String providedUsername = roomRequest.getPostedBy();
            if (providedUsername != null && !providedUsername.isBlank()) {
                userRepo.findByUsername(providedUsername).ifPresent(user -> roomRequest.setUsers(user));
            } else {
            roomRequest.setPostedBy("anonymous");
            }
        }
        
        RoomRequest savedRequest = profRoomBookService.saveRoomRequest(roomRequest);
        
        // Create notification for admin
        notificationService.createRoomRequestNotification(roomRequest.getDescription(), roomRequest.getPostedBy());
        
        return savedRequest;
    }
    
    
    

    @GetMapping("/roomRequest")
    public List<RoomRequest> getAllRoomRequests() {
        return profRoomBookService.getAllRoomRequests();
    }

  
    @DeleteMapping("/roomRequest/{id}")
    public ResponseEntity<?> deleteRoomRequest(@PathVariable Long id) {
        profRoomBookService.deleteRoomRequest(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roomPost/user/{username}")
    public List<RoomPost> getRoomPostsByUsername(@PathVariable String username) {
        return profRoomBookService.getRoomPostsByUsername(username);
    }

 
    @GetMapping("/roomRequest/user/{username}")
    public List<RoomRequest> getRoomRequestsByUsername(@PathVariable String username) {
        return profRoomBookService.getRoomRequestsByUsername(username);
    }
    
    @GetMapping("/roomPost/{id}")
    public RoomPost getRoomPostById(@PathVariable Long id) {
        return profRoomBookService.getRoomPostById(id);
    }
    
    // New endpoints for time-based filtering and current user data
    @GetMapping("/roomPost/current")
    public List<RoomPost> getCurrentUserRoomPosts(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return profRoomBookService.getRoomPostsByUsername(username);
        }
        return List.of(); // Return empty list if not authenticated
    }
    
    @GetMapping("/roomRequest/current")
    public List<RoomRequest> getCurrentUserRoomRequests(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return profRoomBookService.getRoomRequestsByUsername(username);
        }
        return List.of(); // Return empty list if not authenticated
    }
    
    @GetMapping("/roomPost/active")
    public List<RoomPost> getActiveRoomPosts(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getActiveRoomPosts(filterDate);
    }
    
    @GetMapping("/roomRequest/active")
    public List<RoomRequest> getActiveRoomRequests(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getActiveRoomRequests(filterDate);
    }
    
    @GetMapping("/roomPost/past")
    public List<RoomPost> getPastRoomPosts(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getPastRoomPosts(filterDate);
    }
    
    @GetMapping("/roomRequest/past")
    public List<RoomRequest> getPastRoomRequests(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getPastRoomRequests(filterDate);
    }
    
    
//    @PatchMapping("/user/{id}")
//    public Users patchUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
//        return profRoomBookService.patchUser(id, updates);
//    }
//    
//    @GetMapping("/roomRequest/{id}")
//    public RoomRequest getRoomRequestById(@PathVariable Long id) {
//        return profRoomBookService.getRoomRequestById(id);
//    }
//
//    @PutMapping("/roomRequest/{id}")
//    public RoomRequest updateRoomRequest(@PathVariable Long id, @RequestBody RoomRequest roomRequest) {
//        return profRoomBookService.updateRoomRequest(id, roomRequest);
//    }
//

//
//    @PutMapping("/roomPost/{id}")
//    public RoomPost updateRoomPost(@PathVariable Long id, @RequestBody RoomPost roomPost) {
//        return profRoomBookService.updateRoomPost(id, roomPost);
//    }
//    
//
//    // Users endpoints
//    @PostMapping("/user")
//    public Users createUser(@RequestBody Users user) {
//        return profRoomBookService.saveUser(user);
//    }
//
//    @GetMapping("/user")
//    public List<Users> getUsers() {
//        return profRoomBookService.getAllUsers();
//    }
//
//    @GetMapping("/user/{id}")
//    public Users getUserById(@PathVariable Long id) {
//        return profRoomBookService.getUserById(id);
//    }
//
//    @PutMapping("/user/{id}")
//    public Users updateUser(@PathVariable Long id, @RequestBody Users user) {
//        return profRoomBookService.updateUser(id, user);
//    }
//
//    @DeleteMapping("/user/{id}")
//    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
//        profRoomBookService.deleteUser(id);
//        return ResponseEntity.ok().build();
//    }

 

}
