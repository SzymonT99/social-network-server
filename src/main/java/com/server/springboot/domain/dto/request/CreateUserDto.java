package com.server.springboot.domain.dto.request;

import com.server.springboot.domain.enumeration.Gender;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class CreateUserDto {

    @NotEmpty
    @Size(min = 6, max = 20)
    private String username;

    @NotEmpty
    @Size(min = 10, max = 100)
    private String password;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    @Positive
    @Size(max = 9)
    private String phoneNumber;

    @NotEmpty
    @Size(max = 100)
    private String firstName;

    @NotEmpty
    @Size(max = 100)
    private String lastName;

    @NotNull
    private Gender gender;

    @NotEmpty
    private String dateOfBirth;

}
