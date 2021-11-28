package com.server.springboot.domain.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @NotNull
    @Column(name = "title", nullable = false, length = 30)
    @Size(max = 30)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "image")
    private String image;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "event_date", nullable = false)
    private Date eventDate;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @NotNull
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User eventCreator;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_address_id", nullable = false)
    private Address eventAddress;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<EventMember> members;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private Set<SharedEvent> sharing;

}
