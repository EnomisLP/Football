package com.example.demo.projections;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Replace the class with an interface
public class UsersNodeProjection {
    private String UserName;
    private String MongoId;
}
