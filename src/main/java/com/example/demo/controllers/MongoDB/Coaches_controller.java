package com.example.demo.controllers.MongoDB;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.models.MongoDB.Coaches;
import com.example.demo.services.MongoDB.Coaches_service;
import com.example.demo.requets.createCoachRequest;
import com.example.demo.requets.updateCoach;
import com.example.demo.requets.updateTeamCoach;

import io.swagger.v3.oas.annotations.Operation;


@RestController
@RequestMapping("/api/v1/")
public class Coaches_controller {

    private final Coaches_service coachesMservice;
    public Coaches_controller(Coaches_service coachesMservice) {
        this.coachesMservice = coachesMservice;
    }

    // READ: Get coach by ID
    @GetMapping("/admin/coach/{_id}")
    @Operation(summary = "READ: Get a coach by ID", tags={"Coach"})
    public Coaches getCoach(@PathVariable String _id) {
        return coachesMservice.getCoach(_id);
    }

    // READ: Get all coaches by gender with pagination
    @GetMapping("admin/coach/byGender/{gender}")
    @Operation(summary = "READ: Get all coaches by gender with pagination", tags={"Admin:Coach"})
    public Page<Coaches> getAllCoaches(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "50") int size, 
                                       @PathVariable String gender) {
        PageRequest pageable = PageRequest.of(page, size);
        return coachesMservice.getAllCoaches(pageable, gender);
    }

    // CREATE: Create a new coach
    @PostMapping("admin/coach/new")
    @Operation(summary = "CREATE: Create a new coach", tags={"Admin:Coach"})
    public Coaches createCoach(@RequestBody createCoachRequest coach) {
        return coachesMservice.createCoach(coach);
    }

    // UPDATE: Update an existing coach by ID
    @PutMapping("admin/coach/modify/{_id}")
    @Operation(summary = "UPDATE: Update an existing coach by ID", tags={"Admin:Coach"})
    public Coaches updateCoach(@PathVariable String _id, @RequestBody updateCoach coachDetails) {
        return coachesMservice.updateCoach(_id, coachDetails);
    }

    // UPDATE: update team of an existing coach by ID
    @PutMapping("admin/coach/modify/{_id}/{fifaV}")
    @Operation(summary = "UPDATE: update team of an existing coach by ID", tags={"Admin:Coach"})
    public Coaches updateTeam(@PathVariable String _id, @PathVariable Integer fifaV, @RequestBody updateTeamCoach coachDetails) {
        return coachesMservice.updateTeamCoach(_id, fifaV, coachDetails);
    }

    // UPDATE: Update FIFA version of a coach by ID
    @PutMapping("admin/coach/modify/{_id}/{oldFifa}/{newFifa}")
    @Operation(summary = "UPDATE: Update FIFA version of a coach by ID", tags={"Admin:Coach"})
    public Coaches updateFifaVersion(@PathVariable String _id, @PathVariable Integer oldFifa, @PathVariable Integer newFifa) {
        return coachesMservice.updateFifaCoach(_id, oldFifa, newFifa);
    }

    // DELETE: Delete a coach by ID
    @DeleteMapping("admin/coach/delete/{_id}")
    @Operation(summary = "DELETE: Delete a coach by ID", tags={"Admin:Coach"})
    public ResponseEntity<Void> deleteCoach(@PathVariable String _id) {
        coachesMservice.deleteCoach(_id);
        return ResponseEntity.noContent().build();
    }

    // DELETE: Delete a coach by ID
    @DeleteMapping("admin/coach/delete/team/{_id}/{fifaV}")
    @Operation(summary = "DELETE: Delete a team in a coach by ID and FIFA version", tags={"Admin:Coach"})
    public ResponseEntity<Void> deleteTeamCoach(@PathVariable String _id, @PathVariable Integer fifaV) {
        coachesMservice.deleteTeamCoach(_id, fifaV);
        return ResponseEntity.noContent().build();
    }
}