package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "interest")
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long interestId;

    @NotNull
    @Column(name = "name", nullable = false, length = 40)
    @Size(max = 40)
    private String name;

    @ManyToMany(mappedBy = "userInterests")
    private List<User> interestedUsers;

    @ManyToMany(mappedBy = "groupInterests")
    private List<Group> interestedGroup;

}
