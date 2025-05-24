package com.example.demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@SpringBootApplication(/*exclude = {
    Neo4jAutoConfiguration.class,
    Neo4jDataAutoConfiguration.class,
    Neo4jReactiveDataAutoConfiguration.class,
    Neo4jReactiveRepositoriesAutoConfiguration.class,
    Neo4jRepositoriesAutoConfiguration.class
  }*/)
@EnableScheduling
public class FootballApplication {

    public static void main(String[] args) {
        SpringApplication.run(FootballApplication.class, args);
        // The following code can be moved to the run method
       
    }
    

   
}
