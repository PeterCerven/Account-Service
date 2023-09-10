package account.controllers;

import account.dtos.AuthDTO.NewPasswordRequest;
import account.dtos.GeneralDTO.UserResponse;
import account.dtos.AuthDTO.PasswordSuccessfulChangeResponse;
import account.service.AuthenticationService;
import account.dtos.AuthDTO.RegistrationRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthenticationController {

    private final AuthenticationService authService;

    @Autowired
    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody RegistrationRequest appUser,
                                               HttpServletRequest httpServletRequest) {
        return new ResponseEntity<>(authService.signUp(appUser, httpServletRequest) ,HttpStatus.OK);
    }

    @PostMapping("/changepass")
    public ResponseEntity<PasswordSuccessfulChangeResponse> changePassword(
            @RequestBody @Valid NewPasswordRequest newPasswordRequest, HttpServletRequest httpServletRequest) {
        return new ResponseEntity<>
                (authService.changePassword(newPasswordRequest.newPassword(), httpServletRequest), HttpStatus.OK);
    }

}
