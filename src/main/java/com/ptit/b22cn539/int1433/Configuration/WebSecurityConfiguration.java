package com.ptit.b22cn539.int1433.Configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class WebSecurityConfiguration {
    AppFilterSecurity appFilterSecurity;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.addFilterBefore(this.appFilterSecurity, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
