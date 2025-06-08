package com.example.demo.services.MongoDB;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.demo.DTO.CoachesNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.TeamObj;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.services.Neo4j.Coaches_node_service;
import com.example.demo.requets.createCoachRequest;
import com.example.demo.requets.updateCoach;
import jakarta.transaction.Transactional;
import com.example.demo.requets.updateTeamCoach;


@Service
public class Coaches_service {
    
    private  Coaches_repository CMr;
    private Coaches_node_rep Cmr;
    private Coaches_node_service CMs;
    private Teams_repository TMs;
    private Teams_node_rep TMr;
    private static final Integer CURRENT_YEAR = 24;
    

    public Coaches_service(Coaches_repository CMr, Coaches_node_rep Cmr, Coaches_node_service CMs
    , Teams_repository TMs, Teams_node_rep TMR){ 
        this.CMr = CMr;
        this.Cmr = Cmr;
        this.CMs = CMs;
        this.TMs = TMs;
        this.TMr = TMR;
    }

    
    //READ
    public Coaches getCoach(String id){
        Optional<Coaches> coach = CMr.findById(id);
        if(coach.isPresent()){
            return coach.get();
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found with id: " + id);
        }
    }
    public Page<Coaches> getAllCoaches(PageRequest page, String gender){
        return CMr.findAllByGender(gender, page);
    }

    //CREATE
    @Async("customAsyncExecutor")
    @Transactional
    public CompletableFuture<Coaches> createCoach(createCoachRequest request){
        Coaches coachM = new Coaches();
        coachM.setGender(request.getGender());
        coachM.setLong_name(request.getLong_name());
        coachM.setNationality_name(request.getNationality_name());
        coachM.setShort_name(request.getShort_name());
        CMr.save(coachM);
        CoachesNode coachNode = new CoachesNode();
        coachNode.setMongoId(coachM.get_id());
        coachNode.setLongName(coachM.getLong_name());
        coachNode.setNationalityName(coachM.getNationality_name());
        coachNode.setGender(coachM.getGender());
        Cmr.save(coachNode);
        return CompletableFuture.completedFuture(coachM);
    };

     //UPDATE
     @Transactional
     @Async("customAsyncExecutor")
     public CompletableFuture<Coaches> updateCoach(String id, updateCoach coachDetails) {
        Optional<Coaches> optionalCoach = CMr.findById(id);
        if (optionalCoach.isPresent()) {
            Coaches existingCoach = optionalCoach.get();
            Optional<CoachesNodeDTO> optionalCoachNode = Cmr.findByMongoIdLight(id);
            if(optionalCoachNode.isPresent()){
                //Checking if the long_name was changed in the request
                if(!coachDetails.getLong_name().equals(existingCoach.getLong_name())){
                    //if it was changed, we need to update the long_name in the Neo4j database 
                    //and change all long_name instances in the teams
                    List<Teams> teams = TMs.findByCoachMongoId(id);
                    if(!teams.isEmpty()){
                        for(Teams team : teams){
                            for(FifaStatsTeam stats : team.getFifaStats()){
                                if(!stats.getCoach().getCoach_mongo_id().equals(existingCoach.get_id())){
                                    continue;
                                }
                                stats.getCoach().setCoach_name(coachDetails.getLong_name());
                                TMs.save(team);
                            }
                        }
                    }
                    
                }
                // Update the other fields of the coach
                existingCoach.setLong_name(coachDetails.getLong_name());
                existingCoach.setShort_name(coachDetails.getShort_name());
                existingCoach.setGender(coachDetails.getGender());
                
                
               Cmr.updateAttributesByMongoId(id, coachDetails.getShort_name(), coachDetails.getLong_name(), coachDetails.getGender());
                // Save the updated coach back to the repository
                return CompletableFuture.completedFuture(CMr.save(existingCoach));
                
            }    
            else{
                throw new RuntimeException("Coach with id: " + id + "is not correctly mapped into Neo4j");
            }
        }
        else {
            throw new RuntimeException("Coach not found with id: " + id);
        }
    }
    
