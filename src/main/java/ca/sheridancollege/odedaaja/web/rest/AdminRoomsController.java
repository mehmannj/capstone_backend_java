package ca.sheridancollege.odedaaja.web.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.springframework.web.bind.annotation.*;

import ca.sheridancollege.odedaaja.domain.*;
import ca.sheridancollege.odedaaja.repo.*;
import lombok.RequiredArgsConstructor;
import ca.sheridancollege.odedaaja.Locker.repo.RoomAssignmentRepository;

@RestController
@RequestMapping("/api/admin/rooms")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"}, allowCredentials = "true")
@RequiredArgsConstructor
public class AdminRoomsController {

    private final DepartmentRepository departmentRepo;
    private final ProgramRepository programRepo;
    private final CourseRepository courseRepo;
    private final RoomRepository roomRepo;
    private final RoomAssignmentRepository assignmentRepo;
    private final UserRepo userRepo;

    // Departments
    @PostMapping("/departments")
    public Department createDepartment(@RequestBody Department d) { return departmentRepo.save(d); }

    @GetMapping("/departments")
    public List<Department> listDepartments() { return departmentRepo.findAll(); }

    // Programs
    @PostMapping("/programs")
    public Program createProgram(@RequestBody Program p) { return programRepo.save(p); }

    @GetMapping("/programs")
    public List<Program> listPrograms() { return programRepo.findAll(); }

    // Courses
    @PostMapping("/courses")
    public Course createCourse(@RequestBody Course c) { return courseRepo.save(c); }

    @GetMapping("/courses")
    public List<Course> listCourses() { return courseRepo.findAll(); }

    // Rooms
    @PostMapping
    public Room createRoom(@RequestBody Room r) { return roomRepo.save(r); }

    @GetMapping
    public List<Room> listRooms() { return roomRepo.findAll(); }

    // Consolidated: list professor assignments grouped by department
    @GetMapping("/assignments")
    public List<Map<String, Object>> listAssignments() {
        return assignmentRepo.findAll().stream().map(a -> {
            ca.sheridancollege.odedaaja.domain.Users user = userRepo.findById(a.getCoordinatorId()).orElse(null);
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("professorId", a.getCoordinatorId());
            m.put("professor", user != null ? user.getUsername() : "unknown");
            m.put("department", a.getDepartment());
            m.put("roomNumber", a.getRoomNumber());
            return m;
        }).collect(Collectors.toList());
    }

    // Rooms availability: assigned vs unassigned
    @GetMapping("/availability")
    public Map<String, Object> availability() {
        List<ca.sheridancollege.odedaaja.Locker.domain.RoomAssignment> assignments = assignmentRepo.findAll();
        java.util.Set<String> assignedRoomNumbers = assignments.stream().map(a -> a.getRoomNumber()).collect(java.util.stream.Collectors.toSet());
        List<Room> allRooms = roomRepo.findAll();

        List<Map<String, Object>> assigned = assignments.stream().map(a -> {
            Map<String, Object> m = new HashMap<>();
            m.put("roomNumber", a.getRoomNumber());
            m.put("department", a.getDepartment());
            m.put("professorId", a.getCoordinatorId());
            String professorName = userRepo.findById(a.getCoordinatorId()).map(usr -> usr.getUsername()).orElse("unknown");
            m.put("professor", professorName);
            return m;
        }).collect(Collectors.toList());

        List<Room> unassigned = allRooms.stream()
            .filter(r -> !assignedRoomNumbers.contains(r.getNumber()))
            .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("assigned", assigned);
        result.put("unassigned", unassigned);
        return result;
    }

    // Filter rooms by department/program/course/resources (all params optional)
    @GetMapping("/search")
    public List<Room> searchRooms(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Long programId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String resources,
            @RequestParam(required = false) Boolean quiet,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        List<Room> all = roomRepo.findAll();
        java.time.LocalDate start;
        java.time.LocalDate end;
        
        if (startDate != null && !startDate.isEmpty()) {
            try {
                start = java.time.LocalDate.parse(startDate);
            } catch (Exception ignored) {
                start = null;
            }
        } else {
            start = null;
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            try {
                end = java.time.LocalDate.parse(endDate);
            } catch (Exception ignored) {
                end = null;
            }
        } else {
            end = null;
        }

        List<ca.sheridancollege.odedaaja.Locker.domain.RoomAssignment> assignments = assignmentRepo.findAll();

        // Create effectively final variables for use in lambda
        final java.time.LocalDate finalStart = start;
        final java.time.LocalDate finalEnd = end;
        final List<ca.sheridancollege.odedaaja.Locker.domain.RoomAssignment> finalAssignments = assignments;

        return all.stream().filter(r -> {
            boolean ok = true;
            if (department != null && !department.isEmpty()) {
                String deptName = r.getDepartment() != null ? r.getDepartment().getName() : null;
                ok = ok && department.equalsIgnoreCase(deptName);
            }
            if (programId != null) {
                ok = ok && r.getProgram() != null && programId.equals(r.getProgram().getId());
            }
            if (courseId != null) {
                ok = ok && r.getCourse() != null && courseId.equals(r.getCourse().getId());
            }
            if (resources != null && !resources.isEmpty()) {
                String rr = r.getResources() != null ? r.getResources().toLowerCase() : "";
                ok = ok && rr.contains(resources.toLowerCase());
            }
            if (quiet != null) {
                ok = ok && Boolean.valueOf(r.isQuiet()).equals(quiet);
            }
            if (type != null && !type.isEmpty()) {
                String rt = r.getType() != null ? r.getType().name() : "";
                ok = ok && rt.equalsIgnoreCase(type);
            }
            // Exclude rooms already assigned overlapping the requested window
            if (ok && finalStart != null && finalEnd != null) {
                boolean taken = finalAssignments.stream().anyMatch(a -> {
                    if (a.getRoomNumber() == null) return false;
                    if (!a.getRoomNumber().equalsIgnoreCase(r.getNumber())) return false;
                    if (a.getSemesterStart() == null || a.getSemesterEnd() == null) return false;
                    return !finalEnd.isBefore(a.getSemesterStart()) && !finalStart.isAfter(a.getSemesterEnd());
                });
                ok = ok && !taken;
            }
            return ok;
        }).collect(Collectors.toList());
    }
}


