package com.server.springboot.domain.entity;

import com.server.springboot.domain.entity.key.UserPostKey;
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
@Table(name = "liked_post")
public class LikedPost {

    @EmbeddedId
    UserPostKey id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User likedPostUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("postId")
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
}