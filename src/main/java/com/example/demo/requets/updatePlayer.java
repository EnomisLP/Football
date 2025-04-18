package com.example.demo.requets;

import java.util.Date;

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
public class updatePlayer {
    private  String short_name;
    private String long_name;
    private Integer age;
    private Date dob;
    private String nationality_name;
    private Integer height_cm;
    private Integer weight_kg;
    private String gender;
}
