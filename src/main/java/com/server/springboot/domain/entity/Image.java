package com.server.springboot.domain.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

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
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String imageId;

    @NotNull
    @Column(name = "filename", nullable = false)
    private String filename;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "filePath", nullable = false)
    private String filePath;

    @Column(name = "caption")
    private String caption;

    @NotNull
    @Column(name = "added_in", nullable = false)
    private LocalDateTime addedIn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    @ManyToMany(mappedBy = "images")
    private Set<Post> posts;

    @OneToOne(mappedBy = "image", fetch = FetchType.LAZY)
    private Event event;
}
