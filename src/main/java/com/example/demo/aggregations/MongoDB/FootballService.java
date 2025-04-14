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
import com.example.demo.aggregations.MongoDB.DTO.DreamTeamPlayer;
import com.example.demo.aggregations.MongoDB.DTO.TopPlayersByCoach;
import java.util.Arrays;
import java.util.List;


@Service
public class FootballService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Return the top 10 clubs by average overall rating,
     * grouped by club name     */
    public List<ClubAverage> getTopClubsByAverageOverall() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.unwind("fifaStats"),
                // Group by team_name, compute average and max club worth
                Aggregation.group("team_name")
                        .avg("fifaStats.overall").as("averageOverall")
                        .avg("fifaStats.club_worth_eur").as("maxClubWorth"),
                Aggregation.project("averageOverall", "maxClubWorth")
                        .and("_id").as("clubName"), // pull team name from _id, i.e. the group key
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
    
    public List<DreamTeamPlayer> getDreamTeam(Integer fifaVersion) {
        // Define the positions to select 
        List<String> dreamTeamPositions = Arrays.asList(
        "GK", "RB", "CB", "LB",  // Defenders
        "CDM", "CM", "CAM",      // Midfielders
        "RW", "LW", "ST"         // Attackers
    );
    
        Aggregation aggregation = Aggregation.newAggregation(
            // Step 1: Unwind the fifaStats array to process each player's stats
            Aggregation.unwind("fifaStats"),
    
            // Step 2: Match based on the fifa_version and only include players with the required positions
            Aggregation.match(Criteria.where("fifaStats.fifa_version").is(fifaVersion)
                    .and("fifaStats.player_positions").in(dreamTeamPositions)),
    
            // Step 3: Project the necessary fields for each player (name, position, overall, player_id, fifa_version)
            Aggregation.project("long_name")
                .and("fifaStats.player_positions").as("position")
                .and("fifaStats.overall").as("overall")
                .and("fifaStats.fifa_version").as("fifaVersion")
                .and("player_id").as("playerId"),
    
            // Step 4: Sort by overall rating in descending order (so best players come first)
            Aggregation.sort(Sort.by(Sort.Direction.DESC, "overall")),
    
            // Step 5: Group by position and select the best player for each position
            Aggregation.group("position")
                .first("long_name").as("playerName")
                .first("overall").as("overall")
                .first("playerId").as("playerId")
                .first("fifaVersion").as("fifaVersion")
                .first("position").as("position"),
    
            // Step 6: Project the final output with player name, overall, position, and fifaVersion
            Aggregation.project("playerName", "overall", "fifaVersion", "position")
        );
    
        // Execute the aggregation query
        AggregationResults<DreamTeamPlayer> result = mongoTemplate.aggregate(
            aggregation, "Players", DreamTeamPlayer.class
        );
    
        // Return the list of the best players for the selected positions
        return result.getMappedResults();
    }

}



