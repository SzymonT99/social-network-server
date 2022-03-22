package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    boolean existsByChatId(Long chatId);
}
