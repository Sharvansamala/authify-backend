package com.sharvan.authify.io;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @Email(message = "Email valid email address")
    @NotNull(message = "Email cannot be empty")
    private String email;
    @Size(min = 6,message = "Must be of length 6")
    private String password;
}
