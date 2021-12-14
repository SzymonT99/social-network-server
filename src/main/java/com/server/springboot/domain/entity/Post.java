package com.server.springboot.domain.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User postAuthor;

    @OneToMany(mappedBy = "basePost", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SharedPost> sharedBasePosts;

    @OneToOne(mappedBy = "newPost", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private SharedPost sharedNewPosts;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<LikedPost> likedPosts;

    @OneToMany(mappedBy = "commentedPost", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToMany(mappedBy = "favouritePosts")
    private Set<User> favourites;

    @ManyToMany
    @JoinTable(
            name = "post_images",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "image_id"))
    private Set<Image> images;

    public void removeImages() {
        this.images.clear();
    }

}
