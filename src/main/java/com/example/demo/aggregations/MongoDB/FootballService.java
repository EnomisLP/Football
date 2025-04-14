package com.example.demo.aggregations.MongoDB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.example.demo.aggregations.MongoDB.DTO.ClubAverage;
import com.example.demo.aggregations.MongoDB.DTO.TopPlayersByCoach;
import com.example.demo.aggregations.MongoDB.DTO.TopUsedPlayers;

import java.util.Arrays;
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
            // Step 1: Unwind fifaStats to filter by coach
            Aggregation.unwind("fifaStats"),
    
            // Step 2: Match only entries with the given coach
            Aggregation.match(Criteria.where("fifaStats.coach_id").is(coachId)),
    
            // Step 3: Create player_ids array from role fields
            Aggregation.project("fifaStats.fifa_version", "fifaStats.coach_id", "fifaStats.team_name")
                .and(ArrayOperators.ConcatArrays.arrayOf(
                    Arrays.asList(
                        Fields.field("fifaStats.short_free_kick"),
                        Fields.field("fifaStats.long_free_kick"),
                        Fields.field("fifaStats.left_short_free_kick"),
                        Fields.field("fifaStats.right_short_free_kick"),
                        Fields.field("fifaStats.penalties"),
                        Fields.field("fifaStats.left_corner"),
                        Fields.field("fifaStats.right_corner"),
                        Fields.field("fifaStats.captain")
                    )
                )).as("player_ids")
                .and("fifaStats.fifa_version").as("fifa_version")
                .and("fifaStats.team_name").as("team_name"),
    
            // Step 4: Unwind player_ids to access each player
            Aggregation.unwind("player_ids"),
    
            // Step 5: Lookup in Players collection
            Aggregation.lookup("Players", "player_ids", "player_id", "player"),
    
            Aggregation.unwind("player"),
            Aggregation.unwind("player.fifaStats"),
    
            // Step 6: Create flattened result for scoring
            Aggregation.project()
                .and("player.long_name").as("playerName")
                .and("player.fifaStats.overall").as("overall")
                .and("player.fifaStats.fifa_version").as("fifaVersion")
                .and("player.fifaStats.club_name").as("teamName")
                .and("player.player_id").as("playerId"),
    
            // Step 7: Group by playerId to get max overall per player
            Aggregation.group("playerId")
                .first("playerName").as("playerName")
                .first("fifaVersion").as("fifaVersion")
                .first("teamName").as("teamName")
                .max("overall").as("overall"),
    
            // Step 8: Sort & Limit
            Aggregation.sort(Sort.by(Sort.Order.desc("overall"))),
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
