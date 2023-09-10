package account.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@ToString
public class PaymentInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "appUser_id")
    @JsonProperty("employee")
    private AppUser appUser;
    @NotBlank
    @Pattern(regexp = "\\w+-\\d{4}")
    private String period;
    @PositiveOrZero
    @NotNull
    private Long salary;

    public PaymentInformation(AppUser appUser, String period, Long salary) {
        this.appUser = appUser;
        this.period = period;
        this.salary = salary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PaymentInformation that = (PaymentInformation) o;

        if (!getAppUser().equals(that.getAppUser())) return false;
        return getPeriod().equals(that.getPeriod());
    }

    @Override
    public int hashCode() {
        int result = getAppUser().hashCode();
        result = 31 * result + getPeriod().hashCode();
        return result;
    }
}
