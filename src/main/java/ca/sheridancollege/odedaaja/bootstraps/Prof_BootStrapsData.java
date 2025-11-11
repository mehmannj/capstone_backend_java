package ca.sheridancollege.odedaaja.bootstraps;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import ca.sheridancollege.odedaaja.domain.DashBoard;
import ca.sheridancollege.odedaaja.domain.Department;
import ca.sheridancollege.odedaaja.domain.Program;
import ca.sheridancollege.odedaaja.domain.Course;
import ca.sheridancollege.odedaaja.domain.Room;
import ca.sheridancollege.odedaaja.domain.Student;
import ca.sheridancollege.odedaaja.domain.Notification;
import ca.sheridancollege.odedaaja.domain.RoomPost;
import ca.sheridancollege.odedaaja.domain.RoomRequest;
import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.domain.Resource;
import ca.sheridancollege.odedaaja.domain.ResourceRental;
import ca.sheridancollege.odedaaja.repo.DashBoardRepo;
import ca.sheridancollege.odedaaja.repo.DepartmentRepository;
import ca.sheridancollege.odedaaja.repo.ProgramRepository;
import ca.sheridancollege.odedaaja.repo.CourseRepository;
import ca.sheridancollege.odedaaja.repo.RoomRepository;
import ca.sheridancollege.odedaaja.repo.StudentRepository;
import ca.sheridancollege.odedaaja.Locker.repo.RoomAssignmentRepository;
import ca.sheridancollege.odedaaja.Locker.domain.RoomAssignment;
import ca.sheridancollege.odedaaja.repo.NotificationRepository;
import ca.sheridancollege.odedaaja.repo.RoomPostRepository;
import ca.sheridancollege.odedaaja.repo.RoomRequestRepository;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import ca.sheridancollege.odedaaja.repo.ResourceRepository;
import ca.sheridancollege.odedaaja.repo.ResourceRentalRepository;

@Component
@DependsOn("entityManagerFactory")
public class Prof_BootStrapsData implements CommandLineRunner {

    private final UserRepo userRepo;
    private final RoomPostRepository roomPostRepository;
    private final RoomRequestRepository roomRequestRepository;
    private final DashBoardRepo dashBoardRepo;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final RoomRepository roomRepository;
    private final StudentRepository studentRepository;
    private final RoomAssignmentRepository roomAssignmentRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceRentalRepository resourceRentalRepository;

    public Prof_BootStrapsData(UserRepo userRepo, 
                               RoomPostRepository roomPostRepository, 
                               RoomRequestRepository roomRequestRepository,
                               DashBoardRepo dashBoardRepo,
                               NotificationRepository notificationRepository,
                               PasswordEncoder passwordEncoder,
                               DepartmentRepository departmentRepository,
                               ProgramRepository programRepository,
                               CourseRepository courseRepository,
                               RoomRepository roomRepository,
                               StudentRepository studentRepository,
                               RoomAssignmentRepository roomAssignmentRepository,
                               ResourceRepository resourceRepository,
                               ResourceRentalRepository resourceRentalRepository) {
        this.userRepo = userRepo;
        this.roomPostRepository = roomPostRepository;
        this.roomRequestRepository = roomRequestRepository;
        this.dashBoardRepo = dashBoardRepo;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.roomRepository = roomRepository;
        this.studentRepository = studentRepository;
        this.roomAssignmentRepository = roomAssignmentRepository;
        this.resourceRepository = resourceRepository;
        this.resourceRentalRepository = resourceRentalRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        loadData();
    }

