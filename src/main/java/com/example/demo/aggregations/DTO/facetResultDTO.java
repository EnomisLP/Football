

package com.example.demo.aggregations.DTO;

import java.util.List;

import lombok.Data;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.demo.DTO.globalSearchResult;
import com.example.demo.aggregations.DTO.facetCountDTO;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class facetResultDTO {
    private List<globalSearchResult> data; 
    private List<facetCountDTO> count;
}
