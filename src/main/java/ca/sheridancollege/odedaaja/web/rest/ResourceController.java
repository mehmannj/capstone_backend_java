package ca.sheridancollege.odedaaja.web.rest;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import ca.sheridancollege.odedaaja.domain.*;
import ca.sheridancollege.odedaaja.repo.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://instimanage.netlify.app" }, allowCredentials = "true")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceRepository resourceRepo;
    private final ResourceRentalRepository rentalRepo;
    private final DepartmentRepository departmentRepo;
    private final UserRepo userRepo;

    // Get all available resources for students
    @GetMapping
    public List<Resource> getAvailableResources(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search
    ) {
        if (search != null && !search.isEmpty()) {
            return resourceRepo.searchAvailableResources(search);
        }
        
        if (category != null && departmentId != null) {
            return resourceRepo.findByCategoryAndAvailableTrueAndActiveTrue(category);
        } else if (category != null) {
            return resourceRepo.findByCategoryAndAvailableTrueAndActiveTrue(category);
        } else if (departmentId != null) {
            return resourceRepo.findByDepartmentIdAndAvailableTrueAndActiveTrue(departmentId);
        }
        
        return resourceRepo.findByAvailableTrueAndActiveTrue();
    }

    // Get resource by ID
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResource(@PathVariable Long id) {
        return resourceRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get resource categories
    @GetMapping("/categories")
    public List<String> getCategories() {
        return resourceRepo.findByAvailableTrueAndActiveTrue().stream()
                .map(Resource::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    // Check resource availability for specific time period
    @PostMapping("/{id}/check-availability")
    public Map<String, Object> checkAvailability(
            @PathVariable Long id,
            @RequestBody Map<String, String> request
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(request.get("startTime"));
            java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(request.get("endTime"));
            
            int conflicts = rentalRepo.countConflictingRentals(id, startTime, endTime);
            boolean available = conflicts == 0;
            
            response.put("available", available);
            response.put("conflicts", conflicts);
            
            if (!available) {
                response.put("message", "Resource is not available for the selected time period");
            }
            
        } catch (Exception e) {
            response.put("available", false);
            response.put("error", "Invalid date format");
        }
        
        return response;
    }

    // Student rent resource
    @PostMapping("/{id}/rent")
    public ResponseEntity<Map<String, Object>> rentResource(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Resource resource = resourceRepo.findById(id).orElse(null);
            if (resource == null) {
                response.put("success", false);
                response.put("message", "Resource not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!resource.isAvailable()) {
                response.put("success", false);
                response.put("message", "Resource is not available");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Get student info
            Long studentId = Long.valueOf(request.get("studentId").toString());
            Users student = userRepo.findById(studentId).orElse(null);
            if (student == null || student.getRole() != Users.Role.Student) {
                response.put("success", false);
                response.put("message", "Student not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Parse dates
            java.time.LocalDateTime pickupTime = java.time.LocalDateTime.parse(request.get("pickupTime").toString());
            java.time.LocalDateTime returnTime = java.time.LocalDateTime.parse(request.get("returnTime").toString());
            int rentalDays = Integer.parseInt(request.get("rentalDays").toString());
            
            // Check for conflicts
            int conflicts = rentalRepo.countConflictingRentals(id, pickupTime, returnTime);
            if (conflicts > 0) {
                response.put("success", false);
                response.put("message", "Resource is not available for the selected time period");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Calculate amounts
            java.math.BigDecimal totalAmount = resource.getRentalPricePerDay().multiply(java.math.BigDecimal.valueOf(rentalDays));
            
            // Create rental request
            ResourceRental rental = ResourceRental.builder()
                    .resource(resource)
                    .student(student)
                    .studentEmail(request.get("studentEmail").toString())
                    .studentPhone(request.get("studentPhone").toString())
                    .studentName(request.get("studentName").toString())
                    .requestedAt(java.time.LocalDateTime.now())
                    .pickupTime(pickupTime)
                    .returnTime(returnTime)
                    .rentalDays(rentalDays)
                    .totalAmount(totalAmount)
                    .depositAmount(resource.getDepositAmount())
                    .pickupLocation(resource.getLocation())
                    .returnLocation(resource.getLocation())
                    .notes(request.get("notes").toString())
                    .status(ResourceRental.RentalStatus.PENDING)
                    .build();
            
            rentalRepo.save(rental);
            
            response.put("success", true);
            response.put("message", "Resource rental request submitted successfully");
            response.put("rentalId", rental.getId());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating rental request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get student's rental history
    @GetMapping("/student/{studentId}/rentals")
    public List<ResourceRental> getStudentRentals(@PathVariable Long studentId) {
        return rentalRepo.findByStudentIdOrderByRequestedAtDesc(studentId);
    }

    // Get rental by ID
    @GetMapping("/rentals/{rentalId}")
    public ResponseEntity<ResourceRental> getRental(@PathVariable Long rentalId) {
        return rentalRepo.findById(rentalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
