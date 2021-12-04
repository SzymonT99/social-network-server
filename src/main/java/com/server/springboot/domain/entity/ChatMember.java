package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "chat_member")
public class ChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_member_id")
    private Long chatMemberId;

    @NotNull
    @Column(name = "added_in", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "has_muted_chat", nullable = false)
    private boolean hasMutedChat;

    @NotNull
    @Column(name = "has_unread_message", nullable = false)
    private boolean hasUnreadMessage;

    @NotNull
    @Column(name = "can_add_others", nullable = false)
    private boolean canAddOthers;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User chatMember;

}
