package com.example.demo.configurations.Swagger;

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
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/user/{_id}/**","api/v1/user/team/**",
                                 "/api/v1/player/{_id}/**","/api/v1/team/{_id}/**",
                                 "/api/v1/coach/{_id}/**","/api/v1/article/{_id}/**",
                                 "/api/v1/user/article/**","/api/v1/search/filter/**").hasRole("USER")
                .requestMatchers("/api/v1/auth/me", "/api/v1/enrolments/**").authenticated()
                .requestMatchers("/api/v1/search/**","/api/v1/user/{_id}","/api/v1/player/{_id}",
                                 "/api/v1/team/{_id}","/api/v1/coach/{_id}").permitAll()
                .requestMatchers("/api/v1/signup").anonymous()
                .requestMatchers("/swagger-ui/**","/v3/api-docs/**","/swagger-resources/**","/webjars/**").permitAll()
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