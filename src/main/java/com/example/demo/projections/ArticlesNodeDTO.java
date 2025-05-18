package com.example.demo.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ArticlesNodeDTO {
    public String mongoId;
    public String title;
    public String author;
}