    private void loadData() {
        System.out.println("🚀 Initializing comprehensive bootstrap data...");

        // Seed academic structure
        seedDepartmentsProgramsCoursesRooms();

        // Always create users (check if they exist first)
        List<Users> users = createUsers();
        List<Users> savedUsers = new ArrayList<>();
        
        for (Users user : users) {
            if (!userRepo.findByUsername(user.getUsername()).isPresent()) {
                Users savedUser = userRepo.save(user);
                savedUsers.add(savedUser);
                System.out.println("✅ Created user: " + user.getUsername());
            } else {
                // Get existing user from database
                Users existingUser = userRepo.findByUsername(user.getUsername()).orElse(null);
                if (existingUser != null) {
                    savedUsers.add(existingUser);
                    System.out.println("ℹ️ User already exists: " + user.getUsername());
                }
            }
        }

        // Create room posts for each professor using saved users
        if (roomPostRepository.count() == 0) {
            List<RoomPost> roomPosts = createRoomPosts(savedUsers);
            try {
                roomPostRepository.saveAll(roomPosts);
                System.out.println("✅ Created " + roomPosts.size() + " room posts");
            } catch (Exception e) {
                System.out.println("⚠️ Error creating room posts: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ Room posts already exist, skipping creation...");
        }

        // Create room requests for each professor using saved users
        if (roomRequestRepository.count() == 0) {
            List<RoomRequest> roomRequests = createRoomRequests(savedUsers);
            try {
                roomRequestRepository.saveAll(roomRequests);
                System.out.println("✅ Created " + roomRequests.size() + " room requests");
            } catch (Exception e) {
                System.out.println("⚠️ Error creating room requests: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ Room requests already exist, skipping creation...");
        }

        // Create professor room assignments (seed)
        seedRoomAssignments(savedUsers);

        // Create student entities for student users
        seedStudents(savedUsers);

        // Clear existing schedule entries and create clean, non-conflicting ones
        System.out.println("🧹 Clearing existing schedule entries to remove conflicts...");
        dashBoardRepo.deleteAll();
        
        List<DashBoard> dashboardEntries = createScheduleEntries(savedUsers);
        try {
            dashBoardRepo.saveAll(dashboardEntries);
            System.out.println("✅ Created " + dashboardEntries.size() + " clean schedule entries (conflicts removed)");
        } catch (Exception e) {
            System.out.println("⚠️ Error creating schedule entries: " + e.getMessage());
        }

        // Create notifications using saved users
        if (notificationRepository.count() == 0) {
            List<Notification> notifications = createNotifications(savedUsers);
            try {
                notificationRepository.saveAll(notifications);
                System.out.println("✅ Created " + notifications.size() + " notifications");
            } catch (Exception e) {
                System.out.println("⚠️ Error creating notifications: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ Notifications already exist, skipping creation...");
        }

        // Seed resources (with error handling)
        try {
            seedResources();
        } catch (Exception e) {
            System.out.println("⚠️ Error seeding resources: " + e.getMessage());
            System.out.println("ℹ️ This is expected on first run - resources will be created on next startup");
        }

        // Seed resource rentals after resources and users exist
        try {
            seedResourceRentals(savedUsers);
        } catch (Exception e) {
            System.out.println("⚠️ Error seeding resource rentals: " + e.getMessage());
        }

        System.out.println("✅ Bootstrap data initialization completed!");
        System.out.println("📊 Summary: " + savedUsers.size() + " users processed, " + 
                          roomPostRepository.count() + " room posts, " + 
                          roomRequestRepository.count() + " room requests, " + 
                          dashBoardRepo.count() + " schedule entries, " + 
                          notificationRepository.count() + " notifications");
    }

    private void seedDepartmentsProgramsCoursesRooms() {
        if (departmentRepository.count() > 0) return;

        // Departments
        Department cs = departmentRepository.save(Department.builder().name("Computer Science").build());
        Department se = departmentRepository.save(Department.builder().name("Software Engineering").build());
        Department phy = departmentRepository.save(Department.builder().name("Physics").build());
        Department chem = departmentRepository.save(Department.builder().name("Chemistry").build());
        Department bio = departmentRepository.save(Department.builder().name("Biology").build());
        Department math = departmentRepository.save(Department.builder().name("Mathematics").build());
        Department eng = departmentRepository.save(Department.builder().name("Engineering").build());
        Department ds = departmentRepository.save(Department.builder().name("Data Science").build());
        Department ml = departmentRepository.save(Department.builder().name("Machine Learning").build());
        Department library = departmentRepository.save(Department.builder().name("Library").build());

        // Programs
        Program bscs = programRepository.save(Program.builder().name("BSc Computer Science").department(cs).build());
        Program bsse = programRepository.save(Program.builder().name("BSc Software Engineering").department(se).build());
        Program bphys = programRepository.save(Program.builder().name("BSc Physics").department(phy).build());
        Program bchem = programRepository.save(Program.builder().name("BSc Chemistry").department(chem).build());
        Program bbio = programRepository.save(Program.builder().name("BSc Biology").department(bio).build());
        Program bmath = programRepository.save(Program.builder().name("BSc Mathematics").department(math).build());
        Program beng = programRepository.save(Program.builder().name("BEng").department(eng).build());
        Program bds = programRepository.save(Program.builder().name("BSc Data Science").department(ds).build());
        Program bml = programRepository.save(Program.builder().name("BSc Machine Learning").department(ml).build());

        // Courses (align with departments)
        courseRepository.save(Course.builder().code("CS101").name("Intro to CS").program(bscs).department(cs).build());
        courseRepository.save(Course.builder().code("CS201").name("Algorithms").program(bscs).department(cs).build());
        courseRepository.save(Course.builder().code("SE201").name("OOP & Design").program(bsse).department(se).build());
        courseRepository.save(Course.builder().code("SE301").name("Software Architecture").program(bsse).department(se).build());
        courseRepository.save(Course.builder().code("PH101").name("Mechanics").program(bphys).department(phy).build());
        courseRepository.save(Course.builder().code("CH101").name("Organic Chemistry").program(bchem).department(chem).build());
        courseRepository.save(Course.builder().code("BI101").name("Cell Biology").program(bbio).department(bio).build());
        courseRepository.save(Course.builder().code("MA101").name("Calculus I").program(bmath).department(math).build());
        courseRepository.save(Course.builder().code("EN201").name("Thermodynamics").program(beng).department(eng).build());
        courseRepository.save(Course.builder().code("DS201").name("Data Mining").program(bds).department(ds).build());
        courseRepository.save(Course.builder().code("ML301").name("Deep Learning").program(bml).department(ml).build());

        // Core rooms including quiet library rooms (10 study rooms)
        roomRepository.save(Room.builder().number("LIB-101").building("Library").capacity(20).department(library).resources("Tables, Quiet Zone").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-102").building("Library").capacity(8).department(library).resources("Study Pod, Quiet Zone").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-103").building("Library").capacity(6).department(library).resources("Study Pod, Whiteboard").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-104").building("Library").capacity(10).department(library).resources("Tables, Power Outlets").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-105").building("Library").capacity(14).department(library).resources("Smart Board, Quiet Zone").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-201").building("Library").capacity(12).department(library).resources("Study Pods, Quiet Zone").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-202").building("Library").capacity(16).department(library).resources("Group Study, Whiteboard").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-203").building("Library").capacity(4).department(library).resources("Booth, Power Outlets").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-204").building("Library").capacity(18).department(library).resources("Tables, Quiet Zone, AC").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("LIB-205").building("Library").capacity(10).department(library).resources("Study Pod, Monitor").quiet(true).type(Room.RoomType.LIBRARY).build());
        roomRepository.save(Room.builder().number("CS-201").building("Building A").capacity(40).department(cs).resources("Projector, Whiteboard, Computers").quiet(false).type(Room.RoomType.CLASSROOM).build());
        roomRepository.save(Room.builder().number("SE-301").building("Building B").capacity(35).department(se).resources("Projector, Whiteboard").quiet(false).type(Room.RoomType.CLASSROOM).build());
        roomRepository.save(Room.builder().number("PHY-LAB1").building("Building C").capacity(24).department(phy).resources("Lab Equipment, Safety Gear").quiet(false).type(Room.RoomType.LAB).build());
    }

    private List<Users> createUsers() {
        List<Users> users = new ArrayList<>();
        
        // Admin user
        users.add(Users.builder()
                .username("admin")
                .email("admin@sheridancollege.ca")
                .password(passwordEncoder.encode("admin123"))
                .role(Users.Role.Admin)
                .gender(Users.Gender.Other)
                .birthdate(LocalDate.of(1980, 1, 1))
                .address("123 Admin St")
                .city("Toronto")
                .country("Canada")
                .bio("System administrator")
                .status("Active")
                .socialLinks("https://linkedin.com/admin")
                .profilePicture("https://ui-avatars.com/api/?name=admin")
                .department("Administration")
                .build());
        
        // Faculty users
        users.add(Users.builder()
                .username("john_doe")
                .email("john.doe@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Male)
                .birthdate(LocalDate.of(1975, 5, 20))
                .address("456 College St")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Computer Science")
                .status("Active")
                .socialLinks("https://linkedin.com/john_doe")
                .profilePicture("https://ui-avatars.com/api/?name=John+Doe")
                .department("Computer Science")
                .build());

        users.add(Users.builder()
                .username("jane_smith")
                .email("jane.smith@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Female)
                .birthdate(LocalDate.of(1982, 8, 15))
                .address("789 University Ave")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Software Engineering")
                .status("Active")
                .socialLinks("https://linkedin.com/jane_smith")
                .profilePicture("https://ui-avatars.com/api/?name=Jane+Smith")
                .department("Software Engineering")
                .build());

        users.add(Users.builder()
                .username("dr_brown")
                .email("dr.brown@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Male)
                .birthdate(LocalDate.of(1978, 3, 10))
                .address("321 Faculty Rd")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Physics")
                .status("Active")
                .socialLinks("https://linkedin.com/dr_brown")
                .profilePicture("https://ui-avatars.com/api/?name=Dr+Brown")
                .department("Physics")
                .build());

        users.add(Users.builder()
                .username("prof_wilson")
                .email("prof.wilson@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Female)
                .birthdate(LocalDate.of(1985, 7, 22))
                .address("654 Academic St")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Chemistry")
                .status("Active")
                .socialLinks("https://linkedin.com/prof_wilson")
                .profilePicture("https://ui-avatars.com/api/?name=Prof+Wilson")
                .department("Chemistry")
                .build());

        users.add(Users.builder()
                .username("dr_davis")
                .email("dr.davis@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Male)
                .birthdate(LocalDate.of(1972, 11, 5))
                .address("987 Research Ave")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Biology")
                .status("Active")
                .socialLinks("https://linkedin.com/dr_davis")
                .profilePicture("https://ui-avatars.com/api/?name=Dr+Davis")
                .department("Biology")
                .build());

        users.add(Users.builder()
                .username("prof_miller")
                .email("prof.miller@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Female)
                .birthdate(LocalDate.of(1988, 4, 18))
                .address("147 Education Blvd")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Mathematics")
                .status("Active")
                .socialLinks("https://linkedin.com/prof_miller")
                .profilePicture("https://ui-avatars.com/api/?name=Prof+Miller")
                .department("Mathematics")
                .build());

        users.add(Users.builder()
                .username("dr_taylor")
                .email("dr.taylor@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Male)
                .birthdate(LocalDate.of(1976, 9, 30))
                .address("258 Science St")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Engineering")
                .status("Active")
                .socialLinks("https://linkedin.com/dr_taylor")
                .profilePicture("https://ui-avatars.com/api/?name=Dr+Taylor")
                .department("Engineering")
                .build());

        users.add(Users.builder()
                .username("prof_anderson")
                .email("prof.anderson@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Female)
                .birthdate(LocalDate.of(1983, 12, 12))
                .address("369 Innovation Dr")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Data Science")
                .status("Active")
                .socialLinks("https://linkedin.com/prof_anderson")
                .profilePicture("https://ui-avatars.com/api/?name=Prof+Anderson")
                .department("Data Science")
                .build());

        users.add(Users.builder()
                .username("dr_thomas")
                .email("dr.thomas@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Professor)
                .gender(Users.Gender.Male)
                .birthdate(LocalDate.of(1979, 6, 25))
                .address("741 Technology Way")
                .city("Toronto")
                .country("Canada")
                .bio("Professor of Machine Learning")
                .status("Active")
                .socialLinks("https://linkedin.com/dr_thomas")
                .profilePicture("https://ui-avatars.com/api/?name=Dr+Thomas")
                .department("Machine Learning")
                .build());
        
        // Coordinator for Resource Rentals / Library
        users.add(Users.builder()
                .username("coordinator_lib")
                .email("coordinator.library@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Coordinator)
                .gender(Users.Gender.Other)
                .birthdate(LocalDate.of(1988, 1, 1))
                .address("1 Library Ave")
                .city("Toronto")
                .country("Canada")
                .bio("Library and resource rental coordinator")
                .status("Active")
                .socialLinks("https://linkedin.com/coordinator_lib")
                .profilePicture("https://ui-avatars.com/api/?name=Coordinator+Lib")
                .department("Library")
                .build());

        // Five Student users
        users.add(Users.builder()
                .username("student_amy")
                .email("amy.student@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Student)
                .gender(Users.Gender.Female)
                .birthdate(LocalDate.of(2003, 3, 15))
                .address("11 Student Way")
                .city("Toronto")
                .country("Canada")
                .bio("CS student")
                .status("Active")
                .profilePicture("https://ui-avatars.com/api/?name=Amy+Student")
                .department("Computer Science")
                .build());

        users.add(Users.builder()
                .username("student_bob")
                .email("bob.student@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Student)
                .gender(Users.Gender.Male)
                .birthdate(LocalDate.of(2002, 7, 9))
                .address("12 Student Way")
                .city("Toronto")
                .country("Canada")
                .bio("SE student")
                .status("Active")
                .profilePicture("https://ui-avatars.com/api/?name=Bob+Student")
                .department("Software Engineering")
                .build());

        users.add(Users.builder()
                .username("student_cara")
                .email("cara.student@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Student)
                .gender(Users.Gender.Female)
                .birthdate(LocalDate.of(2004, 11, 21))
                .address("13 Student Way")
                .city("Toronto")
                .country("Canada")
                .bio("Physics student")
                .status("Active")
                .profilePicture("https://ui-avatars.com/api/?name=Cara+Student")
                .department("Physics")
                .build());

        users.add(Users.builder()
                .username("student_dan")
                .email("dan.student@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Student)
                .gender(Users.Gender.Male)
                .birthdate(LocalDate.of(2003, 2, 2))
                .address("14 Student Way")
                .city("Toronto")
                .country("Canada")
                .bio("Biology student")
                .status("Active")
                .profilePicture("https://ui-avatars.com/api/?name=Dan+Student")
                .department("Biology")
                .build());

        users.add(Users.builder()
                .username("student_ella")
                .email("ella.student@sheridancollege.ca")
                .password(passwordEncoder.encode("password123"))
                .role(Users.Role.Student)
                .gender(Users.Gender.Female)
                .birthdate(LocalDate.of(2002, 5, 28))
                .address("15 Student Way")
                .city("Toronto")
                .country("Canada")
                .bio("Math student")
                .status("Active")
                .profilePicture("https://ui-avatars.com/api/?name=Ella+Student")
                .department("Mathematics")
                .build());

        return users;
    }

    private List<RoomPost> createRoomPosts(List<Users> users) {
        List<RoomPost> roomPosts = new ArrayList<>();
        String[] rooms = {"Room 101", "Room 102", "Room 103", "Room 201", "Room 202", "Room 203", "Room 301", "Room 302", "Room 303", "Room 401"};
        String[] subjects = {"Computer Science", "Mathematics", "Physics", "Chemistry", "Biology", "Engineering", "Statistics", "Data Science", "Machine Learning", "Software Engineering"};
        String[] locations = {"Building A, Floor 1", "Building A, Floor 2", "Building B, Floor 1", "Building B, Floor 2", "Building C, Floor 1", "Building C, Floor 2", "Building D, Floor 1", "Building D, Floor 2", "Building E, Floor 1", "Building E, Floor 2"};
        String[] resources = {"Projector, Whiteboard, Computers", "Whiteboard, Projector", "Lab Equipment, Whiteboard", "Lab Equipment, Safety Gear", "Microscopes, Lab Equipment", "3D Printer, Computers", "Smart Board, Projector", "Audio System, Microphone", "VR Equipment, Computers", "Conference Table, Whiteboard"};
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        
        for (int i = 0; i < users.size(); i++) {
            Users user = users.get(i);
            if (user.getRole() == Users.Role.Professor) {
                // Create 5 room posts per professor based on their weekly schedule
                for (int j = 0; j < 5; j++) {
                    LocalDate date = LocalDate.now().plusDays(j + 1);
                    LocalTime startTime = LocalTime.of(9 + j, 0);
                    LocalTime endTime = startTime.plusHours(2);
                    
                    // Use the same room and subject for consistency with schedule
                    String room = rooms[(i + j) % rooms.length];
                    String subject = subjects[(i + j) % subjects.length];
                    
                    // Calculate the actual day name based on the date to ensure consistency
                    String actualDayName = date.getDayOfWeek().toString();
                    actualDayName = actualDayName.charAt(0) + actualDayName.substring(1).toLowerCase();
                    
                    RoomPost roomPost = RoomPost.builder()
                            .room(room)
                            .description(subject + " - " + getClassType(j) + " (" + actualDayName + ")")
                            .postedBy(user.getUsername())
                            .location(locations[(i + j) % locations.length])
                            .capacity(20 + (i + j) * 5)
                            .resources(resources[(i + j) % resources.length])
                            .date(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .users(user)
                            .build();
                    
                    roomPosts.add(roomPost);
                }
                
                // Add additional room posts for better matching opportunities
                // Add afternoon sessions
                for (int j = 0; j < 3; j++) {
                    LocalDate date = LocalDate.now().plusDays(j + 1);
                    LocalTime startTime = LocalTime.of(14 + j, 0); // Afternoon sessions
                    LocalTime endTime = startTime.plusHours(2);
                    
                    String room = rooms[(i + j + 5) % rooms.length];
                    String subject = subjects[(i + j + 5) % subjects.length];
                    
                    // Calculate the actual day name based on the date to ensure consistency
                    String actualDayName = date.getDayOfWeek().toString();
                    actualDayName = actualDayName.charAt(0) + actualDayName.substring(1).toLowerCase();
                    
                    RoomPost roomPost = RoomPost.builder()
                            .room(room)
                            .description(subject + " - " + getClassType(j + 5) + " (" + actualDayName + ")")
                            .postedBy(user.getUsername())
                            .location(locations[(i + j + 5) % locations.length])
                            .capacity(25 + (i + j) * 3)
                            .resources(resources[(i + j + 5) % resources.length])
                            .date(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .users(user)
                            .build();
                    
                    roomPosts.add(roomPost);
                }
            }
        }
        
        return roomPosts;
    }

    private List<RoomRequest> createRoomRequests(List<Users> users) {
        List<RoomRequest> roomRequests = new ArrayList<>();
        String[] requestTypes = {"Study group session", "Research presentation", "Faculty meeting", "Workshop", "Seminar", "Conference", "Training session", "Project discussion", "Thesis defense", "Guest lecture"};
        String[] rooms = {"Room 101", "Room 102", "Room 103", "Room 201", "Room 202", "Room 203", "Room 301", "Room 302", "Room 303", "Room 401"};
        String[] locations = {"Building A, Floor 1", "Building A, Floor 2", "Building B, Floor 1", "Building B, Floor 2", "Building C, Floor 1", "Building C, Floor 2", "Building D, Floor 1", "Building D, Floor 2", "Building E, Floor 1", "Building E, Floor 2"};
        String[] resources = {"Whiteboard, Tables", "Projector, Microphone", "Conference table, Whiteboard", "Audio System, Projector", "Lab Equipment", "Computers, Projector", "Smart Board", "VR Equipment", "3D Printer", "Conference Setup"};
        
        // Create matching requests for better allocation opportunities
        // Each professor will create requests that match other professors' room posts
        
        for (int i = 0; i < users.size(); i++) {
            Users requester = users.get(i);
            if (requester.getRole() == Users.Role.Professor) {
                // Create 3 requests per professor that match other professors' room posts
                for (int j = 0; j < 3; j++) {
                    // Match with a different professor's room posts
                    int targetUserIndex = (i + j + 1) % users.size();
                    if (targetUserIndex == i) targetUserIndex = (i + 1) % users.size();
                    
                    Users targetUser = users.get(targetUserIndex);
                    
                    LocalDate date = LocalDate.now().plusDays(j + 1);
                    LocalTime startTime = LocalTime.of(9 + j, 0);
                    LocalTime endTime = startTime.plusHours(2);
                    
                    // Create requests that match the target user's room characteristics
                    String targetLocation = locations[(targetUserIndex + j) % locations.length];
                    String targetResources = resources[(targetUserIndex + j) % resources.length];
                    
                    RoomRequest roomRequest = RoomRequest.builder()
                            .description(requestTypes[j % requestTypes.length] + " - " + requester.getUsername())
                            .postedBy(requester.getUsername())
                            .location(targetLocation) // Match target user's location
                            .capacity(15 + j * 5) // Smaller capacity than room posts
                            .resources(targetResources) // Match target user's resources
                            .date(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .users(requester)
                            .build();
                    
                    roomRequests.add(roomRequest);
                }
                
                // Add additional requests for more matching opportunities
                for (int j = 0; j < 2; j++) {
                    LocalDate date = LocalDate.now().plusDays(j + 4);
                    LocalTime startTime = LocalTime.of(14 + j, 0); // Afternoon sessions
                    LocalTime endTime = startTime.plusHours(1).plusMinutes(30);
                    
                    RoomRequest roomRequest = RoomRequest.builder()
                            .description("Additional " + requestTypes[(j + 3) % requestTypes.length] + " - " + requester.getUsername())
                            .postedBy(requester.getUsername())
                            .location(locations[(i + j + 3) % locations.length])
                            .capacity(20 + j * 3)
                            .resources(resources[(i + j + 3) % resources.length])
                            .date(date)
                            .startTime(startTime)
                            .endTime(endTime)
                            .users(requester)
                            .build();
                    
                    roomRequests.add(roomRequest);
                }
            }
        }
        
        return roomRequests;
    }

    private List<DashBoard> createScheduleEntries(List<Users> users) {
        List<DashBoard> dashboardEntries = new ArrayList<>();
        String[] rooms = {"Room 101", "Room 102", "Room 103", "Room 201", "Room 202", "Room 203", "Room 301", "Room 302", "Room 303", "Room 401"};
        String[] subjects = {"Computer Science", "Mathematics", "Physics", "Chemistry", "Biology", "Engineering", "Statistics", "Data Science", "Machine Learning", "Software Engineering"};
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        
        for (int userIndex = 0; userIndex < users.size(); userIndex++) {
            Users user = users.get(userIndex);
            if (user.getRole() == Users.Role.Professor) {
                // Create clean, non-conflicting schedule for each professor
                // Each professor gets 3-4 classes per week, spread across different days and times
                
                // Professor 1 (john_doe): Chemistry on Monday 9:00-10:30
                // Professor 2 (jane_smith): Biology on Tuesday 11:00-12:30, Engineering on Wednesday 1:00-2:30
                // Professor 3 (dr_brown): Physics on Tuesday 9:00-10:30
                // Professor 4 (prof_wilson): Chemistry on Monday 9:00-10:30
                // Professor 5 (dr_davis): Biology on Tuesday 11:00-12:30
                // Professor 6 (prof_miller): Mathematics on Thursday 10:00-11:30
                // Professor 7 (dr_taylor): Engineering on Wednesday 1:00-2:30
                // Professor 8 (prof_anderson): Data Science on Friday 2:00-3:30
                // Professor 9 (dr_thomas): Machine Learning on Friday 10:00-11:30
                
                switch (user.getUsername()) {
                    case "john_doe":
                        // Chemistry - Monday 9:00-10:30 AM
                        addScheduleEntry(dashboardEntries, user, "Room 201", "Chemistry", "Monday", 
                                       LocalTime.of(9, 0), LocalTime.of(10, 30));
                        // Computer Science - Wednesday 11:00-12:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 101", "Computer Science", "Wednesday", 
                                       LocalTime.of(11, 0), LocalTime.of(12, 30));
                        // Mathematics - Friday 2:00-3:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 102", "Mathematics", "Friday", 
                                       LocalTime.of(14, 0), LocalTime.of(15, 30));
                        break;
                        
                    case "jane_smith":
                        // Biology - Tuesday 11:00-12:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 202", "Biology", "Tuesday", 
                                       LocalTime.of(11, 0), LocalTime.of(12, 30));
                        // Engineering - Wednesday 1:00-2:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 203", "Engineering", "Wednesday", 
                                       LocalTime.of(13, 0), LocalTime.of(14, 30));
                        // Software Engineering - Friday 9:00-10:30 AM
                        addScheduleEntry(dashboardEntries, user, "Room 301", "Software Engineering", "Friday", 
                                       LocalTime.of(9, 0), LocalTime.of(10, 30));
                        break;
                        
                    case "dr_brown":
                        // Physics - Tuesday 9:00-10:30 AM
                        addScheduleEntry(dashboardEntries, user, "Room 302", "Physics", "Tuesday", 
                                       LocalTime.of(9, 0), LocalTime.of(10, 30));
                        // Statistics - Thursday 1:00-2:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 303", "Statistics", "Thursday", 
                                       LocalTime.of(13, 0), LocalTime.of(14, 30));
                        break;
                        
                    case "prof_wilson":
                        // Chemistry - Monday 9:00-10:30 AM (same as john_doe - this creates the conflict we saw)
                        addScheduleEntry(dashboardEntries, user, "Room 401", "Chemistry", "Monday", 
                                       LocalTime.of(9, 0), LocalTime.of(10, 30));
                        // Remove this conflict - let's give prof_wilson a different time
                        // Biology - Monday 11:00-12:30 PM (moved to avoid conflict)
                        addScheduleEntry(dashboardEntries, user, "Room 401", "Biology", "Monday", 
                                       LocalTime.of(11, 0), LocalTime.of(12, 30));
                        // Physics - Thursday 2:00-3:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 102", "Physics", "Thursday", 
                                       LocalTime.of(14, 0), LocalTime.of(15, 30));
                        break;
                        
                    case "dr_davis":
                        // Biology - Tuesday 11:00-12:30 PM (same as jane_smith - this creates conflict)
                        addScheduleEntry(dashboardEntries, user, "Room 103", "Biology", "Tuesday", 
                                       LocalTime.of(11, 0), LocalTime.of(12, 30));
                        // Remove this conflict - let's give dr_davis a different time
                        // Chemistry - Tuesday 2:00-3:30 PM (moved to avoid conflict)
                        addScheduleEntry(dashboardEntries, user, "Room 103", "Chemistry", "Tuesday", 
                                       LocalTime.of(14, 0), LocalTime.of(15, 30));
                        // Engineering - Thursday 9:00-10:30 AM
                        addScheduleEntry(dashboardEntries, user, "Room 201", "Engineering", "Thursday", 
                                       LocalTime.of(9, 0), LocalTime.of(10, 30));
                        break;
                        
                    case "prof_miller":
                        // Mathematics - Thursday 10:00-11:30 AM
                        addScheduleEntry(dashboardEntries, user, "Room 202", "Mathematics", "Thursday", 
                                       LocalTime.of(10, 0), LocalTime.of(11, 30));
                        // Statistics - Tuesday 1:00-2:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 301", "Statistics", "Tuesday", 
                                       LocalTime.of(13, 0), LocalTime.of(14, 30));
                        break;
                        
                    case "dr_taylor":
                        // Engineering - Wednesday 1:00-2:30 PM (same as jane_smith - this creates conflict)
                        addScheduleEntry(dashboardEntries, user, "Room 302", "Engineering", "Wednesday", 
                                       LocalTime.of(13, 0), LocalTime.of(14, 30));
                        // Remove this conflict - let's give dr_taylor a different time
                        // Physics - Wednesday 9:00-10:30 AM (moved to avoid conflict)
                        addScheduleEntry(dashboardEntries, user, "Room 302", "Physics", "Wednesday", 
                                       LocalTime.of(9, 0), LocalTime.of(10, 30));
                        // Data Science - Friday 11:00-12:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 303", "Data Science", "Friday", 
                                       LocalTime.of(11, 0), LocalTime.of(12, 30));
                        break;
                        
                    case "prof_anderson":
                        // Data Science - Friday 2:00-3:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 401", "Data Science", "Friday", 
                                       LocalTime.of(14, 0), LocalTime.of(15, 30));
                        // Machine Learning - Monday 1:00-2:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 102", "Machine Learning", "Monday", 
                                       LocalTime.of(13, 0), LocalTime.of(14, 30));
                        break;
                        
                    case "dr_thomas":
                        // Machine Learning - Friday 10:00-11:30 AM
                        addScheduleEntry(dashboardEntries, user, "Room 201", "Machine Learning", "Friday", 
                                       LocalTime.of(10, 0), LocalTime.of(11, 30));
                        // Computer Science - Wednesday 2:00-3:30 PM
                        addScheduleEntry(dashboardEntries, user, "Room 103", "Computer Science", "Wednesday", 
                                       LocalTime.of(14, 0), LocalTime.of(15, 30));
                        break;
                }
            }
        }
        
        return dashboardEntries;
    }
    
    // Helper method to add a schedule entry
    private void addScheduleEntry(List<DashBoard> dashboardEntries, Users user, String room, String subject, 
                                 String day, LocalTime startTime, LocalTime endTime) {
        // Create entries for the next 4 weeks to simulate current semester
        for (int week = 0; week < 4; week++) {
            LocalDate date = LocalDate.now().plusDays(week * 7 + getDayOffset(day));
            
            // Calculate the actual day name based on the date to ensure consistency
            String actualDayName = date.getDayOfWeek().toString();
            actualDayName = actualDayName.charAt(0) + actualDayName.substring(1).toLowerCase();
            
            DashBoard entry = DashBoard.builder()
                    .room(room)
                    .subject(subject)
                    .day(actualDayName) // Use the actual day name based on the date
                    .date(date)
                    .startTime(startTime)
                    .endTime(endTime)
                    .users(user)
                    .build();
            
            dashboardEntries.add(entry);
        }
    }
    
    // Helper method to get day offset for scheduling
    private int getDayOffset(String day) {
        switch (day) {
            case "Monday": return 1;
            case "Tuesday": return 2;
            case "Wednesday": return 3;
            case "Thursday": return 4;
            case "Friday": return 5;
            default: return 1;
        }
    }

    private List<Notification> createNotifications(List<Users> users) {
        List<Notification> notifications = new ArrayList<>();
        
        // System notifications
        notifications.add(Notification.builder()
                .userId(1L)
                .title("System Welcome")
                .message("Welcome to the Faculty Room Management System!")
                .type(Notification.NotificationType.SYSTEM)
                .read(false)
                .build());
        
        // Room post notifications with proper timestamps
        for (int i = 0; i < 5; i++) {
            Users user = users.get(i + 1); // Skip admin
            notifications.add(Notification.builder()
                    .userId(1L)
                    .title("New Room Posted")
                    .message("Room posted by " + user.getUsername())
                    .type(Notification.NotificationType.ROOM_POST)
                    .read(false)
                    .timestamp(LocalDateTime.now().minusHours(i + 1)) // Recent notifications
                    .build());
        }
        
        // Room request notifications with proper timestamps
        for (int i = 0; i < 3; i++) {
            Users user = users.get(i + 1); // Skip admin
            notifications.add(Notification.builder()
                    .userId(1L)
                    .title("New Room Request")
                    .message("Room request from " + user.getUsername())
                    .type(Notification.NotificationType.ROOM_REQUEST)
                    .read(false)
                    .timestamp(LocalDateTime.now().minusMinutes(30 + i * 15)) // Recent notifications
                    .build());
        }
        
        // User-specific notifications with proper timestamps
        for (int i = 1; i < users.size(); i++) {
            Users user = users.get(i);
            notifications.add(Notification.builder()
                    .userId((long)(i + 1))
                    .title("Welcome " + user.getUsername())
                    .message("Your account is ready to use!")
                    .type(Notification.NotificationType.INFO)
                    .read(false)
                    .timestamp(LocalDateTime.now().minusDays(i)) // Welcome notifications spread over days
                    .build());
        }
        
        // Room allocation notifications (simulating successful allocations)
        if (users.size() >= 2) {
            // Notification for prof_anderson about successful room allocation
            notifications.add(Notification.builder()
                    .userId(2L) // prof_anderson's user ID
                    .title("Room Allocated Successfully")
                    .message("Your room request has been fulfilled by prof_brown")
                    .type(Notification.NotificationType.SUCCESS)
                    .read(false)
                    .timestamp(LocalDateTime.now().minusHours(2)) // Recent allocation
                    .build());
        }
        
        if (users.size() >= 3) {
            // Notification for prof_brown about successful room allocation
            notifications.add(Notification.builder()
                    .userId(3L) // prof_brown's user ID
                    .title("Room Allocated Successfully")
                    .message("Your room request has been fulfilled by prof_davis")
                    .type(Notification.NotificationType.SUCCESS)
                    .read(false)
                    .timestamp(LocalDateTime.now().minusHours(4)) // Recent allocation
                    .build());
        }
        
        // Message notifications with proper timestamps
        if (users.size() >= 2) {
            notifications.add(Notification.builder()
                    .userId(2L) // prof_anderson
                    .title("New Message")
                    .message("You have a new message from prof_brown")
                    .type(Notification.NotificationType.MESSAGE)
                    .read(false)
                    .timestamp(LocalDateTime.now().minusMinutes(15)) // Recent message
                    .build());
        }
        
        // Additional chat notifications
        if (users.size() >= 3) {
            notifications.add(Notification.builder()
                    .userId(3L) // prof_brown
                    .title("New Message")
                    .message("You have a new message from prof_davis")
                    .type(Notification.NotificationType.MESSAGE)
                    .read(false)
                    .timestamp(LocalDateTime.now().minusMinutes(45)) // Recent message
                    .build());
        }
        
        // System notifications with proper timestamps
        notifications.add(Notification.builder()
                .userId(1L) // Admin
                .title("System Update")
                .message("New features have been added to the room management system")
                .type(Notification.NotificationType.SYSTEM)
                .read(false)
                .timestamp(LocalDateTime.now().minusDays(1)) // Yesterday
                .build());
        
        return notifications;
    }

    private String getClassType(int index) {
        String[] types = {"Lecture", "Lab Session", "Tutorial", "Workshop", "Seminar"};
        return types[index % types.length];
    }

    private void seedResources() {
        if (resourceRepository.count() > 0) return;

        System.out.println("🔧 Seeding resources...");

        // Get departments for resource assignment
        List<Department> departments = departmentRepository.findAll();
        Department csDept = departments.stream()
                .filter(d -> d.getName().equals("Computer Science"))
                .findFirst()
                .orElse(departments.isEmpty() ? null : departments.get(0));

        List<Resource> resources = new ArrayList<>();

        // 1) Electronics (5)
        resources.add(Resource.builder().name("Dell XPS 13 Laptop").description("Intel i7, 16GB, 512GB SSD").category("Electronics").location("IT Lab - Room 101").condition("Excellent").specifications("i7/16/512").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(25)).depositAmount(java.math.BigDecimal.valueOf(500)).imageUrl("/images/electronics-laptop-1.jpg").department(csDept).build());
        resources.add(Resource.builder().name("MacBook Pro 14\"").description("Apple M2, 16GB, 512GB").category("Electronics").location("IT Lab - Room 102").condition("Excellent").specifications("M2/16/512").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(35)).depositAmount(java.math.BigDecimal.valueOf(800)).imageUrl("/images/electronics-macbook-2.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Lenovo ThinkPad T14").description("Ryzen 7, 16GB, 512GB").category("Electronics").location("IT Lab - Room 103").condition("Good").specifications("Ryzen7/16/512").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(20)).depositAmount(java.math.BigDecimal.valueOf(400)).imageUrl("/images/electronics-thinkpad-3.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Dell Precision Mobile").description("i9, 32GB, 1TB SSD").category("Electronics").location("IT Lab - Room 104").condition("Good").specifications("i9/32/1TB").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(40)).depositAmount(java.math.BigDecimal.valueOf(1000)).imageUrl("/images/electronics-precision-4.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Surface Laptop").description("Intel i5, 8GB, 256GB").category("Electronics").location("IT Lab - Room 105").condition("Good").specifications("i5/8/256").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(15)).depositAmount(java.math.BigDecimal.valueOf(300)).imageUrl("/images/electronics-surface-5.jpg").department(csDept).build());

        // 2) Transportation (5)
        resources.add(Resource.builder().name("Campus Bike - Mountain A").description("21-speed mountain bike").category("Transportation").location("Bike Rack - Main Building").condition("Good").specifications("Disc brakes").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(10)).depositAmount(java.math.BigDecimal.valueOf(200)).imageUrl("/images/transport-bike-1.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Campus Bike - Mountain B").description("21-speed mountain bike").category("Transportation").location("Bike Rack - Library").condition("Good").specifications("Disc brakes").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(10)).depositAmount(java.math.BigDecimal.valueOf(200)).imageUrl("/images/transport-bike-2.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Campus Bike - City A").description("7-speed city bike").category("Transportation").location("Bike Rack - Library").condition("Good").specifications("Basket included").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(8)).depositAmount(java.math.BigDecimal.valueOf(150)).imageUrl("/images/transport-bike-3.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Scooter - Electric A").description("Electric scooter for quick rides").category("Transportation").location("Bike Rack - A Wing").condition("Good").specifications("350W motor").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(12)).depositAmount(java.math.BigDecimal.valueOf(250)).imageUrl("/images/transport-scooter-4.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Scooter - Electric B").description("Electric scooter, long range").category("Transportation").location("Bike Rack - B Wing").condition("Good").specifications("500W motor").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(14)).depositAmount(java.math.BigDecimal.valueOf(300)).imageUrl("/images/transport-scooter-5.jpg").department(csDept).build());

        // 3) Audio/Visual (5)
        resources.add(Resource.builder().name("Epson Projector HD").description("1080p projector 3500 lumens").category("Audio/Visual").location("Media Room - Library").condition("Excellent").specifications("HDMI/VGA").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(15)).depositAmount(java.math.BigDecimal.valueOf(300)).imageUrl("/images/av-projector-1.jpg").department(csDept).build());
        resources.add(Resource.builder().name("BenQ Projector 4K").description("4K projector 3000 lumens").category("Audio/Visual").location("Media Room - A2").condition("Excellent").specifications("HDMI/DP").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(22)).depositAmount(java.math.BigDecimal.valueOf(500)).imageUrl("/images/av-projector-2.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Portable PA System").description("Battery powered speaker + mic").category("Audio/Visual").location("Events Store").condition("Good").specifications("Wireless mic").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(18)).depositAmount(java.math.BigDecimal.valueOf(350)).imageUrl("/images/av-pa-3.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Studio Microphone").description("Condenser mic with stand").category("Audio/Visual").location("Media Lab").condition("Good").specifications("XLR/USB").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(9)).depositAmount(java.math.BigDecimal.valueOf(120)).imageUrl("/images/av-mic-4.jpg").department(csDept).build());
        resources.add(Resource.builder().name("4K Camcorder").description("Compact 4K video camera").category("Audio/Visual").location("Media Lab").condition("Good").specifications("4K60 HDR").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(20)).depositAmount(java.math.BigDecimal.valueOf(400)).imageUrl("/images/av-camera-5.jpg").department(csDept).build());

        // 4) Academic Tools (5)
        resources.add(Resource.builder().name("TI-84 Plus CE").description("Graphing calculator").category("Academic Tools").location("Math Lab - Room 205").condition("Good").specifications("Color display").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(5)).depositAmount(java.math.BigDecimal.valueOf(100)).imageUrl("/images/tools-calculator-1.jpg").department(csDept).build());
        resources.add(Resource.builder().name("TI-Nspire CX II").description("Advanced calculator").category("Academic Tools").location("Math Lab - Room 205").condition("Good").specifications("CAS").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(6)).depositAmount(java.math.BigDecimal.valueOf(120)).imageUrl("/images/tools-calculator-2.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Scientific Calculator A").description("Scientific calculator").category("Academic Tools").location("Math Lab - Room 206").condition("Good").specifications("240 functions").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(3)).depositAmount(java.math.BigDecimal.valueOf(50)).imageUrl("/images/tools-calculator-3.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Lab Kit - Physics").description("Basic physics lab kit").category("Academic Tools").location("Physics Lab").condition("Good").specifications("Optics + mechanics").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(7)).depositAmount(java.math.BigDecimal.valueOf(150)).imageUrl("/images/tools-labkit-4.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Lab Kit - Chemistry").description("Basic chemistry lab kit").category("Academic Tools").location("Chem Lab").condition("Good").specifications("Glassware + reagents").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(7)).depositAmount(java.math.BigDecimal.valueOf(150)).imageUrl("/images/tools-labkit-5.jpg").department(csDept).build());

        // 5) Books (5)
        resources.add(Resource.builder().name("Algorithms - CLRS 3rd").description("Algorithms textbook").category("Books").location("Library - Reference").condition("Good").specifications("Hardcover").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(3)).depositAmount(java.math.BigDecimal.valueOf(50)).imageUrl("/images/books-algo-1.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Operating Systems - Silberschatz").description("OS textbook").category("Books").location("Library - Reference").condition("Good").specifications("9th ed").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(3)).depositAmount(java.math.BigDecimal.valueOf(50)).imageUrl("/images/books-os-2.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Computer Networks - Kurose").description("Networks textbook").category("Books").location("Library - Reference").condition("Good").specifications("7th ed").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(3)).depositAmount(java.math.BigDecimal.valueOf(50)).imageUrl("/images/books-networks-3.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Database System Concepts").description("DB textbook").category("Books").location("Library - Reference").condition("Good").specifications("7th ed").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(3)).depositAmount(java.math.BigDecimal.valueOf(50)).imageUrl("/images/books-db-4.jpg").department(csDept).build());
        resources.add(Resource.builder().name("Clean Code").description("Software craftsmanship").category("Books").location("Library - Reference").condition("Good").specifications("Robert C. Martin").available(true).active(true).rentalPricePerDay(java.math.BigDecimal.valueOf(2)).depositAmount(java.math.BigDecimal.valueOf(30)).imageUrl("/images/books-clean-code-5.jpg").department(csDept).build());

        resourceRepository.saveAll(resources);
        System.out.println("✅ Created " + resources.size() + " resources");
    }

    private void seedResourceRentals(List<Users> savedUsers) {
        if (resourceRentalRepository.count() > 0) return;

        System.out.println("🔧 Seeding resource rentals...");

        List<Resource> resources;
        try {
            resources = resourceRepository.findAll();
        } catch (Exception ex) {
            System.out.println("ℹ️ Skipping resource rentals seeding because resources table is not ready yet: " + ex.getMessage());
            return;
        }
        if (resources.isEmpty()) {
            System.out.println("ℹ️ No resources available to create rentals");
            return;
        }

        Users coordinator = savedUsers.stream()
                .filter(u -> u.getRole() == Users.Role.Coordinator)
                .findFirst().orElse(null);

        List<Users> students = savedUsers.stream()
                .filter(u -> u.getRole() == Users.Role.Student)
                .toList();
        if (students.isEmpty()) {
            System.out.println("ℹ️ No students available to create rentals");
            return;
        }

        List<ResourceRental> rentals = new ArrayList<>();

        // Create a few staged rentals across statuses
        Users s1 = students.get(0);
        Users s2 = students.size() > 1 ? students.get(1) : s1;

        Resource r1 = resources.get(0);
        Resource r2 = resources.size() > 1 ? resources.get(1) : r1;

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        rentals.add(ResourceRental.builder()
                .resource(r1)
                .student(s1)
                .studentEmail(s1.getEmail())
                .studentPhone("555-1001")
                .studentName("Amy Student")
                .requestedAt(now.minusDays(1))
                .pickupTime(now.plusDays(1))
                .returnTime(now.plusDays(4))
                .rentalDays(3)
                .totalAmount(r1.getRentalPricePerDay() != null ? r1.getRentalPricePerDay().multiply(java.math.BigDecimal.valueOf(3)) : java.math.BigDecimal.TEN)
                .depositAmount(r1.getDepositAmount() != null ? r1.getDepositAmount() : java.math.BigDecimal.valueOf(100))
                .pickupLocation(r1.getLocation())
                .returnLocation(r1.getLocation())
                .notes("Need for project work")
                .status(ResourceRental.RentalStatus.PENDING)
                .build());

        rentals.add(ResourceRental.builder()
                .resource(r2)
                .student(s2)
                .studentEmail(s2.getEmail())
                .studentPhone("555-1002")
                .studentName("Bob Student")
                .requestedAt(now.minusDays(2))
                .approvedAt(now.minusDays(1))
                .pickupTime(now.plusHours(6))
                .returnTime(now.plusDays(2))
                .rentalDays(2)
                .totalAmount(r2.getRentalPricePerDay() != null ? r2.getRentalPricePerDay().multiply(java.math.BigDecimal.valueOf(2)) : java.math.BigDecimal.TEN)
                .depositAmount(r2.getDepositAmount() != null ? r2.getDepositAmount() : java.math.BigDecimal.valueOf(100))
                .pickupLocation(r2.getLocation())
                .returnLocation(r2.getLocation())
                .notes("Short-term use")
                .status(ResourceRental.RentalStatus.APPROVED)
                .approvedBy(coordinator)
                .build());

        resourceRentalRepository.saveAll(rentals);
        System.out.println("✅ Created " + rentals.size() + " resource rentals");
    }

    private void seedRoomAssignments(List<Users> savedUsers) {
        if (roomAssignmentRepository.count() > 0) return;

        System.out.println("🏫 Seeding room assignments...");
        
        // Get some professors and rooms for assignments
        List<Users> professors = savedUsers.stream()
                .filter(u -> u.getRole() == Users.Role.Professor)
                .collect(java.util.stream.Collectors.toList());
        
        List<Room> rooms = roomRepository.findAll();
        
        if (professors.isEmpty() || rooms.isEmpty()) {
            System.out.println("⚠️ No professors or rooms available for assignments");
            return;
        }

        List<RoomAssignment> assignments = new ArrayList<>();
        
        // Create some sample assignments
        for (int i = 0; i < Math.min(3, professors.size()); i++) {
            Users professor = professors.get(i);
            Room room = rooms.get(i % rooms.size());
            
            assignments.add(RoomAssignment.builder()
                    .coordinatorId(professor.getId())
                    .department(professor.getDepartment())
                    .roomNumber(room.getNumber())
                    .courseId(1L + i)
                    .courseName("Course " + (i + 1))
                    .semesterStart(java.time.LocalDate.now().minusMonths(2))
                    .semesterEnd(java.time.LocalDate.now().plusMonths(4))
                    .build());
        }
        
        roomAssignmentRepository.saveAll(assignments);
        System.out.println("✅ Created " + assignments.size() + " room assignments");
    }

    private void seedStudents(List<Users> savedUsers) {
        if (studentRepository.count() > 0) return;

        System.out.println("🎓 Seeding students...");
        
        // Get departments and programs
        List<Department> departments = departmentRepository.findAll();
        List<Program> programs = programRepository.findAll();
        
        if (departments.isEmpty() || programs.isEmpty()) {
            System.out.println("⚠️ No departments or programs available for students");
            return;
        }

        // Create student records for users with Student role
        List<Student> students = new ArrayList<>();
        for (Users user : savedUsers) {
            if (user.getRole() == Users.Role.Student) {
                students.add(Student.builder()
                        .user(user)
                        .department(departments.get(0)) // Assign to first department
                        .program(programs.get(0)) // Assign to first program
                        .build());
            }
        }
        
        studentRepository.saveAll(students);
        System.out.println("✅ Created " + students.size() + " student records");
    }
}
