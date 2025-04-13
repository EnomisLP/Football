package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.example.demo.aggregations.MongoDB.DTO.ClubAverage;
import com.example.demo.aggregations.MongoDB.DTO.TopPlayersByCoach;
import com.example.demo.aggregations.MongoDB.DTO.TopUsedPlayers;

import java.util.List;

@Service
public class FootballService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Return the top 10 clubs by average overall rating,
     * grouped by club name and FIFA version.
     */
    public List<ClubAverage> getTopClubsByAverageOverall() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("fifaStats"),
                // Group by team_name and fifa_version, compute average and max club worth
                Aggregation.group("team_name", "fifaStats.fifa_version")
                        .avg("fifaStats.overall").as("averageOverall")
                        .max("fifaStats.club_worth_eur").as("maxClubWorth"),
                Aggregation.project("averageOverall", "maxClubWorth")
                        .and("_id.team_name").as("clubName")
                        .and("_id.fifa_version").as("fifaVersion"),
                Aggregation.sort(Sort.by(
                        Sort.Order.desc("averageOverall"),
                        Sort.Order.desc("maxClubWorth")
                )),
                Aggregation.limit(10)
        );
    
        AggregationResults<ClubAverage> results = mongoTemplate.aggregate(
                aggregation, "Teams", ClubAverage.class
        );
    
        return results.getMappedResults();
    }
    
    

    /**
     * Return the top 10 best players managed by a specific coach,
     * including the FIFA version of the corresponding stat.
     */
    public List<TopPlayersByCoach> getTopPlayersManagedByCoach(int coachId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("coach_id").is(coachId)),
                Aggregation.project("long_name")
                        .and("fifaStats.overall").as("overall")
                        .and("fifaStats.coach_id").as("coachId")
                        .and("fifaStats.fifa_version").as("fifaVersion")
                        .and("long_name").as("playerName"),
                Aggregation.sort(Sort.by(
                        Sort.Order.desc("overall")
                )),
                Aggregation.limit(10)
        );

        AggregationResults<TopPlayersByCoach> results = mongoTemplate.aggregate(
                aggregation, "Teams", TopPlayersByCoach.class
        );
        return results.getMappedResults();
    }

    /**
     * Return the top 10 players based on usage, overall, and value,
     * and include the maximum FIFA version from their fifaStats.
     * "Usage" is computed as the size of the fifaStats array.
     */
    public List<TopUsedPlayers> getTopPlayersByUsageOverallAndValue() {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.project("long_name", "fifaStats")
                .andExpression("size(fifaStats)").as("usage")
                .andExpression("max(fifaStats.overall)").as("maxOverall")
                .andExpression("max(fifaStats.value_eur)").as("maxValue")
                .andExpression("max(fifaStats.fifa_version)").as("fifaVersion"),
            
            Aggregation.sort(Sort.by(
                Sort.Order.desc("usage"),
                Sort.Order.desc("maxOverall"),
                Sort.Order.desc("maxValue")
            )),
    
            Aggregation.limit(10),
    
            Aggregation.project("long_name", "usage", "maxOverall", "maxValue", "fifaVersion")
        );
    
        AggregationResults<TopUsedPlayers> results = mongoTemplate.aggregate(
            aggregation, "Players", TopUsedPlayers.class
        );
    
        return results.getMappedResults();
    }
    
    
}
