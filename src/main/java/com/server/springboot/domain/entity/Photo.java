package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "photo")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "photo_id")
    private Long photoId;

    @NotNull
    @Column(name = "name", nullable = false)
    private String filename;

    @Column(name = "caption")
    private String caption;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "added_in", nullable = false)
    private Date addedIn;

    @NotNull
    @Column(name = "is_profile_photo", nullable = false)
    private boolean isProfilePhoto;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToMany(mappedBy = "photos")
    private List<Post> posts;

}
