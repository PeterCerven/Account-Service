package account.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
@Getter
public class NotFoundUserException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1;
    private final String email;
    public NotFoundUserException(String message, String email) {
        super(message);
        this.email = email;
    }
}
