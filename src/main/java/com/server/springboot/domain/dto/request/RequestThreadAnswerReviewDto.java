package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestThreadAnswerReviewDto {

    @NotNull
    private Float rate;
}
