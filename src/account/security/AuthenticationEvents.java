package account.security;

import account.dtos.ExceptionDTO;
import account.models.AppUser;
import account.models.SecurityEvent;
import account.repository.AppUserRepository;
import account.repository.SecurityEventRepository;
import account.service.AdminService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static account.enums.SecurityEventInfo.*;

@Slf4j
@Component
public class AuthenticationEvents {

    private final AdminService adminService;
    private final SecurityEventRepository secEventRep;
    private final AppUserRepository appUserRepository;

    @Autowired
    public AuthenticationEvents(AdminService adminService, SecurityEventRepository secEventRep,
                                          AppUserRepository appUserRepository) {
        this.adminService = adminService;
        this.secEventRep = secEventRep;
        this.appUserRepository = appUserRepository;
    }


    @EventListener
    public void onSuccess(AuthenticationSuccessEvent success) {
        log.info("In SUCCESSFUL LOGIN");
        AppUserAdapter userDetails = (AppUserAdapter) success.getAuthentication().getPrincipal();
        AppUser appUser = userDetails.getAppUser();
        appUser.setFailedAttempt(0);
        this.appUserRepository.save(appUser);
    }
}
