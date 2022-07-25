package com.server.springboot.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class ProfilePhotoDto {
    private String filename;
    private String url;
    private String type;
    private String addedIn;
}
