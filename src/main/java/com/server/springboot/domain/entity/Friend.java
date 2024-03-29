package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

@Entity
@Table(name = "friend")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long friendId;

    @Column(name = "is_invitation_accepted")
    private Boolean isInvitationAccepted;

    @NotNull
    @Column(name = "invitation_displayed", nullable = false)
    private boolean invitationDisplayed;

    @Column(name = "invitation_date")
    private LocalDateTime invitationDate;

    @Column(name = "friend_from_date")
    private LocalDateTime friendFromDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_friend_id", nullable = false)
    private User userFriend;

    @Column(name = "is_user_notified_about_accepting")
    private boolean isUserNotifiedAboutAccepting;
}
