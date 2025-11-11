package ca.sheridancollege.odedaaja.web.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class UserController {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    // ---------------- Get all users or filter by role ----------------
    @GetMapping
    public List<Users> getAllUsers(@RequestParam(required = false) String role) {
        if (role != null) {
            try {
                Users.Role parsedRole = Users.Role.valueOf(role);
                return userRepo.findByRole(parsedRole);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + role);
            }
        }
        return userRepo.findAll();
    }

    // ---------------- Get user by ID ----------------
    @GetMapping("/{id}")
    public ResponseEntity<Users> getUserById(@PathVariable Long id) {
        return userRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // ---------------- Create new user (signup) ----------------
    @PostMapping
    public ResponseEntity<Users> createUser(@RequestBody Users user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // Encode password before saving
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Assign default profile picture if not provided
        if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()) {
            user.setProfilePicture("https://ui-avatars.com/api/?name=" + user.getUsername().replaceAll(" ", "+"));
        }

        Users savedUser = userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // ---------------- Update user ----------------
    @PutMapping("/{id}")
    public ResponseEntity<Users> updateUser(@PathVariable Long id, @RequestBody Users updatedUser) {
        return userRepo.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername() != null ? updatedUser.getUsername() : user.getUsername());
            user.setEmail(updatedUser.getEmail() != null ? updatedUser.getEmail() : user.getEmail());
            user.setBirthdate(updatedUser.getBirthdate() != null ? updatedUser.getBirthdate() : user.getBirthdate());
            user.setGender(updatedUser.getGender() != null ? updatedUser.getGender() : user.getGender());
            user.setRole(updatedUser.getRole() != null ? updatedUser.getRole() : user.getRole());
            user.setAddress(updatedUser.getAddress() != null ? updatedUser.getAddress() : user.getAddress());
            user.setCity(updatedUser.getCity() != null ? updatedUser.getCity() : user.getCity());
            user.setCountry(updatedUser.getCountry() != null ? updatedUser.getCountry() : user.getCountry());
            user.setBio(updatedUser.getBio() != null ? updatedUser.getBio() : user.getBio());
            user.setStatus(updatedUser.getStatus() != null ? updatedUser.getStatus() : user.getStatus());
            user.setSocialLinks(updatedUser.getSocialLinks() != null ? updatedUser.getSocialLinks() : user.getSocialLinks());
            user.setProfilePicture(updatedUser.getProfilePicture() != null ? updatedUser.getProfilePicture() : user.getProfilePicture());

            // Encode password if provided
            if (updatedUser.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            Users savedUser = userRepo.save(user);
            return ResponseEntity.ok(savedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ---------------- Delete user ----------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userRepo.findById(id).map(user -> {
            userRepo.delete(user);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }

    // ---------------- Get current logged-in user ----------------
    @GetMapping("/me")
    public ResponseEntity<Users> getCurrentUser(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("username");
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return userRepo.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ---------------- Get user by username ----------------
    @GetMapping("/by-username/{username}")
    public ResponseEntity<Users> getUserByUsername(@PathVariable String username) {
        return userRepo.findByUsername(username.toLowerCase())   // force lowercase
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
