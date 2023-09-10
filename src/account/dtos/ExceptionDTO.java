package account.dtos;

import java.time.LocalDateTime;

public interface ExceptionDTO {
    record CustomBadResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
    }
}
