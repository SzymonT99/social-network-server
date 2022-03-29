package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Chat;
import com.server.springboot.domain.entity.ChatMember;
import com.server.springboot.domain.entity.ChatMessage;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Integer countAllByMessageChatAndMessageAuthorInAndCreatedAtGreaterThan(Chat chat, List<User> authors, LocalDateTime date);
}
