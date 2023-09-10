package account.security;

import account.repository.AppUserRepository;
import account.repository.SecurityEventRepository;
import account.service.AdminService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import static account.enums.Role.*;
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class AccServiceConfig {
    private final AdminService adminService;
    private final SecurityEventRepository securityEventRepository;
    private final AppUserRepository appUserRepository;


    @Autowired
    public AccServiceConfig(AdminService adminService, SecurityEventRepository securityEventRepository,
                            AppUserRepository appUserRepository) {

        this.adminService = adminService;
        this.securityEventRepository = securityEventRepository;
        this.appUserRepository = appUserRepository;

    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(matcherRegistry -> matcherRegistry
                        .requestMatchers(POST, "/api/auth/signup").permitAll()
                        .requestMatchers(POST, "/api/auth/changepass").authenticated()
                        .requestMatchers(GET, "/api/security/events/").hasRole(AUDITOR.name())
                        .requestMatchers(GET, "/api/empl/payment").hasAnyRole(USER.name(), ACCOUNTANT.name())
                        .requestMatchers(POST, "/api/acct/payments").hasRole(ACCOUNTANT.name())
                        .requestMatchers(PUT, "/api/acct/payments").hasRole(ACCOUNTANT.name())
                        .requestMatchers(PUT, "/api/admin/user/**").hasRole(ADMINISTRATOR.name())
                        .requestMatchers(GET, "/api/admin/user/**").hasRole(ADMINISTRATOR.name())
                        .requestMatchers(DELETE, "/api/admin/user/**").hasRole(ADMINISTRATOR.name())
                        .requestMatchers("/actuator/shutdown").permitAll()
                        .anyRequest().denyAll()
                )
                .httpBasic(s -> s.authenticationEntryPoint(authenticationEntryPoint()))
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(s -> s
                        .accessDeniedHandler(accessDeniedHandler()))
                .csrf(AbstractHttpConfigurer::disable)  // for POST requests via Postman
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                .build();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2A, 13);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler(securityEventRepository);
    }



    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint(adminService, securityEventRepository, appUserRepository);
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new DefaultAuthenticationEventPublisher(applicationEventPublisher);
    }


}
