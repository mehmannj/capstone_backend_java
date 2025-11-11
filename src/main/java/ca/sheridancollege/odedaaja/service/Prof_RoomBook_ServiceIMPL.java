package ca.sheridancollege.odedaaja.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import ca.sheridancollege.odedaaja.domain.BookedRoom;
import ca.sheridancollege.odedaaja.domain.DashBoard;
import ca.sheridancollege.odedaaja.domain.RoomPost;
import ca.sheridancollege.odedaaja.domain.RoomRequest;
import ca.sheridancollege.odedaaja.domain.Users;
import ca.sheridancollege.odedaaja.repo.BookedRoomRepo;
import ca.sheridancollege.odedaaja.repo.DashBoardRepo;
import ca.sheridancollege.odedaaja.repo.RoomPostRepository;
import ca.sheridancollege.odedaaja.repo.RoomRequestRepository;
import ca.sheridancollege.odedaaja.repo.UserRepo;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class Prof_RoomBook_ServiceIMPL implements Prof_RoomBook_Service {

    private final UserRepo userRepo;
    private final RoomPostRepository roomPostRepository;
    private final RoomRequestRepository roomRequestRepository;
    
    private final DashBoardRepo dashBoardRepo;
    
    private BookedRoomRepo bookedRoomRepo;

    @Override
    public List<Users> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public Users saveUser(Users user) {
        return userRepo.save(user);
    }

    @Override
    public Users getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public Users updateUser(Long id, Users user) {
        return userRepo.findById(id).map(existingUser -> {
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setPassword(user.getPassword());
            return userRepo.save(existingUser);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }

    @Override
    public Users patchUser(Long id, Map<String, Object> updates) {
        return userRepo.findById(id).map(user -> {
            updates.forEach((key, value) -> {
                switch (key) {
                    case "username": user.setUsername((String) value); break;
                    case "email": user.setEmail((String) value); break;
                    case "password": user.setPassword((String) value); break;
                }
            });
            return userRepo.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public List<RoomPost> getAllPostedRooms() {
        return roomPostRepository.findAll();
    }

    @Override
    public RoomPost saveRoom(RoomPost roomPost) {
        return roomPostRepository.save(roomPost);
    }

    @Override
    public RoomPost getRoomPostById(Long id) {
        return roomPostRepository.findById(id).orElseThrow(() -> new RuntimeException("Room post not found"));
    }

    @Override
    public RoomPost updateRoomPost(Long id, RoomPost roomPost) {
        return roomPostRepository.findById(id).map(existingRoom -> {
            existingRoom.setRoom(roomPost.getRoom());
            existingRoom.setDate(roomPost.getDate());
            existingRoom.setStartTime(roomPost.getStartTime());
            existingRoom.setEndTime(roomPost.getEndTime());
            existingRoom.setPostedBy(roomPost.getPostedBy());
            existingRoom.setDescription(roomPost.getDescription());
            existingRoom.setLocation(roomPost.getLocation());
            existingRoom.setCapacity(roomPost.getCapacity());
            existingRoom.setResources(roomPost.getResources());
            existingRoom.setUsers(roomPost.getUsers());  // Link to Users table
            return roomPostRepository.save(existingRoom);
        }).orElseThrow(() -> new RuntimeException("Room post not found"));
    }

    @Override
    public void deleteRoomPost(Long id) {
        roomPostRepository.deleteById(id);
    }

    @Override
    public List<RoomRequest> getAllRoomRequests() {
        return roomRequestRepository.findAll();
    }

    @Override
    public RoomRequest saveRoomRequest(RoomRequest roomRequest) {
        return roomRequestRepository.save(roomRequest);
    }

    @Override
    public RoomRequest getRoomRequestById(Long id) {
        return roomRequestRepository.findById(id).orElseThrow(() -> new RuntimeException("Room request not found"));
    }

    @Override
    public RoomRequest updateRoomRequest(Long id, RoomRequest roomRequest) {
        return roomRequestRepository.findById(id).map(existingRequest -> {
            existingRequest.setDate(roomRequest.getDate());
            existingRequest.setStartTime(roomRequest.getStartTime());
            existingRequest.setEndTime(roomRequest.getEndTime());
            existingRequest.setDescription(roomRequest.getDescription());
            existingRequest.setLocation(roomRequest.getLocation());
            existingRequest.setCapacity(roomRequest.getCapacity());
            existingRequest.setResources(roomRequest.getResources());
            existingRequest.setUsers(roomRequest.getUsers());  // Link to Users table
            return roomRequestRepository.save(existingRequest);
        }).orElseThrow(() -> new RuntimeException("Room request not found"));
    }

    @Override
    public void deleteRoomRequest(Long id) {
        roomRequestRepository.deleteById(id);
    }

    @Override
    public List<RoomPost> getRoomPostsByUsername(String username) {
        // Some posts may have only postedBy set (when auth is absent), so fetch both and merge
        List<RoomPost> byUserLink = roomPostRepository.findByUsersUsername(username);
        List<RoomPost> byPostedBy = roomPostRepository.findByPostedBy(username);
        if (byUserLink.isEmpty()) return byPostedBy;
        if (byPostedBy.isEmpty()) return byUserLink;
        // merge unique by id
        java.util.Map<Long, RoomPost> merged = new java.util.LinkedHashMap<>();
        for (RoomPost rp : byUserLink) { if (rp.getId() != null) merged.put(rp.getId(), rp); }
        for (RoomPost rp : byPostedBy) { if (rp.getId() != null) merged.putIfAbsent(rp.getId(), rp); }
        return new java.util.ArrayList<>(merged.values());
    }

    @Override
    public List<RoomRequest> getRoomRequestsByUsername(String username) {
        // Some requests may have only postedBy set (when auth is absent), so fetch both and merge
        List<RoomRequest> byUserLink = roomRequestRepository.findByUsersUsername(username);
        List<RoomRequest> byPostedBy = roomRequestRepository.findByPostedBy(username);
        if (byUserLink.isEmpty()) return byPostedBy;
        if (byPostedBy.isEmpty()) return byUserLink;
        java.util.Map<Long, RoomRequest> merged = new java.util.LinkedHashMap<>();
        for (RoomRequest rr : byUserLink) { if (rr.getId() != null) merged.put(rr.getId(), rr); }
        for (RoomRequest rr : byPostedBy) { if (rr.getId() != null) merged.putIfAbsent(rr.getId(), rr); }
        return new java.util.ArrayList<>(merged.values());
    }

    @Override
    public BookedRoom bookRoom(BookedRoom bookedRoom, String username) {
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        bookedRoom.setBookedBy(username);
        bookedRoom.setUsers(user);
        return bookedRoomRepo.save(bookedRoom);
    }

    @Override
    public List<BookedRoom> getAllBookedRooms() {
        return bookedRoomRepo.findAll();
    }

    @Override
    public BookedRoom getBookedRoomById(Long id) {
        return bookedRoomRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Booked Room not found"));
    }

    @Override
    public List<BookedRoom> getBookedRoomsByUsername(String username) {
        return bookedRoomRepo.findByBookedBy(username);
    }


    @Override
    public void deleteBookedRoom(Long id) {
        if (!bookedRoomRepo.existsById(id)) {
            throw new RuntimeException("Booked Room not found");
        }
        bookedRoomRepo.deleteById(id);
    }
    
    @Override
    public DashBoard addEntry(DashBoard dashBoard, String username) {
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        dashBoard.setUsers(user);
        return dashBoardRepo.save(dashBoard);
    }

    @Override
    public List<DashBoard> getEntriesByUsername(String username) {
        return dashBoardRepo.findByUsersUsername(username);
    }

    @Override
    public void deleteEntry(Long id) {
        if (dashBoardRepo.existsById(id)) {
            dashBoardRepo.deleteById(id);
        } else {
            throw new RuntimeException("Entry not found");
        }
    }
    
    // New time-based filtering methods
    @Override
    public List<RoomPost> getActiveRoomPosts(LocalDate date) {
        return roomPostRepository.findByDateGreaterThanEqualOrderByDateAsc(date);
    }
    
    @Override
    public List<RoomRequest> getActiveRoomRequests(LocalDate date) {
        return roomRequestRepository.findByDateGreaterThanEqualOrderByDateAsc(date);
    }
    
    @Override
    public List<RoomPost> getPastRoomPosts(LocalDate date) {
        return roomPostRepository.findByDateLessThanOrderByDateDesc(date);
    }
    
    @Override
    public List<RoomRequest> getPastRoomRequests(LocalDate date) {
        return roomRequestRepository.findByDateLessThanOrderByDateDesc(date);
    }
    
    // Admin statistics methods
    @Override
    public Map<String, Long> getRoomPostsCountByUser() {
        List<RoomPost> allPosts = roomPostRepository.findAll();
        return allPosts.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    RoomPost::getPostedBy,
                    java.util.stream.Collectors.counting()
                ));
    }
    
    @Override
    public Map<String, Long> getRoomRequestsCountByUser() {
        List<RoomRequest> allRequests = roomRequestRepository.findAll();
        return allRequests.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    RoomRequest::getPostedBy,
                    java.util.stream.Collectors.counting()
                ));
    }

}
