package com.example.demo.requets;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
