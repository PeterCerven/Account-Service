package account.controllers;

import account.dtos.PaymentDTO.PaymentInformationRequest;
import account.dtos.GeneralDTO.ResponseCustomStatus;
import account.security.AppUserAdapter;
import account.service.PaymentsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/api")
public class PaymentsController {

    private final PaymentsService payService;

    @Autowired
    public PaymentsController(PaymentsService payService) {
        this.payService = payService;
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<?> getEmployeePayroll(@RequestParam(required = false) String period,
                                                                                @AuthenticationPrincipal AppUserAdapter details) {
        return period == null
                ? new ResponseEntity<>(this.payService.getEmployeePayroll(details.getAppUser()), HttpStatus.OK)
                : new ResponseEntity<>(this.payService.getEmployeePayroll(details.getAppUser(), period), HttpStatus.OK);


    }

    @PostMapping("/acct/payments")
    public ResponseEntity<ResponseCustomStatus> uploadsPayrolls(@RequestBody List<@Valid PaymentInformationRequest> payments) {
        return new ResponseEntity<>(payService.uploadsPayrolls(payments), HttpStatus.OK);
    }

    @PutMapping("/acct/payments")
    public ResponseEntity<ResponseCustomStatus> changeSalary(@RequestBody @Valid PaymentInformationRequest request) {
        return new ResponseEntity<>(payService.changeSalary(request), HttpStatus.OK);
    }
}
