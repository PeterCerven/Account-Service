package account.exceptions;

import account.dtos.ExceptionDTO.CustomBadResponse;
import account.repository.SecurityEventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class AccExceptionController {


    private final SecurityEventRepository securityEventRepository;

    @Autowired
    public AccExceptionController(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomBadResponse handleBadRequestException(BadRequestException ex,
                                                       HttpServletRequest httpRequest) {

        return new CustomBadResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                httpRequest.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomBadResponse handleBadRequestMethodArgument(MethodArgumentNotValidException ex,
                                                            HttpServletRequest httpRequest) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation error occurred!");

        return new CustomBadResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                errorMessage,
                httpRequest.getRequestURI()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomBadResponse handleBadRequestConstraintViolation(ConstraintViolationException ex,
                                                                 HttpServletRequest httpRequest) {

        String errorMessage = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation error occurred!");


        return new CustomBadResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                errorMessage,
                httpRequest.getRequestURI()
        );
    }

    @ExceptionHandler(NotFoundUserException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public CustomBadResponse handleNotFoundUserException(NotFoundUserException ex,
                                                         HttpServletRequest httpRequest) {

        log.info("In ExceptionHandler handling NotFoundUserException");

//        this.securityEventRepository.save(new SecurityEvent(
//                LocalDate.now(),
//                LOGIN_FAILED,
//                ex.getEmail(),
//                httpRequest.getRequestURI(),
//                httpRequest.getRequestURI()
//        ));

        return new CustomBadResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                httpRequest.getRequestURI()
        );
    }

}
