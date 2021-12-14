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
@Table(name = "account_verification")
public class AccountVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @NotNull
    @Column(name = "verification_code", nullable = false)
    private String verificationCode;

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

    public AccountVerification(User user, String verificationCode, int tokenTime) {
        this.verificationCode = verificationCode;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = LocalDateTime.now().plus(tokenTime, ChronoField.MILLI_OF_DAY.getBaseUnit());
        this.user = user;
    }
}
