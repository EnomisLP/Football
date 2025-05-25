package com.example.demo.requets;
import java.util.Date;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Data
public class createPlayerRequest {
    
    private  String short_name;
    private String long_name;
    private Integer age;
    private Date dob;
    private Long nationality_id;
    private String nationality_name;
    private Integer height_cm;
    private Integer weight_kg;
    private String gender;
}
