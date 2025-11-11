package ca.sheridancollege.odedaaja.Locker.web.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.ByteArrayOutputStream;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ca.sheridancollege.odedaaja.Locker.domain.Booking;
import ca.sheridancollege.odedaaja.Locker.domain.BookingStatus;
import ca.sheridancollege.odedaaja.Locker.domain.ContractTemplate;
import ca.sheridancollege.odedaaja.Locker.domain.Locker;
import ca.sheridancollege.odedaaja.Locker.domain.LockerLocation;
import ca.sheridancollege.odedaaja.Locker.domain.PricingRule;
import ca.sheridancollege.odedaaja.Locker.domain.RoomAssignment;
import ca.sheridancollege.odedaaja.Locker.repo.BookingRepository;
import ca.sheridancollege.odedaaja.Locker.repo.ContractTemplateRepository;
import ca.sheridancollege.odedaaja.Locker.repo.LockerLocationRepository;
import ca.sheridancollege.odedaaja.Locker.repo.LockerRepository;
import ca.sheridancollege.odedaaja.Locker.repo.PricingRuleRepository;
import ca.sheridancollege.odedaaja.Locker.repo.RoomAssignmentRepository;
import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import ca.sheridancollege.odedaaja.repo.CourseRepository;
import ca.sheridancollege.odedaaja.domain.Course;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/locker")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AdminController {

    private final LockerRepository lockerRepo;
    private final LockerLocationRepository locationRepo;
    private final PricingRuleRepository pricingRepo;
    private final ContractTemplateRepository templateRepo;
    private final BookingRepository bookingRepo;
    private final RoomAssignmentRepository roomAssignRepo;
    private final UserRepo userRepo;
    private final CourseRepository courseRepo;

    // ✅ Create location
    @PostMapping("/locations")
    public LockerLocation createLocation(@RequestBody LockerLocation l) {
        return locationRepo.save(l);
    }

    // ✅ Create single locker
 // ✅ Existing: create single locker
    @PostMapping("/lockers")
    public Locker createLocker(@RequestBody Locker l) {
        if (l.getBasePrice() == null) {
            l.setBasePrice(new BigDecimal("0.00")); // default if not provided
        }
        return lockerRepo.save(l);
    }


    // ✅ Toggle locker status
    @PatchMapping("/lockers/{id}/status")
    public Locker toggle(@PathVariable Long id,
                         @RequestParam boolean active,
                         @RequestParam boolean online) {
        var l = lockerRepo.findById(id).orElseThrow();
        l.setActive(active);
        l.setOnline(online);
        return lockerRepo.save(l);
    }

    // ✅ Save pricing rule
    @PostMapping("/pricing")
    public PricingRule savePricing(@RequestBody PricingRule r) {
        return pricingRepo.save(r);
    }

    // ✅ Save contract template
    @PostMapping("/template")
    public ContractTemplate saveTemplate(@RequestBody ContractTemplate t) {
        return templateRepo.save(t);
    }

    // ✅ Simple report summary
    @GetMapping("/report/summary")
    public Map<String, Object> report() {
        var allLockerBookings = bookingRepo.findAll();
        var paidLockerBookings = allLockerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID)
                .collect(Collectors.toList());
        
        var revenue = paidLockerBookings.stream()
                .map(b -> b.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return Map.of("count", paidLockerBookings.size(), "revenue", revenue);
    }

    // ✅ Extended stats for admin reports
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        // Get locker bookings (paid)
        var lockerBookings = bookingRepo.findAll();
        var paidLockerBookings = lockerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID)
                .collect(Collectors.toList());
        
        var totalRevenue = paidLockerBookings.stream()
                .map(b -> b.getTotalAmount())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        var activeLockerBookings = lockerBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PAID)
                .count();

        // Note: Room bookings are free, so only locker bookings contribute to revenue
        return Map.of(
            "totalBookings", paidLockerBookings.size(), // Only count paid locker bookings for revenue calculation
            "totalRevenue", totalRevenue,
            "activeBookings", activeLockerBookings
        );
    }

    // ✅ Download detailed locker sales report
    @GetMapping("/report/download")
    public ResponseEntity<String> downloadReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long lockerId) {
        
        var allBookings = bookingRepo.findAll();
        
        // Filter by date range if provided
        if (startDate != null && endDate != null) {
            allBookings = allBookings.stream()
                    .filter(b -> {
                        String bookingDate = b.getStartDate().toString();
                        return bookingDate.compareTo(startDate) >= 0 && bookingDate.compareTo(endDate) <= 0;
                    })
                    .collect(Collectors.toList());
        }
        
        // Filter by locker ID if provided
        if (lockerId != null) {
            allBookings = allBookings.stream()
                    .filter(b -> b.getLocker().getId().equals(lockerId))
                    .collect(Collectors.toList());
        }
        
        // Generate CSV content
        StringBuilder csv = new StringBuilder();
        csv.append("Booking ID,Locker Number,Building,Floor,Customer Name,Customer Email,Start Date,End Date,Duration,Base Price,Tax,Processing Fee,Total Amount,Status,Booking Date\n");
        
        for (var booking : allBookings) {
            csv.append(booking.getId() != null ? booking.getId() : "").append(",");
            csv.append(booking.getLocker() != null && booking.getLocker().getLockerNumber() != null ? booking.getLocker().getLockerNumber() : "").append(",");
            csv.append(booking.getLocker() != null && booking.getLocker().getLocation() != null && booking.getLocker().getLocation().getBuilding() != null ? booking.getLocker().getLocation().getBuilding() : "").append(",");
            csv.append(booking.getLocker() != null && booking.getLocker().getLocation() != null && booking.getLocker().getLocation().getFloor() != null ? booking.getLocker().getLocation().getFloor() : "").append(",");
            csv.append(booking.getUserName() != null ? booking.getUserName() : "").append(",");
            csv.append(booking.getUserEmail() != null ? booking.getUserEmail() : "").append(",");
            csv.append(booking.getStartDate() != null ? booking.getStartDate() : "").append(",");
            csv.append(booking.getEndDate() != null ? booking.getEndDate() : "").append(",");
            csv.append(booking.getDuration() != null ? booking.getDuration().getMonths() + " months" : "N/A").append(",");
            csv.append(booking.getPriceSubTotal() != null ? booking.getPriceSubTotal() : "0.00").append(",");
            csv.append(booking.getTaxAmount() != null ? booking.getTaxAmount() : "0.00").append(",");
            csv.append(booking.getFeesAmount() != null ? booking.getFeesAmount() : "0.00").append(",");
            csv.append(booking.getTotalAmount() != null ? booking.getTotalAmount() : "0.00").append(",");
            csv.append(booking.getStatus() != null ? booking.getStatus() : "UNKNOWN").append(",");
            csv.append(booking.getCreatedAt() != null ? booking.getCreatedAt() : "").append("\n");
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "locker_sales_report.csv");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.toString());
    }

    // ✅ Download detailed locker sales report as formatted text (for PDF conversion)
    @GetMapping("/report/download/pdf")
    public ResponseEntity<String> downloadReportPdf(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long lockerId) {
        
        try {
            var allBookings = bookingRepo.findAll();
            
            // Filter by date range if provided
            if (startDate != null && endDate != null) {
                allBookings = allBookings.stream()
                        .filter(b -> {
                            String bookingDate = b.getStartDate().toString();
                            return bookingDate.compareTo(startDate) >= 0 && bookingDate.compareTo(endDate) <= 0;
                        })
                        .collect(Collectors.toList());
            }
            
            // Filter by locker ID if provided
            if (lockerId != null) {
                allBookings = allBookings.stream()
                        .filter(b -> b.getLocker().getId().equals(lockerId))
                        .collect(Collectors.toList());
            }
            
            // Generate formatted text report
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(80)).append("\n");
            report.append("                    LOCKER SALES REPORT                    \n");
            report.append("=".repeat(80)).append("\n\n");
            
            report.append("Generated on: ").append(java.time.LocalDateTime.now().toString()).append("\n");
            if (startDate != null && endDate != null) {
                report.append("Date Range: ").append(startDate).append(" to ").append(endDate).append("\n");
            }
            if (lockerId != null) {
                report.append("Locker ID: ").append(lockerId).append("\n");
            }
            report.append("Total Records: ").append(allBookings.size()).append("\n\n");
            
            if (allBookings.isEmpty()) {
                report.append("No booking data found for the specified criteria.\n");
            } else {
                report.append("-".repeat(80)).append("\n");
                report.append("BOOKING DETAILS\n");
                report.append("-".repeat(80)).append("\n\n");
                
                for (var booking : allBookings) {
                    report.append("Booking ID: ").append(booking.getId() != null ? booking.getId().toString() : "N/A").append("\n");
                    report.append("Locker: ").append(booking.getLocker() != null && booking.getLocker().getLockerNumber() != null ? booking.getLocker().getLockerNumber() : "N/A").append("\n");
                    report.append("Location: ").append(booking.getLocker() != null && booking.getLocker().getLocation() != null ? 
                        booking.getLocker().getLocation().getBuilding() + " - " + booking.getLocker().getLocation().getFloor() : "N/A").append("\n");
                    report.append("Customer: ").append(booking.getUserName() != null ? booking.getUserName() : "N/A")
                          .append(" (").append(booking.getUserEmail() != null ? booking.getUserEmail() : "N/A").append(")\n");
                    report.append("Period: ").append(booking.getStartDate() != null ? booking.getStartDate().toString() : "N/A")
                          .append(" to ").append(booking.getEndDate() != null ? booking.getEndDate().toString() : "N/A").append("\n");
                    report.append("Duration: ").append(booking.getDuration() != null ? booking.getDuration().getMonths() + " months" : "N/A").append("\n");
                    report.append("Amount: $").append(booking.getTotalAmount() != null ? booking.getTotalAmount().toString() : "0.00").append("\n");
                    report.append("Status: ").append(booking.getStatus() != null ? booking.getStatus().toString() : "UNKNOWN").append("\n");
                    report.append("Created: ").append(booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : "N/A").append("\n");
                    report.append("-".repeat(40)).append("\n\n");
                }
            }
            
            report.append("=".repeat(80)).append("\n");
            report.append("End of Report\n");
            report.append("=".repeat(80)).append("\n");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "locker_sales_report.txt");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(report.toString());
                    
        } catch (Exception e) {
            e.printStackTrace();
            // Return error message
            String errorMessage = "PDF generation is temporarily unavailable.\n\nPlease use the CSV download option instead.\n\nError: " + e.getMessage();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "error_message.txt");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(errorMessage);
        }
    }

    // ✅ Bulk upload lockers via CSV
    @PostMapping("/upload-lockers")
    public String uploadLockers(@RequestParam("file") MultipartFile file) throws Exception {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            List<Locker> lockers = br.lines()
                    .skip(1) // skip header
                    .map(line -> {
                        String[] parts = line.split(",");
                        String building = parts[0].trim();
                        String floor = parts[1].trim();
                        String lockerNum = parts[2].trim();

                        LockerLocation loc = locationRepo.findAll().stream()
                                .filter(l -> l.getBuilding().equals(building) && l.getFloor().equals(floor))
                                .findFirst()
                                .orElseGet(() -> locationRepo.save(
                                        LockerLocation.builder()
                                                .building(building)
                                                .floor(floor)
                                                .build()));

                        return Locker.builder()
                                .location(loc)
                                .lockerNumber(lockerNum)
                                .active(true)
                                .online(true)
                                .basePrice(new BigDecimal("50.00")) // Default base price
                                .description("Locker " + lockerNum + " in " + building + " - " + floor)
                                .build();
                    })
                    .collect(Collectors.toList());

            lockerRepo.saveAll(lockers);
            return "Uploaded " + lockers.size() + " lockers.";
        }
    }

    // ✅ List all room assignments
    @GetMapping("/assignments")
    public List<RoomAssignment> getAssignments() {
        return roomAssignRepo.findAll();
    }

    // ✅ Assign a room to a professor
    @PostMapping("/assign-room")
    public RoomAssignment assignRoom(@RequestParam Long coordinatorId,
                                     @RequestParam String department,
                                     @RequestParam String roomNumber,
                                     @RequestParam(required = false) Long courseId,
                                     @RequestParam(required = false) String semesterStart,
                                     @RequestParam(required = false) String semesterEnd) {

        Users user = userRepo.findById(coordinatorId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Users.Role.Professor) {
            throw new RuntimeException("Only Professors can be assigned rooms");
        }

        java.time.LocalDate start = null;
        java.time.LocalDate end = null;
        try {
            if (semesterStart != null && !semesterStart.isEmpty()) start = java.time.LocalDate.parse(semesterStart);
            if (semesterEnd != null && !semesterEnd.isEmpty()) end = java.time.LocalDate.parse(semesterEnd);
        } catch (Exception e) {
            throw new RuntimeException("Invalid semester dates");
        }

        // Conflict checks: same room overlapping, or same professor+course overlapping
        List<RoomAssignment> existing = roomAssignRepo.findAll();
        for (RoomAssignment a : existing) {
            boolean roomMatches = a.getRoomNumber() != null && a.getRoomNumber().equalsIgnoreCase(roomNumber);
            boolean profCourseMatches = a.getCoordinatorId() != null && a.getCoordinatorId().equals(coordinatorId)
                    && (courseId != null && a.getCourseId() != null && a.getCourseId().equals(courseId));
            boolean overlaps = false;
            if (start != null && end != null && a.getSemesterStart() != null && a.getSemesterEnd() != null) {
                overlaps = !end.isBefore(a.getSemesterStart()) && !start.isAfter(a.getSemesterEnd());
            } else if (start == null && end == null) {
                overlaps = true; // treat as general assignment
            }
            if ((roomMatches || profCourseMatches) && overlaps) {
                throw new RuntimeException("Assignment conflict for selected room/professor within the semester window");
            }
        }

        String courseName = null;
        if (courseId != null) {
            Course c = courseRepo.findById(courseId).orElse(null);
            if (c != null) courseName = c.getCode() != null ? c.getCode() + " - " + c.getName() : c.getName();
        }

        RoomAssignment assignment = RoomAssignment.builder()
                .coordinatorId(coordinatorId)
                .department(department)
                .roomNumber(roomNumber)
                .courseId(courseId)
                .courseName(courseName)
                .semesterStart(start)
                .semesterEnd(end)
                .build();

        return roomAssignRepo.save(assignment);
    }
    
    @PatchMapping("/assignments/{id}")
    public RoomAssignment updateAssignment(@PathVariable Long id, @RequestBody RoomAssignment updated) {
        RoomAssignment existing = roomAssignRepo.findById(id).orElseThrow();
        if (updated.getCoordinatorId() != null) {
            existing.setCoordinatorId(updated.getCoordinatorId());
        }
        if (updated.getDepartment() != null) {
            existing.setDepartment(updated.getDepartment());
        }
        if (updated.getRoomNumber() != null) {
            existing.setRoomNumber(updated.getRoomNumber());
        }
        return roomAssignRepo.save(existing);
    }

    // ✅ Remove an assignment
    @DeleteMapping("/assignments/{id}")
    public void deleteAssignment(@PathVariable Long id) {
        roomAssignRepo.deleteById(id);
    }


    // ✅ Update locker details
    @PatchMapping("/lockers/{id}")
    public Locker updateLocker(@PathVariable Long id, @RequestBody Locker updated) {
        Locker locker = lockerRepo.findById(id).orElseThrow();

        if (updated.getLockerNumber() != null) {
            locker.setLockerNumber(updated.getLockerNumber());
        }
        if (updated.getLocation() != null) {
            locker.setLocation(updated.getLocation());
        }
        locker.setActive(updated.isActive());
        locker.setOnline(updated.isOnline());

        return lockerRepo.save(locker);
    }

    // ✅ Delete locker
    @DeleteMapping("/lockers/{id}")
    public void deleteLocker(@PathVariable Long id) {
        lockerRepo.deleteById(id);
    }

    // ✅ Get all pending cancellation requests
    @GetMapping("/cancellation-requests")
    public List<Booking> getPendingCancellationRequests() {
        return bookingRepo.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING_CANCELLATION)
                .collect(Collectors.toList());
    }

    // ✅ Approve cancellation request and process refund
    @PostMapping("/cancellation-requests/{bookingId}/approve")
    public Map<String, Object> approveCancellation(@PathVariable Long bookingId, 
                                                   @RequestParam String processedBy) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING_CANCELLATION) {
            throw new RuntimeException("Booking is not in pending cancellation status");
        }

        // Calculate refund amount (full amount for now)
        booking.setRefundAmount(booking.getTotalAmount());
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationProcessedDate(java.time.OffsetDateTime.now());
        booking.setCancellationProcessedBy(processedBy);
        
        // Make locker available again
        Locker locker = booking.getLocker();
        locker.setActive(true);
        locker.setOnline(true);
        lockerRepo.save(locker);

        bookingRepo.save(booking);

        return Map.of(
            "success", true,
            "message", "Cancellation approved and refund processed",
            "refundAmount", booking.getRefundAmount(),
            "bookingId", bookingId
        );
    }

    // ✅ Decline cancellation request
    @PostMapping("/cancellation-requests/{bookingId}/decline")
    public Map<String, Object> declineCancellation(@PathVariable Long bookingId,
                                                   @RequestParam String processedBy,
                                                   @RequestParam(required = false) String reason) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING_CANCELLATION) {
            throw new RuntimeException("Booking is not in pending cancellation status");
        }

        booking.setStatus(BookingStatus.PAID); // Return to paid status
        booking.setCancellationProcessedDate(java.time.OffsetDateTime.now());
        booking.setCancellationProcessedBy(processedBy);
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setCancellationReason(booking.getCancellationReason() + " | Admin Response: " + reason);
        }

        bookingRepo.save(booking);

        return Map.of(
            "success", true,
            "message", "Cancellation request declined",
            "bookingId", bookingId
        );
    }
}
