package account.dtos;

import java.util.List;

public interface GeneralDTO {

    record ResponseCustomStatus(String status) {
    }

    record UserResponse(long id, String name, String lastname, String email, List<String> roles) {
    }


}
