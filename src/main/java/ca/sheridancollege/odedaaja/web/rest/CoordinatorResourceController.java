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
@RequestMapping("/api/coordinator/resources")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequiredArgsConstructor
public class CoordinatorResourceController {

    private final ResourceRepository resourceRepo;
    private final ResourceRentalRepository rentalRepo;
    private final DepartmentRepository departmentRepo;
    private final UserRepo userRepo;

    // Get all resources for coordinator management
    @GetMapping
    public List<Resource> getAllResources() {
        return resourceRepo.findAll();
    }

    // Create new resource
    @PostMapping
    public ResponseEntity<Map<String, Object>> createResource(@RequestBody Resource resource) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            resource.setAvailable(true);
            resource.setActive(true);
            Resource savedResource = resourceRepo.save(resource);
            
            response.put("success", true);
            response.put("message", "Resource created successfully");
            response.put("resource", savedResource);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error creating resource: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Update resource
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateResource(@PathVariable Long id, @RequestBody Resource resource) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Resource existingResource = resourceRepo.findById(id).orElse(null);
            if (existingResource == null) {
                response.put("success", false);
                response.put("message", "Resource not found");
                return ResponseEntity.notFound().build();
            }
            
            // Update fields
            existingResource.setName(resource.getName());
            existingResource.setDescription(resource.getDescription());
            existingResource.setCategory(resource.getCategory());
            existingResource.setLocation(resource.getLocation());
            existingResource.setCondition(resource.getCondition());
            existingResource.setSpecifications(resource.getSpecifications());
            existingResource.setRentalPricePerDay(resource.getRentalPricePerDay());
            existingResource.setDepositAmount(resource.getDepositAmount());
            existingResource.setAvailable(resource.isAvailable());
            existingResource.setActive(resource.isActive());
            existingResource.setImageUrl(resource.getImageUrl());
            existingResource.setDepartment(resource.getDepartment());
            
            Resource savedResource = resourceRepo.save(existingResource);
            
