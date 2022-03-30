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
public class PostsPageDto {
    private List<PostDto> posts;
    private Integer currentPage;
    private Long totalItems;
    private Integer totalPages;
}
