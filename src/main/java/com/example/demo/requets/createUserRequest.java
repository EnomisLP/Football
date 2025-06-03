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
    private String nationality_name;
    private String e_mail;

    public createUserRequest( String username, String password, List<ROLES> roles, String nationality,
    String email) {
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.nationality_name = nationality;
        this.e_mail = email;
    }
}
