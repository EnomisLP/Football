package com.example.demo.services.MongoDB;
import java.util.List;
import java.util.Optional;

import javax.management.RuntimeErrorException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.models.MongoDB.FifaStatsTeam;
import com.example.demo.models.MongoDB.TeamObj;
import com.example.demo.models.MongoDB.Teams;
import com.example.demo.models.Neo4j.CoachesNode;
import com.example.demo.models.Neo4j.TeamsNode;
import com.example.demo.relationships.manages_team;
import com.example.demo.repositories.MongoDB.Coaches_repository;
import com.example.demo.repositories.MongoDB.Teams_repository;
import com.example.demo.repositories.Neo4j.Coaches_node_rep;
import com.example.demo.repositories.Neo4j.Teams_node_rep;
import com.example.demo.services.Neo4j.Coaches_node_service;
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
    @Transactional
    public Coaches createCoach(Coaches coach){
        Coaches coachM = CMr.save(coach);
        CoachesNode coachNode = new CoachesNode();
        coachNode.setMongoId(coachM.get_id());
        coachNode.setCoachId(coachM.getCoach_id());
        coachNode.setLongName(coachM.getLong_name());
        coachNode.setNationalityName(coachM.getNationality_name());
        coachNode.setGender(coachM.getGender());
        Cmr.save(coachNode);
        return coachM;
    };

     //UPDATE
     @Transactional
     public Coaches updateCoach(String id, updateCoach coachDetails) {
        Optional<Coaches> optionalCoach = CMr.findById(id);
        if (optionalCoach.isPresent()) {
            Coaches existingCoach = optionalCoach.get();
            Optional<CoachesNode> optionalCoachNode = Cmr.findByMongoId(id);
            if(optionalCoachNode.isPresent()){
                CoachesNode existingCoachNode = optionalCoachNode.get();
                //Checking if the coach_id was changed in the request
                if(!coachDetails.getCoach_id().equals(existingCoach.getCoach_id())){
                    //if it was changed, we need to update the coach_id in the Neo4j database 
                    //and change all coach_id instances in the teams
                    List<Teams> teams = TMs.findByCoachId(existingCoachNode.getCoachId());
                    if(!teams.isEmpty()){
                        for(Teams team : teams){
                            for(FifaStatsTeam stats : team.getFifaStats()){
                                stats.getCoach().setCoach_id(coachDetails.getCoach_id());
                                stats.getCoach().setCoach_name(coachDetails.getLong_name());
                                TMs.save(team);
                            }
                        }
                    }
                    existingCoach.setCoach_id(coachDetails.getCoach_id());
                    existingCoachNode.setCoachId(coachDetails.getCoach_id());
                }
                // Update the other fields of the coach
                existingCoach.setLong_name(coachDetails.getLong_name());
                existingCoach.setShort_name(coachDetails.getShort_name());
                existingCoach.setGender(coachDetails.getGender());
                
                
                existingCoachNode.setLongName(coachDetails.getLong_name());
                existingCoachNode.setGender(coachDetails.getGender());

                Cmr.save(existingCoachNode);
                // Save the updated coach back to the repository
                return CMr.save(existingCoach);
                
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
    public Coaches updateTeamCoach(String id, Integer fifaV, updateTeamCoach request){
        Optional<Coaches> optionalCoach = CMr.findById(id);
        Optional<Teams> optionalTeam1 = TMs.findByTeamId(request.getTeam_id());
        Optional<Teams> optionalTeam2 = TMs.findByTeamName(request.getTeam_name());
        Optional<CoachesNode> optionalCoachNode = Cmr.findByMongoId(id);
        if (optionalCoach.isPresent() && optionalCoachNode.isPresent()) {
            Coaches existingCoach = optionalCoach.get();
            CoachesNode existingCoachNode = optionalCoachNode.get();
            if(optionalTeam1.isPresent() && optionalTeam2.isPresent()){
                Teams existingTeam1 = optionalTeam1.get();
                Teams existingTeam2 = optionalTeam2.get();
                if(existingTeam1.getTeam_id().equals(existingTeam2.getTeam_id()) ||
                existingTeam1.getTeam_name().equals(existingTeam2.getTeam_name())){
                    List<Teams> teams = TMs.findByCoachId(existingCoachNode.getCoachId());
                    List<TeamObj> Check = existingCoach.getTeams();
                    for(TeamObj team : Check){
                        if(team.getFifa_version().equals(fifaV)){
                            if(!team.getTeam_id().equals(request.getTeam_id()) || team.getTeam_name().equals(request.getTeam_name())){
                                return existingCoach;
                            }
                        }
                    }
                    //Updating Team MongoDB
                    for(Teams team : teams){
                        List<FifaStatsTeam> stats = team.getFifaStats();
                         for(FifaStatsTeam stat : stats){
                             if(stat.getFifa_version().equals(fifaV)){
                                 stat.getCoach().setCoach_id(existingCoach.getCoach_id());
                                 stat.getCoach().setCoach_name(existingCoach.getLong_name());
                                 TMs.save(team);
                             }
                         }
                     }

                    //Updating Neo4j
                    Optional<TeamsNode> optionalTeamsNode = TMr.findByTeamId(request.getTeam_id());
                    if(optionalTeamsNode.isPresent()){
                        TeamsNode existingTeamsNode = optionalTeamsNode.get();
                        List<manages_team> manages = existingCoachNode.getTeamMNodes();
                        for(manages_team manage : manages){
                            if(manage.getFifaV().equals(fifaV)){
                                existingCoachNode.getTeamMNodes().remove(manage);
                                manages_team newManage = new manages_team(existingTeamsNode, fifaV);
                                existingCoachNode.getTeamMNodes().add(newManage);
                                Cmr.save(existingCoachNode);
                                break;
                            }
                        }
                    }

                   
                    List<TeamObj> teamObj = existingCoach.getTeams();
                    //Updating coach MongoDB
                    for(TeamObj team : teamObj){
                        if(team.getFifa_version().equals(fifaV)){
                            team.setTeam_id(request.getTeam_id());
                            team.setTeam_name(request.getTeam_name());
                            team.setTeam_mongo_id(existingTeam1.get_id());
                            CMr.save(existingCoach);
                            break;
                        }
                    }
                    
                }
            return existingCoach;
            }
            else{
                throw new RuntimeException("Teams not found with id: " + request.getTeam_id() + " or name: " + request.getTeam_name());
            }     
        }
        else {
            throw new RuntimeException("Coach not found with id: " + id);
        }
    }
    
    @Transactional
    public Coaches updateFifaCoach(String id, Integer oldFifaV, Integer newFifaV){
        if(oldFifaV.equals(newFifaV)){
            throw new RuntimeException("The FIFA version is the same as the one already in the database: " + oldFifaV);
        }
        Optional<Coaches> optionalCoach = CMr.findById(id);
        Optional<CoachesNode> optionalCoachNode = Cmr.findByMongoId(id);
        if (optionalCoach.isPresent() && optionalCoachNode.isPresent()) {
            Coaches existingCoach = optionalCoach.get();
            CoachesNode existingCoachNode = optionalCoachNode.get();

            //Updating Team MongoDB
            List<Teams> teams = TMs.findByCoachId(existingCoachNode.getCoachId());
            for(Teams team : teams){
                List<FifaStatsTeam> stats = team.getFifaStats();
                for(FifaStatsTeam stat : stats){
                    if(stat.getFifa_version().equals(oldFifaV)){
                        stat.getCoach().setCoach_id(null);
                        stat.getCoach().setCoach_name(null);
                        stat.getCoach().setCoach_mongo_id(null);
                        TMs.save(team);
                    }
                }
                for(FifaStatsTeam stat : stats){
                    if(stat.getFifa_version().equals(newFifaV)){
                        Optional<TeamsNode> optionalTeamsNode = TMr.findByTeamId(team.getTeam_id());
                        if(optionalTeamsNode.isPresent()){
                            manages_team newManage = new manages_team(optionalTeamsNode.get(), newFifaV);
                            existingCoachNode.getTeamMNodes().add(newManage);
                            Cmr.save(existingCoachNode);
                        }
                        
                        stat.getCoach().setCoach_id(existingCoach.getCoach_id());
                        stat.getCoach().setCoach_name(existingCoach.getLong_name());
                        TMs.save(team);
                    }
                }
            }

            //Updating Coach MongoDB
            List<TeamObj> teamObj = existingCoach.getTeams();
            for(TeamObj team : teamObj){
                if(team.getFifa_version().equals(oldFifaV)){
                    team.setFifa_version(newFifaV);
                    CMr.save(existingCoach);
                    break;
                }
            }

            //Updating Neo4j
            List<manages_team> manage = existingCoachNode.getTeamMNodes();
            for(manages_team existing : manage){
                if(existing.getFifaV().equals(oldFifaV)){
                    existingCoachNode.getTeamMNodes().remove(existing);
                    break;
                }
            }
            
        return existingCoach;
        }
        else {
            throw new RuntimeException("Coach not found with id: " + id);
        }
    }
    //DELETE


    @Transactional
    public void deleteCoach(String id){
        Optional<Coaches> coach = CMr.findById(id);
        Optional<CoachesNode> coachNode = Cmr.findByMongoId(id);
        if(coach.isPresent() && coachNode.isPresent()){
            // Delete the coach_id istances
            CoachesNode existing = coachNode.get();
            List<Teams> teams = TMs.findByCoachId(existing.getCoachId());
            if(!teams.isEmpty()){
                for(Teams team : teams){
                    for(FifaStatsTeam stats : team.getFifaStats()){
                        stats.getCoach().setCoach_id(null);
                        stats.getCoach().setCoach_mongo_id(null);
                        stats.getCoach().setCoach_name(null);
                        TMs.save(team);
                    }
                }
            }
            // Delete the coach from MongoDB and Neo4j
            CMs.deleteCoach(existing.get_id());
            CMr.deleteById(coach.get().get_id());
        }
        else{
            throw new RuntimeErrorException(null, "Coach not found with id: " + id);
        } 
    }

}
