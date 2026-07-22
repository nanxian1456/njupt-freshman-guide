package cn.xszn.comments.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, SameOriginFilter sameOriginFilter, AdminTokenFilter adminTokenFilter)
      throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.disable())
        .httpBasic(httpBasic -> httpBasic.disable())
        .formLogin(formLogin -> formLogin.disable())
        .logout(logout -> logout.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/comments/**", "/api/health").permitAll()
            .anyRequest().denyAll())
        .addFilterBefore(sameOriginFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(adminTokenFilter, SameOriginFilter.class)
        .headers(headers -> headers
            .contentTypeOptions(contentType -> {})
            .frameOptions(frame -> frame.deny()))
        .build();
  }
}
