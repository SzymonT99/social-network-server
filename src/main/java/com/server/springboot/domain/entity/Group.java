package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "thematic_group")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @NotNull
    @Column(name = "name", nullable = false, length = 30)
    @Size(max = 30)
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "description")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User groupCreator;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<GroupRule> groupRules;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<GroupMember> groupMembers;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Thread> threads;

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<Post> posts;

    @ManyToMany
    @JoinTable(
            name = "group_interest",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "interest_id"))
    private List<Interest> groupInterests;

}
