package account.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public interface AuthDTO {

    record RegistrationRequest(@NotBlank
                               String name,
                               @NotBlank
                               String lastname,
                               @NotBlank
                               @Email(regexp = ".*@acme.com")
                               String email,
                               @NotBlank
                               @Size(min = 12)
                               String password) {
    }

    record NewPasswordRequest(@JsonProperty("new_password")
                              @Size(min = 12, message = "Password length must be 12 chars minimum!")
                              String newPassword) {
    }

    record PasswordSuccessfulChangeResponse(String email, String status) {

    }
}
