package com.example.demo.services.MongoDB;
import java.util.List;
import java.util.Optional;
import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsPlayer;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.Players;
import com.example.demo.models.MongoDB.TeamObj;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.models.Neo4j.PlayersNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.relationships.manages_team;
import com.example.demo.relationships.plays_in_team;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Players_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Players_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.requets.updateTeam;
import com.example.demo.requets.updateFifaTeam;
import com.example.demo.requets.createTeamRequest;
import com.example.demo.requets.updateCoachTeam;

import jakarta.transaction.Transactional;
@Service
public class Teams_service {
    
    private Teams_repository TMr;
    private Teams_node_rep Tmr;
    private static final Integer CURRENT_YEAR = 24;
    private Players_repository PMr;
    private Coaches_repository CMr;
    private Coaches_node_rep CMn;
    private Players_node_rep Pmn;

    public Teams_service(Teams_repository tmr, Teams_node_rep TMR,
    Players_repository pmr, Coaches_repository cmr, Coaches_node_rep CMn,
    Players_node_rep pmn) {
        this.TMr = tmr;
        this.Tmr = TMR;
        this.PMr = pmr;
        this.CMr = cmr;
        this.CMn = CMn;
        this.Pmn = pmn;
    }

    //READ
    public Teams getTeam(String id){
        Optional<Teams> team = TMr.findById(id);
        if(team.isPresent()){
            return team.get();
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }

    public Page<Teams> getAllTeams(PageRequest page, String gender){
        return TMr.findAllByGender(gender, page);
    }
    //CREATE
    @Transactional
    public Teams createTeam(createTeamRequest request){
        Teams teamM = new Teams();
        teamM.setGender(request.getGender());
        teamM.setLeague_id(request.getLeague_id());
        teamM.setLeague_level(request.getLeague_level());
        teamM.setLeague_name(request.getLeague_name());
        teamM.setNationality_id(request.getNationality_id());
        teamM.setTeam_name(request.getTeam_name());
        teamM.setNationality_name(request.getNationality_name());
        TMr.save(teamM);
        TeamsNode newTeam = new TeamsNode();
        newTeam.setMongoId(teamM.get_id());
        newTeam.setLongName(teamM.getTeam_name());
        newTeam.setGender(teamM.getGender());
        Tmr.save(newTeam);
        return teamM;
    }

    //UPDATE
    @Transactional
    public Teams updateTeam(String id, updateTeam teamsDetails) {
        Optional<Teams> optionalTeam = TMr.findById(id);
        if (optionalTeam.isPresent()) {
            Teams existingTeam = optionalTeam.get();
            Optional<TeamsNode> optionalTeamNode = Tmr.findByMongoId(existingTeam.get_id());
            if(optionalTeamNode.isPresent()){
                TeamsNode existingTeamNode = optionalTeamNode.get();
                if(!existingTeam.getTeam_name().equals(teamsDetails.getTeam_name())){
                    //Updating attribute istances
                    List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
                    if(!players.isEmpty()){
                        for (Players player : players) {
                            List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                            for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                                playerFifaStat.getTeam().setTeam_name(teamsDetails.getTeam_name());
                                existingTeam.setTeam_name(teamsDetails.getTeam_name());
                                existingTeamNode.setLongName(teamsDetails.getTeam_name());
                                PMr.save(player);
                            }
                        }
                    }
                    List<Coaches> coaches = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
                    if(!coaches.isEmpty()){
                        for(Coaches coach : coaches){
                            List<TeamObj> teams = coach.getTeam();
                            for(TeamObj team : teams){
                                team.setTeam_name(teamsDetails.getTeam_name());
                                CMr.save(coach);
                            }
                        }
                    }
                }
                
                existingTeam.setGender(teamsDetails.getGender());
                existingTeam.setLeague_id(teamsDetails.getLeague_id());
                existingTeam.setLeague_level(teamsDetails.getLeague_level());
                existingTeam.setLeague_name(teamsDetails.getLeague_name());
                existingTeam.setNationality_id(teamsDetails.getNationality_id());
                existingTeam.setNationality_name(teamsDetails.getNationality_name());
                
                existingTeamNode.setGender(teamsDetails.getGender());
               

                Tmr.save(existingTeamNode);
                // Save the updated team back to the repository
                return TMr.save(existingTeam);
            }
            else{
                throw new RuntimeException("Team with id: " + id+" not correctly mapped in Neo4j");
            }
        
        } else {
        throw new RuntimeException("Team not found with id: " + id);
        }
    }

