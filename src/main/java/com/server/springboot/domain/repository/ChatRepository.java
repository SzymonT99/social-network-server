package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Chat;
import com.server.springboot.domain.entity.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    boolean existsByChatId(Long chatId);

    List<Chat> findByChatMembersInAndIsPrivate(List<ChatMember> chatMembers, boolean isPrivate);
}
