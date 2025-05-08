package com.example.demo.requets;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class createArticleRequest {
    private String content;
    private String title;

    public createArticleRequest(String title, String content){
        this.title = title;
        this.content = content;
    }
}
