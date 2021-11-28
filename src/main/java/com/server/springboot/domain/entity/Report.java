package com.server.springboot.domain.entity;

import com.server.springboot.domain.enumeration.ReportType;
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
@Table(name = "report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @NotNull
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "description")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @NotNull
    @Column(name = "is_confirmed", nullable = false)
    private boolean isConfirmed;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "suspect_id", nullable = false)
    private User suspect;

}