            response.put("success", true);
            response.put("message", "Resource updated successfully");
            response.put("resource", savedResource);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating resource: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Delete resource
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteResource(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Resource resource = resourceRepo.findById(id).orElse(null);
            if (resource == null) {
                response.put("success", false);
                response.put("message", "Resource not found");
                return ResponseEntity.notFound().build();
            }
            
            // Check if resource has active rentals
            List<ResourceRental> activeRentals = rentalRepo.findByResourceIdOrderByRequestedAtDesc(id)
                    .stream()
                    .filter(rental -> rental.getStatus() == ResourceRental.RentalStatus.PICKED_UP)
                    .toList();
            
            if (!activeRentals.isEmpty()) {
                response.put("success", false);
                response.put("message", "Cannot delete resource with active rentals");
                return ResponseEntity.badRequest().body(response);
            }
            
            resource.setActive(false);
            resourceRepo.save(resource);
            
            response.put("success", true);
            response.put("message", "Resource deactivated successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting resource: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get pending rental requests
    @GetMapping("/rentals/pending")
    public List<ResourceRental> getPendingRentals() {
        return rentalRepo.findPendingRentals();
    }

    // Get all rentals for coordinator
    @GetMapping("/rentals")
    public List<ResourceRental> getAllRentals() {
        return rentalRepo.findAll();
    }

    // Get rentals by department
    @GetMapping("/rentals/department/{departmentId}")
    public List<ResourceRental> getRentalsByDepartment(@PathVariable Long departmentId) {
        return rentalRepo.findByDepartmentId(departmentId);
    }

    // Approve rental request
    @PostMapping("/rentals/{rentalId}/approve")
    public ResponseEntity<Map<String, Object>> approveRental(
            @PathVariable Long rentalId,
            @RequestParam Long coordinatorId
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ResourceRental rental = rentalRepo.findById(rentalId).orElse(null);
            if (rental == null) {
                response.put("success", false);
                response.put("message", "Rental request not found");
                return ResponseEntity.notFound().build();
            }
            
            if (rental.getStatus() != ResourceRental.RentalStatus.PENDING) {
                response.put("success", false);
                response.put("message", "Rental request is not pending");
                return ResponseEntity.badRequest().body(response);
            }
            
            Users coordinator = userRepo.findById(coordinatorId).orElse(null);
            if (coordinator == null) {
                response.put("success", false);
                response.put("message", "Coordinator not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            rental.setStatus(ResourceRental.RentalStatus.APPROVED);
            rental.setApprovedAt(java.time.LocalDateTime.now());
            rental.setApprovedBy(coordinator);
            
            rentalRepo.save(rental);
            
            response.put("success", true);
            response.put("message", "Rental request approved successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error approving rental: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Deny rental request
    @PostMapping("/rentals/{rentalId}/deny")
    public ResponseEntity<Map<String, Object>> denyRental(
            @PathVariable Long rentalId,
            @RequestParam Long coordinatorId,
            @RequestParam(required = false) String reason
    ) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ResourceRental rental = rentalRepo.findById(rentalId).orElse(null);
            if (rental == null) {
                response.put("success", false);
                response.put("message", "Rental request not found");
                return ResponseEntity.notFound().build();
            }
            
            if (rental.getStatus() != ResourceRental.RentalStatus.PENDING) {
                response.put("success", false);
                response.put("message", "Rental request is not pending");
                return ResponseEntity.badRequest().body(response);
            }
            
            Users coordinator = userRepo.findById(coordinatorId).orElse(null);
            if (coordinator == null) {
                response.put("success", false);
                response.put("message", "Coordinator not found");
                return ResponseEntity.badRequest().body(response);
            }
            
            rental.setStatus(ResourceRental.RentalStatus.DENIED);
            rental.setApprovedAt(java.time.LocalDateTime.now());
            rental.setApprovedBy(coordinator);
            if (reason != null && !reason.trim().isEmpty()) {
                rental.setAdminNotes(rental.getAdminNotes() + " | Denial reason: " + reason);
            }
            
            rentalRepo.save(rental);
            
            response.put("success", true);
            response.put("message", "Rental request denied successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error denying rental: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Mark resource as picked up
    @PostMapping("/rentals/{rentalId}/pickup")
    public ResponseEntity<Map<String, Object>> markPickedUp(@PathVariable Long rentalId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ResourceRental rental = rentalRepo.findById(rentalId).orElse(null);
            if (rental == null) {
                response.put("success", false);
                response.put("message", "Rental request not found");
                return ResponseEntity.notFound().build();
            }
            
            if (rental.getStatus() != ResourceRental.RentalStatus.APPROVED) {
                response.put("success", false);
                response.put("message", "Rental request is not approved");
                return ResponseEntity.badRequest().body(response);
            }
            
            rental.setStatus(ResourceRental.RentalStatus.PICKED_UP);
            rentalRepo.save(rental);
            
            response.put("success", true);
            response.put("message", "Resource marked as picked up");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking as picked up: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Mark resource as returned
    @PostMapping("/rentals/{rentalId}/return")
    public ResponseEntity<Map<String, Object>> markReturned(@PathVariable Long rentalId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ResourceRental rental = rentalRepo.findById(rentalId).orElse(null);
            if (rental == null) {
                response.put("success", false);
                response.put("message", "Rental request not found");
                return ResponseEntity.notFound().build();
            }
            
            if (rental.getStatus() != ResourceRental.RentalStatus.PICKED_UP) {
                response.put("success", false);
                response.put("message", "Resource is not picked up");
                return ResponseEntity.badRequest().body(response);
            }
            
            rental.setStatus(ResourceRental.RentalStatus.RETURNED);
            rental.setActualReturnTime(java.time.LocalDateTime.now());
            rentalRepo.save(rental);
            
            response.put("success", true);
            response.put("message", "Resource marked as returned");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error marking as returned: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get resource statistics
    @GetMapping("/stats")
    public Map<String, Object> getResourceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalResources = resourceRepo.count();
        long availableResources = resourceRepo.findByAvailableTrueAndActiveTrue().size();
        long pendingRentals = rentalRepo.findPendingRentals().size();
        long activeRentals = rentalRepo.findActiveRentals().size();
        long overdueRentals = rentalRepo.findOverdueRentals().size();
        
        stats.put("totalResources", totalResources);
        stats.put("availableResources", availableResources);
        stats.put("pendingRentals", pendingRentals);
        stats.put("activeRentals", activeRentals);
        stats.put("overdueRentals", overdueRentals);
        
        return stats;
    }
}
