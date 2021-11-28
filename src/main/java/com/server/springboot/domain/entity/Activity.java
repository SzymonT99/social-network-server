package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.ActivityType;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder

@Entity
@Table(name = "activity")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;

    @NotNull
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "activity_date", nullable = false)
    private Date activityDate;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
