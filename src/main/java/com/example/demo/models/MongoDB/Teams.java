package com.example.demo.models.MongoDB;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
@Document(collection ="Teams")

public class Teams {

    @Id
    @GeneratedValue
    private String _id;
    @Field("team_name")
    private String team_name;

    private List<FifaStatsTeam> fifaStats;
    @Field("league_name")
    private String league_name;
    @Field("league_level")
    private Integer league_level;
    @Field("nationality_name")
    private String nationality_name;
    @Field("gender")
    private String gender;
    //Constructor, Getters, Setters automatically generated

    public Teams() {
    this.fifaStats = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
        FifaStatsTeam stats = new FifaStatsTeam();
        stats.setFifa_version(-1);
        stats.setHome_stadium("DefaultStadiumName");
        stats.setOverall(-1);
        stats.setAttack(-1);
        stats.setMidfield(-1);
        stats.setDefence(-1);
        stats.setClub_worth_eur((long) -1);

        CoachObj coach = new CoachObj();
        coach.setCoach_mongo_id("XXXXXXXXXXXX");
        coach.setCoach_name("DefaultCoachName");
        stats.setCoach(coach);

        this.fifaStats.add(stats);
    }
}

}
