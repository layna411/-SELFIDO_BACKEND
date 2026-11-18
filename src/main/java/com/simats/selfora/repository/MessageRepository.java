package com.simats.selfora.repository;

import com.simats.selfora.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.child.id = :childId ORDER BY m.sentAt ASC, m.id ASC")
    List<Message> findConversationByChildId(@Param("childId") Long childId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.child.id = :childId AND m.receiver.id = :receiverId AND (m.isRead = false OR m.isRead IS NULL)")
    int markUnreadAsRead(@Param("childId") Long childId, @Param("receiverId") Long receiverId);
}
