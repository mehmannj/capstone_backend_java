package ca.sheridancollege.odedaaja.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import ca.sheridancollege.odedaaja.domain.BookedRoom;
import ca.sheridancollege.odedaaja.domain.DashBoard;
import ca.sheridancollege.odedaaja.domain.RoomPost;
import ca.sheridancollege.odedaaja.domain.RoomRequest;
import ca.sheridancollege.odedaaja.domain.Users;

public interface Prof_RoomBook_Service {

    // User methods
    List<Users> getAllUsers();
    Users saveUser(Users user);
    Users getUserById(Long id);
    Users updateUser(Long id, Users user);
    void deleteUser(Long id);
    Users patchUser(Long id, Map<String, Object> updates);

    // RoomPost methods
    List<RoomPost> getAllPostedRooms();
    RoomPost saveRoom(RoomPost roomPost);
    RoomPost getRoomPostById(Long id);
    RoomPost updateRoomPost(Long id, RoomPost roomPost);
    void deleteRoomPost(Long id);

    // RoomRequest methods
    List<RoomRequest> getAllRoomRequests();
    RoomRequest saveRoomRequest(RoomRequest roomRequest);
    RoomRequest getRoomRequestById(Long id);
    RoomRequest updateRoomRequest(Long id, RoomRequest roomRequest);
    void deleteRoomRequest(Long id);
    
	List<RoomPost> getRoomPostsByUsername(String username);
	
	List<RoomRequest> getRoomRequestsByUsername(String username);
	
	// New methods for time-based filtering
	List<RoomPost> getActiveRoomPosts(LocalDate date);
	List<RoomRequest> getActiveRoomRequests(LocalDate date);
	List<RoomPost> getPastRoomPosts(LocalDate date);
	List<RoomRequest> getPastRoomRequests(LocalDate date);
	
	// Admin statistics methods
	Map<String, Long> getRoomPostsCountByUser();
	Map<String, Long> getRoomRequestsCountByUser();
	
	
	    BookedRoom bookRoom(BookedRoom bookedRoom, String username);
	    List<BookedRoom> getAllBookedRooms();
	    BookedRoom getBookedRoomById(Long id);
	    List<BookedRoom> getBookedRoomsByUsername(String username);
	    void deleteBookedRoom(Long id);
	    
	    
	    
	    
	    DashBoard addEntry(DashBoard dashBoard, String username);
	    List<DashBoard> getEntriesByUsername(String username);
	    void deleteEntry(Long id);
	    
	    

}
