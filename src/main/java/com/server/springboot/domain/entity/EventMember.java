package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.EventParticipationStatus;
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
@Table(name = "event_member")
public class EventMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_member_id")
    private Long eventMemberId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "participation_status", nullable = false)
    private EventParticipationStatus participationStatus;

    @Column(name = "added_in")
    private LocalDateTime addedIn;

    @Column(name = "invitation_displayed")
    private boolean invitationDisplayed;

    @Column(name = "invitation_date")
    private LocalDateTime invitationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User eventMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

}