    @Transactional
    @Async("customAsyncExecutor")
    public CompletableFuture<Coaches> updateTeamCoach(String id, Integer fifaV, updateTeamCoach request){
        Optional<Coaches> optionalCoach = CMr.findById(id);
        Optional<Teams> optionalTeam = TMs.findById(request.getTeam_mongo_id());
        Optional<CoachesNodeDTO> optionalCoachNode = Cmr.findByMongoIdLight(id);
        if (optionalCoach.isPresent() && optionalCoachNode.isPresent()) {
            Coaches existingCoach = optionalCoach.get();
            if(optionalTeam.isPresent()){
                Teams existingTeam = optionalTeam.get();
                    List<Teams> teams = TMs.findByCoachMongoId(id);
                    List<TeamObj> Check = existingCoach.getTeam();
                    for(TeamObj team : Check){
                        if(team.getFifa_version().equals(fifaV)){
                            if(team.getTeam_mongo_id().equals(request.getTeam_mongo_id())){
                                return CompletableFuture.completedFuture(existingCoach);
                            }
                        }
                    }
                    //Updating Team MongoDB
                    for(Teams team : teams){
                        List<FifaStatsTeam> stats = team.getFifaStats();
                         for(FifaStatsTeam stat : stats){
                             if(stat.getFifa_version().equals(fifaV)){
                                 stat.getCoach().setCoach_mongo_id("XXXXXXXXXXXX");
                                stat.getCoach().setCoach_name("DefaultCoachName");
                                 TMs.save(team);
                             }
                         }
                     }
                     
                    //Updating Neo4j
                    Optional<TeamsNodeDTO> optionalTeamsNode = TMr.findByMongoIdLight(request.getTeam_mongo_id());
                    if(optionalTeamsNode.isPresent()){
                        Cmr.deleteOldManagesRelationToTeam(id, fifaV);
                        Cmr.createManagesRelationToTeam(id, request.getTeam_mongo_id(), fifaV);
                    }

                   
                    List<TeamObj> teamObj = existingCoach.getTeam();
                    //Updating coach MongoDB
                    for(TeamObj team : teamObj){
                        if(team.getFifa_version().equals(fifaV)){
                            team.setTeam_name(existingTeam.getTeam_name());
                            team.setTeam_mongo_id(existingTeam.get_id());
                            team.setFifa_version(fifaV);
                            CMr.save(existingCoach);
                            break;
                        }
                    }
                    
                
            return CompletableFuture.completedFuture(existingCoach);
            }
            else{
                throw new RuntimeException("Teams not found with id: " + request.getTeam_mongo_id());
            }     
        }
        else {
            throw new RuntimeException("Coach not found with id: " + id);
        }
    }
    
    @Transactional
    @Async("customAsyncExecutor")
    public CompletableFuture<Coaches> updateFifaCoach(String id, Integer oldFifaV, Integer newFifaV){
        if(oldFifaV.equals(newFifaV)){
            throw new RuntimeException("The FIFA version is the same as the one already in the database: " + oldFifaV);
        }
        Optional<Coaches> optionalCoach = CMr.findById(id);
        Optional<CoachesNodeDTO> optionalCoachNode = Cmr.findByMongoIdLight(id);
        if (optionalCoach.isPresent() && optionalCoachNode.isPresent()) {
            Coaches existingCoach = optionalCoach.get();

            //Updating Team MongoDB
            List<Teams> teams = TMs.findByCoachMongoId(id);
            if(teams.isEmpty()){
                throw new RuntimeException("Team list empty");
            }
            for(Teams team : teams){
                List<FifaStatsTeam> stats = team.getFifaStats();
                Optional<TeamsNodeDTO> optionalTeamsNode = TMr.findByMongoIdLight(team.get_id());
                if(!optionalTeamsNode.isPresent()){
                    throw new RuntimeException("Team not found with id: " + team.get_id());
                }
                for(FifaStatsTeam stat : stats){
                    if(stat.getFifa_version().equals(oldFifaV)){
                        Cmr.deleteManagesRelationToTeam(existingCoach.get_id(), team.get_id(), oldFifaV);
                        Cmr.createManagesRelationToTeam(existingCoach.get_id(), team.get_id(), newFifaV);
                        stat.setFifa_version(newFifaV);
                        TMs.save(team);
                        break;
                    }
                }
                
            }

            //Updating Coach MongoDB
            List<TeamObj> teamObj = existingCoach.getTeam();
            for(TeamObj team : teamObj){
                if(team.getFifa_version().equals(oldFifaV)){
                    team.setFifa_version(newFifaV);
                    CMr.save(existingCoach);
                    break;
                }
            }

            
        return CompletableFuture.completedFuture(existingCoach);
        }
        else {
            throw new RuntimeException("Coach not found with id: " + id);
        }
    }
    //DELETE


