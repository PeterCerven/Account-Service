package account.controllers;

import account.dtos.AdminDTO.LockUserRequest;
import account.dtos.AdminDTO.DeleteUserResponse;
import account.dtos.GeneralDTO.ResponseCustomStatus;
import account.dtos.GeneralDTO.UserResponse;
import account.dtos.AdminDTO.AdminOperationRequest;
import account.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PutMapping("/user/role")
    public ResponseEntity<UserResponse> changeUserRole(@RequestBody AdminOperationRequest request,
                                                       HttpServletRequest servletRequest) {
        return new ResponseEntity<>(adminService.changeUserRole(request, servletRequest), HttpStatus.OK);
    }

    @DeleteMapping("/user/{email}")
    public ResponseEntity<DeleteUserResponse> deletesUser(@PathVariable String email, HttpServletRequest servletRequest) {
        return new ResponseEntity<>(adminService.deletesUser(email, servletRequest), HttpStatus.OK);
    }

    @GetMapping("/user/")
    public ResponseEntity<List<UserResponse>> getUsersInfo() {
        return new ResponseEntity<>(adminService.getUsersInfo(), HttpStatus.OK);
    }

    @PutMapping("/user/access")
    public ResponseEntity<ResponseCustomStatus> changeLockOnUser(@RequestBody LockUserRequest request,  HttpServletRequest servletRequest) {
        return new ResponseEntity<>(adminService.changeLockUser(request, servletRequest), HttpStatus.OK);
    }
}
