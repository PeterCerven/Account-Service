package account.security;

import account.dtos.ExceptionDTO;
import account.dtos.ExceptionDTO.CustomBadResponse;
import account.models.AppUser;
import account.models.SecurityEvent;
import account.repository.AppUserRepository;
import account.repository.SecurityEventRepository;
import account.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static account.enums.SecurityEventInfo.*;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {


    private final AdminService adminService;
    private final SecurityEventRepository secEventRep;
    private final AppUserRepository appUserRepository;

    @Autowired
    public CustomAuthenticationEntryPoint(AdminService adminService, SecurityEventRepository secEventRep,
                                          AppUserRepository appUserRepository) {
        this.adminService = adminService;
        this.secEventRep = secEventRep;
        this.appUserRepository = appUserRepository;
    }




    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {
        log.info("In CustomAuthenticationEntryPoint");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (request.getHeader("authorization") == null) {
            return;
        }

        String encodedCredentials = request.getHeader("authorization").replace("Basic", "").trim();
        String email = new String(Base64.getDecoder().decode(encodedCredentials)).split(":")[0];


        Optional<AppUser> user = appUserRepository.findAppUserByEmailIgnoreCase(email);


        if (user.isEmpty()) {
            log.info("Username doesnt exist!");
            log.warn("LOGIN FAILED");
            this.secEventRep.save(new SecurityEvent(
                    LocalDate.now(),
                    LOGIN_FAILED,
                    email,
                    request.getRequestURI(),
                    request.getRequestURI()
            ));
            return;
        }

        AppUser appUser = user.get();


        if (appUser.isAccountNonLocked()) {
            log.warn("LOGIN FAILED");
            this.secEventRep.save(new SecurityEvent(
                    LocalDate.now(),
                    LOGIN_FAILED,
                    email,
                    request.getRequestURI(),
                    request.getRequestURI()
            ));
            appUser = adminService.increaseFailedAttempts(appUser);

            if (appUser.getFailedAttempt() > 4) {
                log.info("Brute Force");
                this.secEventRep.save(new SecurityEvent(
                        LocalDate.now(),
                        BRUTE_FORCE,
                        email,
                        request.getRequestURI(),
                        request.getRequestURI()
                ));
                log.info("Lock user");
                adminService.lock(appUser, appUser, request);
            }
        }
        ExceptionDTO.CustomBadResponse cbr = new ExceptionDTO.CustomBadResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "User account is locked",
                request.getRequestURI()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String jsonResponse = objectMapper.writeValueAsString(cbr);
        response.getWriter().write(jsonResponse);


    }
}
