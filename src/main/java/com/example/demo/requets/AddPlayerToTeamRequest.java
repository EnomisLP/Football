package com.example.demo.requets;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddPlayerToTeamRequest {
    @NotNull
    private Long playerId;

    @NotNull
    private Integer fifaValue;
}
