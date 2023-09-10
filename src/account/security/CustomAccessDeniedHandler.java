package account.security;

import account.dtos.ExceptionDTO.CustomBadResponse;
import account.models.SecurityEvent;
import account.repository.SecurityEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

import static account.enums.SecurityEventInfo.ACCESS_DENIED;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityEventRepository secEventRep;

    @Autowired
    public CustomAccessDeniedHandler(SecurityEventRepository secEventRep) {
        this.secEventRep = secEventRep;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ade) throws IOException, ServletException {

        log.info("In CustomAccessDeniedHandler");

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        String encodedCredentials = request.getHeader("authorization").replace("Basic", "").trim();
        String email = new String(Base64.getDecoder().decode(encodedCredentials)).split(":")[0];



        CustomBadResponse cbr = new CustomBadResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Access Denied!",
                request.getRequestURI()
        );

        this.secEventRep.save(new SecurityEvent(
                LocalDate.now(),
                ACCESS_DENIED,
                email,
                request.getRequestURI(),
                request.getRequestURI()
        ));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String jsonResponse = objectMapper.writeValueAsString(cbr);
        response.getWriter().write(jsonResponse);

    }
}
