package com.server.springboot.domain.entity;

import com.server.springboot.domain.entity.key.UserEventKey;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

@Entity
@Table(name = "shared_event")
public class SharedEvent {

    @EmbeddedId
    UserEventKey id;

    @NotNull
    @Column(name = "date",  nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User sharedEventUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}
