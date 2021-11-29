package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.EventParticipationStatus;
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
@Table(name = "event_member")
public class EventMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_member_id")
    private Long eventMemberId;

    @NotNull
    @Column(name = "participation_status", nullable = false)
    private EventParticipationStatus participationStatus;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "added_in")
    private Date addedIn;

    @NotNull
    @Column(name = "invitation_displayed", nullable = false)
    private boolean invitationDisplayed;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User eventMember;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

}
