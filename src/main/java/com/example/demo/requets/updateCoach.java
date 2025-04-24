package com.example.demo.requets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class updateCoach {
    private Integer coach_id;
    private String short_name;
    private String long_name;
    private String gender;
    
}
