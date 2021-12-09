package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.ActivityStatus;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    @Column(name = "username", nullable = false, length = 20)
    @Size(min = 6, max = 20)
    private String username;

    @NotNull
    @Column(name = "password", nullable = false, length = 100)
    @Size(max = 100)
    private String password;

    @NotNull
    @Email
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "incorrect_login_counter")
    private Integer incorrectLoginCounter;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "verified_account")
    private boolean verifiedAccount;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "activity_status")
    private ActivityStatus activityStatus;

    @NotNull
    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", orphanRemoval = true)
    private RefreshToken refreshToken;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<AccountVerification> verificationCodes;

    @OneToMany(mappedBy = "postAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Post> posts;

    @OneToMany(mappedBy = "commentAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Comment> comments;

    @OneToMany(mappedBy = "sharedPostUser", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<SharedPost> sharedPosts;

    @OneToMany(mappedBy = "likedPostUser", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<LikedPost> likedPosts;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Friend> friends;

    @OneToMany(mappedBy = "userFriend", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Friend> userFriends;

    @OneToMany(mappedBy = "chatCreator", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Chat> createdChats;

    @OneToMany(mappedBy = "messageAuthor", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Message> messages;

    @OneToMany(mappedBy = "chatMember", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<ChatMember> memberOfChats;

    @OneToMany(mappedBy = "eventCreator", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Event> createdEvents;

    @OneToMany(mappedBy = "eventMember", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<EventMember> memberOfEvents;

    @OneToMany(mappedBy = "sharedEventUser", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<SharedEvent> sharedEvents;

    @OneToMany(mappedBy = "suspect", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Report> reports;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Activity> activities;

    @OneToMany(mappedBy = "groupCreator", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Group> createdGroups;

    @OneToMany(mappedBy = "groupMember", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<GroupMember> memberOfGroups;

    @ManyToMany
    @JoinTable(
            name = "user_interest",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private List<Interest> userInterests;

    @ManyToMany
    @JoinTable(
            name = "favourite_post",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id"))
    private List<Post> favouritePosts;

    @ManyToMany
    @JoinTable(
            name = "liked_comment",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "comment_id"))
    private List<Comment> likedComments;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

}