    @Transactional
    @Async("customAsyncExecutor")
    public CompletableFuture<String> deleteCoach(String id){
        Optional<Coaches> coach = CMr.findById(id);
        Optional<CoachesNodeDTO> coachNode = Cmr.findByMongoIdLight(id);
        if(coach.isPresent() && coachNode.isPresent()){
            // Delete the coach_id istances
            List<Teams> teams = TMs.findByCoachMongoId(id);
            if(!teams.isEmpty()){
                for(Teams team : teams){
                    for(FifaStatsTeam stats : team.getFifaStats()){
                        stats.getCoach().setCoach_mongo_id("XXXXXXXXXXXX");
                        stats.getCoach().setCoach_name("DefaultCoachName");
                        TMs.save(team);
                    }
                }
            }
            // Delete the coach from MongoDB and Neo4j
            Cmr.deleteCoachByMongoIdLight(id);
            CMr.deleteById(coach.get().get_id());
            return CompletableFuture.completedFuture("Completed...");
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found with id: " + id);
        } 
    }

    @Transactional
    @Async("customAsyncExecutor")
    public CompletableFuture<String> deleteTeamCoach(String id, Integer fifaV){
        Optional<Coaches> optionalCoach = CMr.findById(id);
        Optional<CoachesNodeDTO> optionalCoachNode = Cmr.findByMongoIdLight(id);
        if(optionalCoach.isPresent() && optionalCoachNode.isPresent()){
            Coaches existingCoach = optionalCoach.get();
            List<TeamObj> existingList = existingCoach.getTeam();

            //Removing relationships
            if(!existingList.isEmpty()){
                for(TeamObj team : existingList){
                    if(team.getFifa_version().equals(fifaV)){
                        Optional<TeamsNodeDTO> optionalTeamsNode = TMr.findByMongoIdLight(team.getTeam_mongo_id());
                        if(optionalTeamsNode.isPresent()){
                            Optional<CoachesNodeDTO> relationship = Cmr.findFifaVersionByMongoIdAndFifaV(team.getTeam_mongo_id(), fifaV);
                            if (relationship != null) {
                                
                                Cmr.deleteManagesRelationToTeam(id, team.getTeam_mongo_id(), fifaV);
                                break;
                            }
                        }
                        else{
                            throw new RuntimeErrorException(null, "Team not found with name: " + team.getTeam_name());
                        }

                    }
                }
                //Removing istances
                List<Teams> teamIstances = TMs.findByCoachMongoId(id);
                if(!teamIstances.isEmpty()){
                    for(Teams teamI : teamIstances){
                        List<FifaStatsTeam> stats = teamI.getFifaStats();
                        for(FifaStatsTeam stat : stats){
                            if(stat.getFifa_version().equals(fifaV)){
                                stat.getCoach().setCoach_mongo_id("XXXXXXXXXXXX");
                                stat.getCoach().setCoach_name("DefaultCoachName");
                                TMs.save(teamI);
                                break;
                            }
                        }
                    }
                }
                else{
                throw new RuntimeErrorException(null, "Team coach-list is empty");
            }
                //Updating MongoDB
                List<TeamObj> currentList = existingCoach.getTeam();
                for(TeamObj current : currentList){
                    if(current.getFifa_version().equals(fifaV)){
                        current.setFifa_version(-1);
                        current.setTeam_mongo_id("XXXXXXXXXXXX");
                        current.setTeam_name("DefaultTeamName");
                        CMr.save(existingCoach);
                        break;
                    }
                }
                return CompletableFuture.completedFuture("Completed...");
            }
            else{
                throw new RuntimeErrorException(null, "Coach team-list is empty");
            }
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found");
        }
    }

}
