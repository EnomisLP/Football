package com.example.demo.models.MongoDB;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.neo4j.core.schema.GeneratedValue;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Data
@Document(collection ="Players")
public class Players {

    @Id
    @GeneratedValue
    private String _id;
    @Field("short_name")
    private  String short_name;
    @Field("long_name")
    private String long_name;
    @Field("age")
    private Integer age;
    @Field("dob")
    private Date dob;
    @Field("nationality_id")
    private Long nationality_id;
    @Field("nationality_name")
    private String nationality_name;
    @Field("height_cm")
    private Integer height_cm;
    @Field("weight_kg")
    private Integer weight_kg;
    
    private List<FifaStatsPlayer> fifaStats;
    @Field("gender")
    @Indexed
    private String gender;

    //Cunstructors, Getters, Setters automatically generated
  public Players() {
    this.fifaStats = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        FifaStatsPlayer stats = new FifaStatsPlayer();
        stats.setFifa_version(99);
        stats.setPlayer_positions("NA");
        stats.setOverall(-1);
        stats.setPotential(-1);
        stats.setValue_eur((long) -1);
        stats.setClub_position("NA");
        stats.setClub_jersey_number(-1);
        stats.setClub_contract_valid_until_year(2999);
        stats.setLeague_name("DefaultLeague");
        stats.setLeague_level(-1);

        TeamObj team = new TeamObj();
        team.setTeam_mongo_id("XXXXXXXXXXXX");
        team.setTeam_name("DefaultTeam");
        stats.setTeam(team);

        stats.setPace(-1);
        stats.setShooting(-1);
        stats.setPassing(-1);
        stats.setDribbling(-1);
        stats.setDefending(-1);
        stats.setPhysic(-1);

        stats.setAttacking_crossing(-1);
        stats.setAttacking_finishing(-1);
        stats.setAttacking_heading_accuracy(-1);
        stats.setAttacking_short_passing(-1);
        stats.setAttacking_volleys(-1);

        stats.setSkill_dribbling(-1);
        stats.setSkill_curve(-1);
        stats.setSkill_fk_accuracy(-1);
        stats.setSkill_long_passing(-1);
        stats.setSkill_ball_control(-1);

        stats.setMovement_acceleration(-1);
        stats.setMovement_sprint_speed(-1);
        stats.setMovement_agility(-1);
        stats.setMovement_reactions(-1);
        stats.setMovement_balance(-1);

        stats.setPower_shot_power(-1);
        stats.setPower_jumping(-1);
        stats.setPower_stamina(-1);
        stats.setPower_strength(-1);
        stats.setPower_long_shots(-1);

        stats.setMentality_aggression(-1);
        stats.setMentality_interceptions(-1);
        stats.setMentality_positioning(-1);
        stats.setMentality_vision(-1);
        stats.setMentality_penalties(-1);

        stats.setDefending_marking_awareness(-1);
        stats.setDefending_standing_tackle(-1);
        stats.setDefending_sliding_tackle(-1);

        stats.setGoalkeeping_diving(-1);
        stats.setGoalkeeping_handling(-1);
        stats.setGoalkeeping_kicking(-1);
        stats.setGoalkeeping_positioning(-1);
        stats.setGoalkeeping_reflexes(-1);

        this.fifaStats.add(stats);
    }
}

}
