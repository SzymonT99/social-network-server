package com.server.springboot.domain.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UserAccountPageDto {
    private List<UserAccountDto> userAccounts;
    private Integer currentPage;
    private Long totalItems;
    private Integer totalPages;
}
