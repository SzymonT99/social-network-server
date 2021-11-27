package com.server.springboot.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.server.springboot.domain.enumeration.ActivityStatus;
import com.server.springboot.domain.enumeration.Role;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

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
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @NotNull
    @Column(name = "username")
    @Size(max = 20)
    private String username;

    @NotNull
    @Column(name = "password")
    @Size(max = 100)
    private String password;

    @NotNull
    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "incorrect_login_counter")
    private Integer incorrectLoginCounter;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "verified_account")
    private boolean verifiedAccount;

    @NotNull
    @Column(name = "activity_status")
    private ActivityStatus activityStatus;

    @NotNull
    @Column(name = "is_blocked")
    private boolean isBlocked;

    @NotNull
    @Column(name = "is_deleted")
    private boolean isDeleted;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

}
