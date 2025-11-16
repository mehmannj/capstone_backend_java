package ca.sheridancollege.odedaaja.web.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
@RequestMapping("/api/admin/rooms")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173","https://instimanage.netlify.app" })
public class AdminRoomController {
    
    private final Prof_RoomBook_Service profRoomBookService;
    private final UserRepo userRepo;
    private final NotificationService notificationService;

    // ==================== ROOM POSTS ADMIN ENDPOINTS ====================
    
    /**
     * Get all room posts (admin view)
     */
    @GetMapping("/posts")
    public List<RoomPost> getAllRoomPosts() {
        return profRoomBookService.getAllPostedRooms();
    }

    /**
     * Get room post by ID
     */
    @GetMapping("/posts/{id}")
    public RoomPost getRoomPostById(@PathVariable Long id) {
        return profRoomBookService.getRoomPostById(id);
    }

    /**
     * Create room post (admin can post rooms)
     */
    @PostMapping("/posts")
    public RoomPost createRoomPost(@RequestBody RoomPost roomPost, 
                                   Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Users user = userRepo.findByUsername(username)
                                 .orElseThrow(() -> new RuntimeException("User not found"));
            roomPost.setUsers(user);
            roomPost.setPostedBy(username);
        } else {
            // For admin posts, use admin as the poster
            roomPost.setPostedBy("admin");
        }
        
        RoomPost savedPost = profRoomBookService.saveRoom(roomPost);
        
        // Create notification for all users about new room post
        notificationService.createRoomPostNotification(roomPost.getRoom(), roomPost.getPostedBy());
        
        return savedPost;
    }

    /**
     * Update room post
     */
    @PutMapping("/posts/{id}")
    public RoomPost updateRoomPost(@PathVariable Long id, @RequestBody RoomPost roomPost) {
        roomPost.setId(id);
        return profRoomBookService.saveRoom(roomPost);
    }

    /**
     * Delete room post (admin can delete any post)
     */
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deleteRoomPost(@PathVariable Long id) {
        profRoomBookService.deleteRoomPost(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get room posts by username
     */
    @GetMapping("/posts/user/{username}")
    public List<RoomPost> getRoomPostsByUsername(@PathVariable String username) {
        return profRoomBookService.getRoomPostsByUsername(username);
    }

    /**
     * Get active room posts
     */
    @GetMapping("/posts/active")
    public List<RoomPost> getActiveRoomPosts(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getActiveRoomPosts(filterDate);
    }

    /**
     * Get past room posts
     */
    @GetMapping("/posts/past")
    public List<RoomPost> getPastRoomPosts(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getPastRoomPosts(filterDate);
    }

    // ==================== ROOM REQUESTS ADMIN ENDPOINTS ====================
    
    /**
     * Get all room requests (admin view)
     */
    @GetMapping("/requests")
    public List<RoomRequest> getAllRoomRequests() {
        return profRoomBookService.getAllRoomRequests();
    }

    /**
     * Get room request by ID
     */
    @GetMapping("/requests/{id}")
    public RoomRequest getRoomRequestById(@PathVariable Long id) {
        return profRoomBookService.getRoomRequestById(id);
    }

    /**
     * Create room request (admin can create requests)
     */
    @PostMapping("/requests")
    public RoomRequest createRoomRequest(@RequestBody RoomRequest roomRequest, 
                                        Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Users user = userRepo.findByUsername(username)
                                 .orElseThrow(() -> new RuntimeException("User not found"));
            roomRequest.setUsers(user);
            roomRequest.setPostedBy(username);
        } else {
            roomRequest.setPostedBy("admin");
        }
        
        RoomRequest savedRequest = profRoomBookService.saveRoomRequest(roomRequest);
        
        // Create notification for all users about new room request
        notificationService.createRoomRequestNotification(roomRequest.getDescription(), roomRequest.getPostedBy());
        
        return savedRequest;
    }

    /**
     * Update room request
     */
    @PutMapping("/requests/{id}")
    public RoomRequest updateRoomRequest(@PathVariable Long id, @RequestBody RoomRequest roomRequest) {
        roomRequest.setId(id);
        return profRoomBookService.saveRoomRequest(roomRequest);
    }

    /**
     * Delete room request (admin can delete any request)
     */
    @DeleteMapping("/requests/{id}")
    public ResponseEntity<?> deleteRoomRequest(@PathVariable Long id) {
        profRoomBookService.deleteRoomRequest(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get room requests by username
     */
    @GetMapping("/requests/user/{username}")
    public List<RoomRequest> getRoomRequestsByUsername(@PathVariable String username) {
        return profRoomBookService.getRoomRequestsByUsername(username);
    }

    /**
     * Get active room requests
     */
    @GetMapping("/requests/active")
    public List<RoomRequest> getActiveRoomRequests(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getActiveRoomRequests(filterDate);
    }

    /**
     * Get past room requests
     */
    @GetMapping("/requests/past")
    public List<RoomRequest> getPastRoomRequests(@RequestParam(required = false) String date) {
        LocalDate filterDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return profRoomBookService.getPastRoomRequests(filterDate);
    }

    // ==================== ADMIN STATISTICS ENDPOINTS ====================
    
    /**
     * Get admin dashboard statistics
     */
    @GetMapping("/stats")
    public Map<String, Object> getAdminStats() {
        List<RoomPost> allPosts = profRoomBookService.getAllPostedRooms();
        List<RoomRequest> allRequests = profRoomBookService.getAllRoomRequests();
        
        LocalDate today = LocalDate.now();
        List<RoomPost> activePosts = profRoomBookService.getActiveRoomPosts(today);
        List<RoomRequest> activeRequests = profRoomBookService.getActiveRoomRequests(today);
        
        return Map.of(
            "totalRoomPosts", allPosts.size(),
            "totalRoomRequests", allRequests.size(),
            "activeRoomPosts", activePosts.size(),
            "activeRoomRequests", activeRequests.size(),
            "pastRoomPosts", allPosts.size() - activePosts.size(),
            "pastRoomRequests", allRequests.size() - activeRequests.size()
        );
    }

    /**
     * Get room posts count by user
     */
    @GetMapping("/posts/count-by-user")
    public Map<String, Long> getRoomPostsCountByUser() {
        return profRoomBookService.getRoomPostsCountByUser();
    }

    /**
     * Get room requests count by user
     */
    @GetMapping("/requests/count-by-user")
    public Map<String, Long> getRoomRequestsCountByUser() {
        return profRoomBookService.getRoomRequestsCountByUser();
    }
}
