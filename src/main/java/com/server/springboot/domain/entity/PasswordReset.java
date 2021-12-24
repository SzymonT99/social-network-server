package com.server.springboot.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

@Entity
@Table(name = "password_reset")
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "password_reset_id")
    private Long passwordResetId;

    @NotNull
    @Column(name = "reset_code", nullable = false)
    private String resetCode;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    public PasswordReset(String resetCode, User user, int tokenTime) {
        this.resetCode = resetCode;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plus(tokenTime, ChronoField.MILLI_OF_DAY.getBaseUnit());
        this.user = user;
    }
}
