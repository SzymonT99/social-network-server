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
    @NotNull
    @Column(name = "group_permission_type", nullable = false)
    private GroupPermissionType groupPermissionType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "group_member_status", nullable = false)
    private GroupMemberStatus groupMemberStatus;

    @Column(name = "added_in")
    private LocalDateTime addedIn;

    @NotNull
    @Column(name = "invitation_displayed", nullable = false)
    private boolean invitationDisplayed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User groupMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @OneToMany(mappedBy = "threadAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Thread> threads;

    @OneToMany(mappedBy = "answerAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ThreadAnswer> threadAnswers;

    @OneToMany(mappedBy = "answerReviewAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ThreadAnswerReview> answerReviews;

}
