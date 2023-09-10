package account.dtos;

import account.enums.SecurityEventInfo;

import java.time.LocalDate;

public interface SecurityEventDTO {

    record SecurityEventResponse(SecurityEventInfo action, String subject, String object, String path) {}
}
