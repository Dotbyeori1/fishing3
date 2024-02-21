package com.haegreen.fishing.config;

import com.haegreen.fishing.security.*;
import com.haegreen.fishing.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    // 로그인&시큐리티 관련
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOauth2UserService customOauth2UserService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource())) // CROS 허용
                .csrf(AbstractHttpConfigurer::disable) // csrf 비허용
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/member/logout")));

        http.oauth2Login(oauth2Login -> oauth2Login.redirectionEndpoint(redirectionEndpoint ->redirectionEndpoint.baseUri("/member/login/oauth2/code/**"))
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(customOauth2UserService))
                .successHandler(oAuth2AuthenticationSuccessHandler()));

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)); //세션 유지 정책

        // 스프링 시큐리티 필터가 돌기전에 해야하는 것들
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http.authorizeRequests((authorize)->authorize
                    .requestMatchers( "/jowhangboard/modify", "/jowhangboard/register",
                            "/noticeboard/register", "/noticeboard/modify").hasRole("ADMIN") // 관리자만 허용
                    .requestMatchers( "/admin","/admin/**","/", "/main", "/authsession", "/page/**", "/review/**", "/reservation/**",
                            "/member/**", "/noticeboard/list", "/noticeboard/read", "/jowhangboard/list","/jowhangboard/read",
                            "/member/login/oauth2/code/**").permitAll() // 모두 허용
                    .requestMatchers("/css/**", "/js/**", "/img/**", "/haegreen/**").permitAll()
                    .requestMatchers("/images/**", "/product/**").permitAll());

        // http로 접속하면 https로 강제 리다이렉트 하는 기능
        // http.requiresChannel(requiresChannel -> requiresChannel.requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                        //.requiresSecure());

        return http.build();
    }


    // 비밀번호 암호화 세팅
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler() {
        return new OAuth2AuthenticationSuccessHandler(customUserDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001")); // 프론트엔드 서버 포트에 맞게 수정
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("X-Requested-With", "Content-Type", "Authorization", "X-XSRF-token"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}