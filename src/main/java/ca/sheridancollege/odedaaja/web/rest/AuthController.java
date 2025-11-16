package ca.sheridancollege.odedaaja.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173","https://instimanage.netlify.app" }, allowCredentials = "true")
public class AuthController {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------ Register ------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Users user) {
        if (userRepo.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Username already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Users saved = userRepo.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ------------------ Login ------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users loginRequest, HttpServletRequest request) {
        return userRepo.findByUsername(loginRequest.getUsername())
                .filter(u -> passwordEncoder.matches(loginRequest.getPassword(), u.getPassword()))
                .map(u -> {
                    // ✅ Store username in session
                    request.getSession().setAttribute("username", u.getUsername());

                    // ✅ Build response JSON
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", "dummy-token-" + u.getId()); // replace with JWT later
                    response.put("role", u.getRole().name().toUpperCase());
                    response.put("userId", u.getId());
                    response.put("name", u.getUsername());

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid username or password")));
    }

    // ------------------ Check Auth ------------------
    @GetMapping("/check")
    public ResponseEntity<String> checkAuth() {
        return ResponseEntity.ok("OK");
    }

    // ------------------ Logout ------------------
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        try {
            request.getSession().invalidate();
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed");
        }
    }
}
