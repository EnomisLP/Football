package com.example.demo.requets;

import java.util.List;

import com.example.demo.models.MongoDB.ROLES;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class createUserRequest {
    private String username;
    private String password;
    private List<ROLES> roles;

    public createUserRequest( String username, String password, List<ROLES> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }
}
