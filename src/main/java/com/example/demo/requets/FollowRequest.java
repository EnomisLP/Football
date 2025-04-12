package com.example.demo.requets;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FollowRequest {
    @NotBlank
    private String follower;

    @NotBlank
    private String followee;
}
