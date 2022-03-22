package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Chat;
import com.server.springboot.domain.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Integer countAllByMessageChatAndCreatedAtGreaterThan(Chat chat, LocalDateTime date);
}
