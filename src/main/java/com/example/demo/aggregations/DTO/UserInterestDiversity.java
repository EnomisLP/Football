package com.example.demo.aggregations.DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Data
@Getter
@Setter 
@AllArgsConstructor
@NoArgsConstructor
public class UserInterestDiversity {
    private String userName;
    private Double avgInterestCount;
}
