package account.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public interface PaymentDTO {

    @JsonPropertyOrder({"name", "lastname", "period", "salary"})
    record UserPaymentResponse(String name,
                               @JsonProperty("lastname")
                               String lastName,
                               String period,
                               String salary) {
    }

    record PaymentInformationRequest(
            @NotBlank(message = "Employee email is required field!")
            String employee,
            @NotBlank(message = "Time period is required field!")
            @Pattern(regexp = "(0\\d|1[012])-\\d{4}")
            String period,
            @NotNull(message = "Salary is required field!")
            @PositiveOrZero
            Long salary) {
    }
}
