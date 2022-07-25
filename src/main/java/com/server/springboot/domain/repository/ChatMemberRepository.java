package com.server.springboot.domain.repository;

import com.server.springboot.domain.entity.Chat;
import com.server.springboot.domain.entity.ChatMember;
import com.server.springboot.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

    List<ChatMember> findByUserMember(User user);

    Optional<ChatMember> findByUserMemberAndChat(User user, Chat chat);

    boolean existsByChatAndUserMember(Chat chat, User user);
}
