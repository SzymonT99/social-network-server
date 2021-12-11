package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
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
@Table(name = "post")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "text")
    private String text;

    @NotNull
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @NotNull
    @Column(name = "is_edited", nullable = false)
    private boolean isEdited;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User postAuthor;

    @OneToMany(mappedBy = "basePost", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<SharedPost> sharedBasePosts;

    @OneToMany(mappedBy = "newPost", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<SharedPost> sharedNewPosts;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<LikedPost> likedPosts;

    @OneToMany(mappedBy = "commentedPost", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Comment> comments;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToMany(mappedBy = "favouritePosts")
    private List<User> favourites;

    @ManyToMany
    @JoinTable(
            name = "post_images",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id"))
    private List<Image> images;

}
