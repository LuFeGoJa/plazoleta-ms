package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.model.RoleNames;
import com.pragma.powerup.infrastructure.out.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .authorizeRequests()
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/restaurant").hasAuthority(RoleNames.ADMIN)
                .antMatchers(HttpMethod.GET, "/api/v1/restaurant").hasAuthority(RoleNames.CLIENT)
                .antMatchers(HttpMethod.GET, "/api/v1/dish").hasAuthority(RoleNames.CLIENT)
                .antMatchers(HttpMethod.POST, "/api/v1/restaurant/*/employee").hasAuthority(RoleNames.OWNER)
                .antMatchers(HttpMethod.GET, "/api/v1/order").hasAuthority(RoleNames.EMPLOYEE)
                .antMatchers(HttpMethod.PATCH, "/api/v1/order/*/assign").hasAuthority(RoleNames.EMPLOYEE)
                .antMatchers(HttpMethod.PATCH, "/api/v1/order/*/ready").hasAuthority(RoleNames.EMPLOYEE)
                .antMatchers(HttpMethod.PATCH, "/api/v1/order/*/deliver").hasAuthority(RoleNames.EMPLOYEE)
                .antMatchers(HttpMethod.PATCH, "/api/v1/order/*/cancel").hasAuthority(RoleNames.CLIENT)
                .antMatchers(HttpMethod.POST, "/api/v1/order").hasAuthority(RoleNames.CLIENT)
                .antMatchers(HttpMethod.POST, "/api/v1/dish").hasAuthority(RoleNames.OWNER)
                .antMatchers(HttpMethod.PATCH, "/api/v1/dish/**").hasAuthority(RoleNames.OWNER)
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
