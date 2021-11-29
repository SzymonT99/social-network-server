package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
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
    private boolean isInvitationAccepted;

    @NotNull
    @Column(name = "invitation_displayed", nullable = false)
    private boolean invitationDisplayed;

    @NotNull
    @Temporal(TemporalType.DATE)
    @Column(name = "invitation_date", nullable = false)
    private Date invitationDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "friend_from_date")
    private Date friendFromDate;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_friend_id", nullable = false)
    private User userFriend;

}
