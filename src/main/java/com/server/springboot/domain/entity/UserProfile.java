package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.Gender;
import com.server.springboot.domain.enumeration.RelationshipStatus;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_profile_id")
    private Long userProfileId;

    @NotNull
    @Column(name = "is_public", nullable = false)
    private boolean isPublic;

    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    @Size(max = 100)
    private String firstName;

    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    @Size(max = 100)
    private String lastName;

    @Column(name = "about_user")
    private String aboutUser;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @NotNull
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull
    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "job")
    private String job;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_status")
    private RelationshipStatus relationshipStatus;

    @Column(name = "skills")
    private String skills;

    @OneToOne(mappedBy = "userProfile", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<School> schools;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<UserFavourite> favourites;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<WorkPlace> workPlaces;

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Image> userImages;

}
