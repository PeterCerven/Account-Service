package account.dtos;

public interface AdminDTO {

    record DeleteUserResponse(String user, String status) {}

    record AdminOperationRequest(String user, String role, String operation)  {}

    record LockUserRequest(String user, String operation) {}
}
