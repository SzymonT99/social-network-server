package com.server.springboot.domain.entity.key;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
@Embeddable
public class UserEventKey implements Serializable {

    @Column(name = "user_id")
    Long userId;

    @Column(name = "event_id")
    Long eventId;
}
