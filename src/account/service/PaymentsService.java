package account.service;

import account.exceptions.BadRequestException;
import account.models.AppUser;
import account.models.PaymentInformation;
import account.dtos.PaymentDTO.PaymentInformationRequest;
import account.dtos.GeneralDTO.ResponseCustomStatus;
import account.dtos.PaymentDTO.UserPaymentResponse;
import account.repository.AppUserRepository;
import account.repository.PaymentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@Transactional
public class PaymentsService {

    private final ModelMapper modelMapper;
    private final PaymentsRepository paymentsRepository;
    private final AppUserRepository appUserRepository;

    @Autowired
    public PaymentsService(ModelMapper modelMapper, PaymentsRepository paymentsRepository, AppUserRepository appUserRepository) {
        this.modelMapper = modelMapper;
        this.paymentsRepository = paymentsRepository;
        this.appUserRepository = appUserRepository;
    }

    public UserPaymentResponse getEmployeePayroll(AppUser appUser, String period) {
        log.info("getEmployeePayroll");
        PaymentInformation paymentInformation = this.paymentsRepository.findByAppUserAndPeriod(appUser, period)
                .orElseThrow(() -> new BadRequestException("Couldn't find payment with this time!"));

        String localDate = getMonthYearDate(period);
        String salary = convertSalaryToWordForm(paymentInformation.getSalary());

        return new UserPaymentResponse(appUser.getName(), appUser.getLastName(), localDate, salary);
    }

    public List<UserPaymentResponse> getEmployeePayroll(AppUser appUser) {
        List<PaymentInformation> payments = this.paymentsRepository.findAllByAppUserOrderByPeriodDesc(appUser);


        return payments.stream()
                .map(payment -> new UserPaymentResponse(appUser.getName(), appUser.getLastName(),
                        getMonthYearDate(payment.getPeriod()), convertSalaryToWordForm(payment.getSalary())))
                .toList();
    }

    private String convertSalaryToWordForm(Long salary) {
        return String.format("%s dollar(s) %s cent(s)", salary / 100, salary % 100);
    }


    public ResponseCustomStatus uploadsPayrolls(List<PaymentInformationRequest> payments) {
        log.info("uploadsPayrolls");
        List<PaymentInformation> alreadyAcceptedPayments = new ArrayList<>();
        for (PaymentInformationRequest request : payments) {

            AppUser appUser = appUserRepository.findAppUserByEmailIgnoreCase(request.employee())
                    .orElseThrow(() -> new BadRequestException("This user doesnt exist! Wrong payment request!"));

            PaymentInformation paymentInformation = new PaymentInformation(appUser, request.period(), request.salary());
            if (alreadyAcceptedPayments.contains(paymentInformation)) {
                throw new BadRequestException("This payment has the same date!");
            }
            alreadyAcceptedPayments.add(paymentInformation);
            this.paymentsRepository.save(paymentInformation);
        }
        return new ResponseCustomStatus("Added successfully!");
    }

    public ResponseCustomStatus changeSalary(PaymentInformationRequest request) {
        log.info("changeSalary");
        AppUser appUser = appUserRepository.findAppUserByEmailIgnoreCase(request.employee())
                .orElseThrow(() -> new BadRequestException("This user doesnt exist! Wrong payment request!"));

        PaymentInformation paymentInformation = this.paymentsRepository.findByAppUserAndPeriod(appUser, request.period())
                .orElseThrow(() -> new BadRequestException("Couldn't find the payment!"));
        paymentInformation.setSalary(request.salary());
        this.paymentsRepository.save(paymentInformation);

        return new ResponseCustomStatus("Updated successfully!");
    }

    private String getMonthYearDate(String period) {
        try {
            LocalDate localDate = YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy")).atDay(1);
            String month = localDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            int year = localDate.getYear();
            return month + "-" + year;
        } catch (DateTimeParseException e) {
            throw new BadRequestException("The format of date is wrong!");
        }
    }


}