    public Teams updateFifaTeam(String id, Integer fifaV, updateFifaTeam request) {
        Optional<Teams> optionalTeam = TMr.findById(id);
        if(optionalTeam.isPresent()){
            Teams existingTeam = optionalTeam.get();
            List<FifaStatsTeam> existingFifaStatsTeam = existingTeam.getFifaStats();
            for(FifaStatsTeam stat : existingFifaStatsTeam){
                if(stat.getFifa_version().equals(fifaV)){
                    if(!stat.getFifa_version().equals(request.getFifa_version())){
                        stat.setFifa_version(request.getFifa_version());

                        //Updating relationship in Neo4j
                        Optional<CoachesNode> optionalCoach = CMn.findByMongoId(stat.getCoach().getCoach_mongo_id());
                        if(optionalCoach.isPresent()){
                            CoachesNode existingCoach = optionalCoach.get();
                            List<manages_team> relationships = existingCoach.getTeamMNodes();
                            for (manages_team relationship : relationships) {
                                if (relationship.getFifaV().equals(fifaV)) {
                                    relationship.setFifaV(request.getFifa_version());
                                    CMn.save(existingCoach);
                                    break;
                                }
                            }
                        }
                        else{
                            throw new RuntimeErrorException(null, "Coach not found with name: " + stat.getCoach().getCoach_name());
                        }

                        //Updating attribute istances
                        List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
                        for(Players player : players){
                            List<FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                            for(FifaStatsPlayer playerFifaStat : playerFifaStats){
                                if(playerFifaStat.getFifa_version().equals(fifaV)){
                                    Optional<PlayersNode> optionalPlayersNode = Pmn.findByMongoId(player.get_id());
                                    if(optionalPlayersNode.isPresent()){
                                        PlayersNode existingPlayerNode = optionalPlayersNode.get();
                                        List<plays_in_team> existing = existingPlayerNode.getTeamMNodes();
                                        for(plays_in_team exist: existing){
                                            if(exist.getFifaV().equals(fifaV)){
                                                exist.setFifaV(request.getFifa_version());
                                                Pmn.save(existingPlayerNode);
                                                break;
                                            }
                                        }
                                    }
                                    playerFifaStat.setFifa_version(request.getFifa_version());
                                    PMr.save(player);
                                    break;
                                }
                            }
                        }
                        List<Coaches> coaches = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
                        for(Coaches coach : coaches){
                            List<TeamObj> teams = coach.getTeam();
                            for(TeamObj team : teams){
                                if(team.getFifa_version().equals(fifaV)){
                                    team.setFifa_version(request.getFifa_version());
                                    break;
                                }
                            }
                        }
                    }
                        stat.setHome_stadium(request.getHome_stadium());
                        stat.setOverall(request.getOverall());
                        stat.setClub_worth_eur(request.getClub_worth_eur());
                        stat.setAttack(request.getAttack());
                        stat.setDefence(request.getDefence());
                        stat.setMidfield(request.getMidfield());
                        stat.setOff_players_in_box(request.getOff_players_in_box());
                        stat.setOff_corners(request.getOff_corners());
                        stat.setOff_free_kicks(request.getOff_free_kicks());
                        break;
                }
                else{
                    throw new RuntimeErrorException(null, "Fifa stats not found with id: " + id);
                }
            }
            return TMr.save(existingTeam);
            
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }
    
    public Teams updateCoachTeam(String id, Integer fifaV, updateCoachTeam request){
        Optional<Teams> optionalTeam = TMr.findById(id);
        Optional<TeamsNode> optionalTeamNode = Tmr.findByMongoId(id);
        Optional<Coaches> optionalCoach = CMr.findById(request.getCoach_mongo_id());
        if(optionalTeam.isPresent() && optionalTeamNode.isPresent()){
            Teams existingTeam = optionalTeam.get();
            TeamsNode existingTeamNode = optionalTeamNode.get();
            List<FifaStatsTeam> existingFifaStats = existingTeam.getFifaStats();
            if( optionalCoach.isPresent()){
                
                Coaches existingCoach = optionalCoach.get();
                for(FifaStatsTeam stats : existingFifaStats){
                    if(stats.getFifa_version().equals(fifaV)){
                        if(request.getCoach_mongo_id().equals(stats.getCoach().getCoach_mongo_id())){
                            return existingTeam;
                        }
                        List<TeamObj> teams = existingCoach.getTeam();
                        for(TeamObj team : teams){
                            if(team.getFifa_version().equals(fifaV)){
                                team.setTeam_name(existingTeam.getTeam_name());
                                team.setTeam_mongo_id(existingTeam.get_id());
                                team.setFifa_version(fifaV);
                                break;
                            }
                        }
                        CMr.save(existingCoach);
                            
                        //Update Coach Neo4j
                        List<Coaches> optCoach = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
                        for(Coaches coach : optCoach){
                            for(TeamObj team : teams){
                                if(team.getFifa_version().equals(fifaV)){
                                    Optional<CoachesNode> optionalCoachNodeOld = CMn.findByMongoId(coach.get_id());
                                    if(optionalCoachNodeOld.isPresent()){
                                        CoachesNode existingCoachNode = optionalCoachNodeOld.get();
                                        List<manages_team> relationships = existingCoachNode.getTeamMNodes();
                                        for (manages_team relationship : relationships) {
                                            if (relationship.getFifaV().equals(fifaV)) {
                                                relationships.remove(relationship);
                                                CMn.save(existingCoachNode);
                                                break;
                                            }
                                        }
                                    }
                                    else{
                                        throw new RuntimeErrorException(null, "Coach not found with name: " + coach.getLong_name());
                                }
                                break;
                            }
                        }
                    }
                           
                            Optional<CoachesNode> optionalCoachNode = CMn.findByMongoId(existingCoach.get_id());
                            if(optionalCoachNode.isPresent()){
                                CoachesNode existingCoachNode = optionalCoachNode.get();
                                manages_team relationship = new manages_team(existingTeamNode, fifaV);
                                existingCoachNode.getTeamMNodes().add(relationship);
                                CMn.save(existingCoachNode);
                            }
                            else{
                                throw new RuntimeErrorException(null, "Coach not found with id: " + request.getCoach_mongo_id());
                            }
                            //Update Team MongoDB
                            
                            stats.getCoach().setCoach_name(existingCoach.getLong_name());
                            stats.getCoach().setCoach_mongo_id(existingCoach.get_id());
                            TMr.save(existingTeam);
                        }

                    
                    }
                    return existingTeam;
                
                
            }
            else{
                throw new RuntimeErrorException(null, "Coach not found with id: " + request.getCoach_mongo_id());
            }

        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }

    }  
    //DELETE
    @Transactional
    public void deleteTeam(String id){
        Optional<Teams> team = TMr.findById(id);
        Optional<TeamsNode> teamsNode = Tmr.findByMongoId(id);
        if(team.isPresent()){
            Teams existingTeam = team.get();

            //Deleting attribute istances
            List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
            if(!players.isEmpty()){
                for (Players player : players) {
                    List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                    for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                        playerFifaStat.getTeam().setTeam_name("DefaultTeam");
                        playerFifaStat.getTeam().setTeam_mongo_id("DefaultTeam");
                        playerFifaStat.setClub_position("NA");
                        playerFifaStat.setLeague_name("DefaultLeague");
                        playerFifaStat.setClub_contract_valid_until_year(2999);
                        playerFifaStat.setClub_jersey_number(-1);
                        playerFifaStat.setLeague_level(-1);
                        PMr.save(player);
                    }
                }
            }

            List<Coaches> coaches = CMr.findByTeamMongoIdInTeams(existingTeam.get_id());
            if(!coaches.isEmpty()){
                for(Coaches coach : coaches){
                    List<TeamObj> teams = coach.getTeam();
                    for(TeamObj Team : teams){
                        Team.setTeam_name("DefaultTeam");
                        Team.setTeam_mongo_id("XXXXXXXXXXXX");
                        CMr.save(coach);
                    }
                }
            }
            //deleting team in mongoDB
            TMr.deleteById(id);
        }
        //Deleting team in Neo4j
        if(teamsNode.isPresent()){
            Tmr.delete(teamsNode.get());
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
        
    }
    @Transactional
    public void deleteFifaTeam(String id, Integer fifaV){
        Optional<Teams> team = TMr.findById(id);
        Optional<TeamsNode> teamsNode = Tmr.findByMongoId(id);
        if(team.isPresent() && teamsNode.isPresent()){
            TeamsNode existingTeamNode = teamsNode.get();
            Teams existingTeam = team.get();
            List<FifaStatsTeam> existingFifaStats = existingTeam.getFifaStats();
            //removing relationship in Neo4j
            if(!existingFifaStats.isEmpty()) {
                for (FifaStatsTeam fifaStat : existingFifaStats) {
                    if (fifaStat.getFifa_version().equals(fifaV)) {
                        Optional<CoachesNode> optionalCoach = CMn.findByMongoId(fifaStat.getCoach().getCoach_mongo_id());
                        if(optionalCoach.isPresent()){
                            CoachesNode existingCoach = optionalCoach.get();
                            manages_team relationship = CMn.findFifaVersionByLongNameAndFifaV(existingTeamNode.getLongName(), fifaV);
                            if (relationship != null) {
                                existingCoach.getTeamMNodes().remove(relationship);
                                CMn.save(existingCoach);
                                break;
                            }
                        }
                        else{
                            throw new RuntimeErrorException(null, "Coach not found with name: " + fifaStat.getCoach().getCoach_name());
                        }
                    }
                }
                //Deleting fifa stats from MongoDB
                for (FifaStatsTeam fifaStat : existingFifaStats) {
                    if (fifaStat.getFifa_version().equals(fifaV)) {
                        fifaStat.setFifa_version(-1);
                        fifaStat.setHome_stadium("DefaultStadiumName");
                        fifaStat.setOverall(-1);
                        fifaStat.setAttack(-1);
                        fifaStat.setMidfield(-1);
                        fifaStat.setDefence(-1);
                        fifaStat.setClub_worth_eur(-1);

                        fifaStat.getCoach().setCoach_mongo_id("XXXXXXXXXXXX");
                        fifaStat.getCoach().setCoach_name("DefaultCoachName");

                        break;
                    }
                }
                TMr.save(existingTeam);

                //Deleting attribute istances
                
                List<Players> players = PMr.findByClubTeamMongoIdInFifaStats(existingTeam.get_id());
                if(!players.isEmpty()){
                    for (Players player : players) {
                        List <FifaStatsPlayer> playerFifaStats = player.getFifaStats();
                        for (FifaStatsPlayer playerFifaStat : playerFifaStats) {
                            if (playerFifaStat.getFifa_version().equals(fifaV)) {
                                playerFifaStat.getTeam().setTeam_name("DefaultTeam");
                                playerFifaStat.getTeam().setTeam_mongo_id("DefaultTeam");
                                playerFifaStat.setClub_position("NA");
                                playerFifaStat.setLeague_name("DefaultLeague");
                                playerFifaStat.setClub_contract_valid_until_year(2999);
                                playerFifaStat.setClub_jersey_number(-1);
                                playerFifaStat.setLeague_level(-1);
                                PMr.save(player);
                                break;
                            }
                        }
                    }
                }
            }
            else{
                throw new RuntimeErrorException(null, "Fifa stats not found with id: " + id);
            }
        }
        else{
            throw new RuntimeErrorException(null, "Team not found with id: " + id);
        }
    }
}