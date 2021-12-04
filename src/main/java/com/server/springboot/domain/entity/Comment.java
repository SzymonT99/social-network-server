package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "post_comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @NotNull
    @Column(name = "text", nullable = false)
    private String text;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User commentAuthor;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post commentedPost;

    @ManyToMany(mappedBy = "likedComments")
    private List<User> likes;

}
