package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.FavouriteType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "user_favourite")
public class UserFavourite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_favourite_id")
    private Long userFavouriteId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "favourite_type", nullable = false)
    private FavouriteType favouriteType;

    @NotNull
    @Column(name = "name", nullable = false, length = 30)
    @Size(max = 30)
    private String name;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

}
