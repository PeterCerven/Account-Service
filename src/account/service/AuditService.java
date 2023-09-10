package account.service;

import account.dtos.SecurityEventDTO.SecurityEventResponse;
import account.models.SecurityEvent;
import account.repository.SecurityEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AuditService {

    private final SecurityEventRepository ser;

    @Autowired
    public AuditService(SecurityEventRepository ser) {
        this.ser = ser;
    }

    public List<SecurityEventResponse> getSecurityEvents() {
        log.info("getSecurityEvents");
        List<SecurityEvent> securityEvents = (List<SecurityEvent>) ser.findAll();
        return securityEvents.stream()
                .map(event -> new SecurityEventResponse(event.getAction(), event.getSubject(),
                        event.getObject(), event.getPath()))
                .toList();
    }
}
