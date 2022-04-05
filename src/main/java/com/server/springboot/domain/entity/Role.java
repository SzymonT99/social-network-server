package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.AppRole;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

@Entity
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "name",length = 20, nullable = false)
    private AppRole name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users;

    public Role(Integer roleId, AppRole name) {
        this.roleId = roleId;
        this.name = name;
    }
}
