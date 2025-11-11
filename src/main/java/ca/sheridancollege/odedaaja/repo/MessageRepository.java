package ca.sheridancollege.odedaaja.repo;

import ca.sheridancollege.odedaaja.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.timestamp ASC")
    List<Message> findMessagesBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :receiverId AND m.isRead = false ORDER BY m.timestamp DESC")
    List<Message> findUnreadMessagesByReceiverId(@Param("receiverId") Long receiverId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :receiverId AND m.isRead = false")
    int countUnreadMessagesByReceiverId(@Param("receiverId") Long receiverId);
}