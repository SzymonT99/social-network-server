package com.server.springboot.domain.dto.response;

import com.server.springboot.domain.enumeration.FavouriteType;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class UserFavouriteDto {
    private Long favouriteId;
    private FavouriteType favouriteType;
    private String name;
}
