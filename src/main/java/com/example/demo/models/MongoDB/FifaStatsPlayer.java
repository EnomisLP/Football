package com.example.demo.models.MongoDB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class FifaStatsPlayer {
    public FifaStatsPlayer() {
    }

    private Integer fifa_version;
    private String player_positions;
    private Integer overall;
    private Integer potential;
    private Long value_eur;
    private Long wage_eur;
    private String club_position;
    private Integer club_jersey_number;
    private Integer club_contract_valid_until_year;
    private String league_name;
    private Integer league_level;
    private Integer pace;
    private Integer shooting;
    private Integer passing;
    private Integer defending;
    private Integer physic;
    private Integer attacking_crossing;
    private Integer attacking_finishing;
    private Integer attacking_heading_accuracy;
    private Integer attacking_short_passing;
    private Integer attacking_volleys;
    private Integer skill_dribbling;
    private Integer skill_curve;
    private Integer skill_fk_accuracy;
    private Integer skill_long_passing;
    private Integer skill_ball_control;
    private Integer movement_acceleration;
    private Integer movement_agility;
    private Integer movement_reactions;
    private Integer movement_balance;
    private Integer power_shot_power;
    private Integer power_jumping;
    private Integer power_stamina;
    private Integer power_strength;
    private Integer power_long_shots;
    private Integer mentality_aggression;
    private Integer mentality_interceptions;
    private Integer mentality_positioning;
    private Integer mentality_vision;
    private Integer mentality_penalties;
    private Integer defending_marking_awareness;
    private Integer defending_standing_tackle;
    private Integer defending_sliding_tackle;
    private Integer goalkeeping_diving;
    private Integer goalkeeping_handling;
    private Integer goalkeeping_kicking;
    private Integer goalkeeping_positioning;
    private Integer goalkeeping_reflexes;
    private TeamObj team;
}
