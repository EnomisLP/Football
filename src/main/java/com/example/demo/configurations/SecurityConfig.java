package com.example.demo.configurations;

import com.example.demo.services.MongoDB.MongoUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final MongoUserDetailService mongoUserDetailService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/me", "/api/v1/enrolments/**").authenticated()
                .requestMatchers("/api/v1/Articles/admin/**","/api/v1/Coaches/admin/**","/api/v1/Players/admin/**"
                ,"/api/v1/Teams/admin/**","/api/v1/Users/admin/**","/api/v1/Coaches_Node/admin/**","/api/v1/Players_Node/admin/**"
                ,"/api/v1/Teams_Node/admin/**","/api/v1/Users_Node/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/Articles/user/**", "/api/v1/Coaches/user/**", "/api/v1/Players/user/**",
                "/api/v1/Teams/user/**","/api/v1/Users/user/**","/api/v1/Coaches_Node/user/**","/api/v1/Players_node/user/**", 
                "/api/v1/Teams_Node/user/**","/api/v1/Users_Node/user/**").hasRole("USER")
                .requestMatchers("/api/v1/Users/registration").anonymous()
                .anyRequest().permitAll()
            )
            .userDetailsService(mongoUserDetailService)
            .httpBasic(httpBasic -> httpBasic.realmName("MyApp"))
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout") // Custom logout URL
                .logoutSuccessUrl("/api/v1/auth/me") // Redirect after logout (if needed)
                .invalidateHttpSession(true)  // Invalidates the session
                .clearAuthentication(true)  // Clears the authentication context
                .deleteCookies("JSESSIONID")  // Deletes the session cookie
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(mongoUserDetailService).passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

