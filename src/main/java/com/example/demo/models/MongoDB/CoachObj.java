package com.example.demo.models.MongoDB;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class CoachObj {
    private String coach_mongo_id;
    private String coach_name;
    private Integer coach_id;
}
