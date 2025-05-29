package com.example.demo.aggregations.DTO;
import com.example.demo.DTO.PlayersNodeDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MostEngaggedPlayer {
    private PlayersNodeDTO player;
    private Integer totalEngagement;
}
