package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.GroupMemberStatus;
import com.server.springboot.domain.enumeration.GroupPermissionType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

@Entity
@Table(name = "group_member")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_member_id")
    private Long groupMemberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_permission_type")
    private GroupPermissionType groupPermissionType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "group_member_status", nullable = false)
    private GroupMemberStatus groupMemberStatus;

    @Column(name = "added_in")
    private LocalDateTime addedIn;

    @NotNull
    @Column(name = "invitation_displayed")
    private boolean invitationDisplayed;

    @Column(name = "invitation_date")
    private LocalDateTime invitationDate;

    @NotNull
    @Column(name = "has_notification", nullable = false)
    private boolean hasNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @OneToMany(mappedBy = "threadAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupThread> groupThreads;

    @OneToMany(mappedBy = "answerAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ThreadAnswer> threadAnswers;

    @OneToMany(mappedBy = "answerReviewAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ThreadAnswerReview> answerReviews;

}
