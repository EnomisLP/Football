package com.example.demo.controllers.Neo4j;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import com.example.demo.DTO.CoachesNodeDTO;
import com.example.demo.DTO.TeamsNodeDTO;
import com.example.demo.services.Neo4j.Coaches_node_service;


import io.swagger.v3.oas.annotations.Operation;



@RestController
@RequestMapping("/api/v1/")


public class Coaches_node_controller{

  
    private Coaches_node_service coachesMNodeService;

    public Coaches_node_controller (Coaches_node_service CMNS){
        this.coachesMNodeService = CMNS;
    }

    // READ: Get coaches nodes
    @GetMapping("admin/coachNode/{_id}")
    @Operation(summary = "READ operation: Get coaches nodes", tags={"Admin:Coach"})
    public CoachesNodeDTO getCoach(@PathVariable String _id) {
        return coachesMNodeService.getCoach(_id);
    }

    @GetMapping("search/filter/coach/list/byGender/{gender}")
    @Operation(summary = "READ: Get all coaches by gender with pagination", tags={"Coach"})
    public Page<CoachesNodeDTO> getAllCoaches(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size, 
                                       @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return coachesMNodeService.getAllCoaches(pageable, gender);
    }
    @GetMapping("coach/{_id}/managing_history")
    @Operation(summary = "show team trained Hystory", tags={"Coach"})
    public List<TeamsNodeDTO> showTrainedHistory(String _id){
        return coachesMNodeService.showTrainedHistory(_id);
    }

    @GetMapping("coach/{_id}/team")
    @Operation(summary = "Show the team currently trained", tags={"Coach"})
    public TeamsNodeDTO showCurrentTeam(String _id){
        return coachesMNodeService.showCurrentTeam(_id);
    }

    @GetMapping("coach/{_id}/team/{fifaV}")
    @Operation(summary = "Show the team trained in a specific year", tags={"Coach"})
    public TeamsNodeDTO showSpecificTeam(String _id, Integer fifaV){
        return coachesMNodeService.showSpecificTeam(_id, fifaV);
    }
    // MAP: Map all Coaches from MongoDB to Neo4j nodes
    @PostMapping("admin/map/coaches")
    @Operation(summary = "MAP operation: Map all Coaches from MongoDB to Neo4j nodes", tags={"Admin:Map"})
    public String mapAllNodes() {
        return coachesMNodeService.MapAllTheNodes();
    }
    @PostMapping("/admin/map/manages_team_relationship/{gender}")
    @Operation(summary = "MAP all MANAGES_TEAMS relationships", tags={"Admin:Map"})
    public String mapManagesTeam(@PathVariable String gender){
        return coachesMNodeService.MapAllManagesTeam(gender);
    }
}