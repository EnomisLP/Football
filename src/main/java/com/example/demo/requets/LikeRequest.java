package com.example.demo.requets;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikeRequest {
    @NotNull
    private Long targetId; // can be teamId or coachId
}
