
package com.example.demo.aggregations.DTO;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.List;
import java.util.Arrays;
/**
 *
 * @author carlo
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter

public class monthSummary {
    
    @Field("_id")
    private String month;
    
    @Field("new_users")
    private Integer newSubscribers;

    @PersistenceConstructor
    public monthSummary(String month, Integer newSubscribers) {
        this.newSubscribers = newSubscribers;
        this.month = convertMonth(month);
    }
    public String convertMonth(String month){
        List<String> Months=Arrays.asList("January","February","March","April","May","June","July","August","September","October","November","December");
        return Months.get(Integer.parseInt(month)-1);
    }
    
}


