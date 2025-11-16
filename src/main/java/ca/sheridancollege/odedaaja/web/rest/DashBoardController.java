package ca.sheridancollege.odedaaja.web.rest;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import ca.sheridancollege.odedaaja.domain.DashBoard;
import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.domain.Student;
import ca.sheridancollege.odedaaja.repo.DashBoardRepo;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://instimanage.netlify.app" })

public class DashBoardController {

    private final DashBoardRepo dashBoardRepo;
    private final UserRepo userRepo;

    // POST - Create a new dashboard entry
    @PostMapping("/add")
    public ResponseEntity<DashBoard> addEntry(@RequestBody DashBoard dashBoard, Authentication authentication) {
        // Handle both authenticated and unauthenticated requests
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Users user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dashBoard.setUsers(user);
        } else {
            // For unauthenticated requests, try to find user from the request body
            if (dashBoard.getUsers() != null && dashBoard.getUsers().getUsername() != null) {
                String username = dashBoard.getUsers().getUsername();
                Users user = userRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                dashBoard.setUsers(user);
            } else {
                throw new RuntimeException("User information is required");
            }
        }

        DashBoard savedEntry = dashBoardRepo.save(dashBoard);
        return ResponseEntity.ok(savedEntry);
    }

    // GET - Get all dashboard entries by username
    @GetMapping("/user/{username}")
    public List<DashBoard> getEntriesByUsername(@PathVariable String username) {
        return dashBoardRepo.findByUsersUsername(username);
    }

    // GET - Get current user's dashboard entries
    @GetMapping("/user/current")
    public List<DashBoard> getCurrentUserEntries(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return dashBoardRepo.findByUsersUsername(username);
        }
        return List.of(); // Return empty list if not authenticated
    }

    // GET - Get student's class schedule based on their department/program
    @GetMapping("/student/classes")
    public List<DashBoard> getStudentClassSchedule(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Users user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // If user is a student, get their department and program
            if (user.getRole() == Users.Role.Student) {
                // Get all professor schedules from the same department as the student
                if (user.getDepartment() != null) {
                    return dashBoardRepo.findByUsersRole(Users.Role.Professor).stream()
                            .filter(schedule -> schedule.getUsers().getDepartment() != null && 
                                    schedule.getUsers().getDepartment().equals(user.getDepartment()))
                            .collect(java.util.stream.Collectors.toList());
                } else {
                    // If no department info, return all professor schedules
                    return dashBoardRepo.findByUsersRole(Users.Role.Professor);
                }
            }
        }
        return List.of(); // Return empty list if not authenticated or not a student
    }

    // PUT - Update a dashboard entry by ID
    @PutMapping("/{id}")
    public ResponseEntity<DashBoard> updateEntry(@PathVariable Long id, @RequestBody DashBoard dashBoard, Authentication authentication) {
        DashBoard existingEntry = dashBoardRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Dashboard entry not found"));

        // Handle both authenticated and unauthenticated requests
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Users user = userRepo.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            dashBoard.setUsers(user);
        } else {
            // For unauthenticated requests, try to find user from the request body
            if (dashBoard.getUsers() != null && dashBoard.getUsers().getUsername() != null) {
                String username = dashBoard.getUsers().getUsername();
                Users user = userRepo.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                dashBoard.setUsers(user);
            } else {
                // Keep existing user if no new user provided
                dashBoard.setUsers(existingEntry.getUsers());
            }
        }

        dashBoard.setId(id);
        DashBoard updatedEntry = dashBoardRepo.save(dashBoard);
        return ResponseEntity.ok(updatedEntry);
    }

    // DELETE - Delete a dashboard entry by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        if (dashBoardRepo.existsById(id)) {
            dashBoardRepo.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
