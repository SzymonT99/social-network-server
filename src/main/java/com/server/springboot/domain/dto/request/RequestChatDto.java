package com.server.springboot.domain.dto.request;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class RequestChatDto {

    @NotEmpty
    @Size(max = 30)
    private String name;

    @NotNull
    private boolean isPrivate;

    private List<Long> addedUsersId;
}
