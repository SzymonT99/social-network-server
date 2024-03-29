package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
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
    private LocalDateTime addedIn;

    @NotNull
    @Column(name = "last_activity_date", nullable = false)
    private LocalDateTime lastActivityDate;

    @NotNull
    @Column(name = "has_muted_chat", nullable = false)
    private boolean hasMutedChat;

    @Column(name = "can_add_others")
    private Boolean canAddOthers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User userMember;

}
