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
@Table(name = "shared_post")
public class SharedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shared_post_id")
    private Long sharedPostId;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sharedPostUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_post_id", nullable = false)
    private Post basePost;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "new_post_id", nullable = false)
    private Post newPost;

    @NotNull
    @Column(name = "is_post_author_notified", nullable = false)
    private boolean isPostAuthorNotified;
}

