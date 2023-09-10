package account.service;


import account.enums.SecurityEventInfo;
import account.models.Group;
import account.dtos.GeneralDTO.UserResponse;
import account.dtos.AuthDTO.PasswordSuccessfulChangeResponse;
import account.dtos.AuthDTO.RegistrationRequest;
import account.models.AppUser;
import account.models.SecurityEvent;
import account.repository.AppUserRepository;
import account.exceptions.BadRequestException;
import account.repository.GroupRepository;
import account.repository.SecurityEventRepository;
import account.security.AppUserAdapter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static account.enums.Role.ADMINISTRATOR;
import static account.enums.Role.USER;
import static account.enums.SecurityEventInfo.CHANGE_PASSWORD;
import static account.enums.SecurityEventInfo.CREATE_USER;

@Slf4j
@Service
public class AuthenticationService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SecurityEventRepository securityEventRepository;
    private final List<String> breachedPasswords = List.of(
                    "PasswordForJanuary", "PasswordForFebruary", "PasswordForMarch", "PasswordForApril",
                    "PasswordForMay", "PasswordForJune", "PasswordForJuly", "PasswordForAugust", "PasswordForSeptember",
                    "PasswordForOctober", "PasswordForNovember", "PasswordForDecember"
            );


    @Autowired
    public AuthenticationService(ModelMapper modelMapper, AppUserRepository userRepository,
                                 PasswordEncoder passwordEncoder, GroupRepository groupRepository,
                                 SecurityEventRepository securityEventRepository) {
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.groupRepository = groupRepository;
        this.securityEventRepository = securityEventRepository;
    }

    public PasswordSuccessfulChangeResponse changePassword(String newPassword, HttpServletRequest servletRequest) {
        log.info("changePassword");
        if (breachedPasswords.contains(newPassword)) {
            throw new BadRequestException("The password is in the hacker's database!");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUserAdapter appUserAdapter = (AppUserAdapter) auth.getPrincipal();
        AppUser appUser = appUserAdapter.getAppUser();

        if (passwordEncoder.matches(newPassword, appUser.getPassword())) {
            throw new BadRequestException("The passwords must be different!");
        }


        appUser.setPassword(passwordEncoder.encode(newPassword));
        this.userRepository.save(appUser);
        this.securityEventRepository.save(new SecurityEvent(
                LocalDate.now(),
                CHANGE_PASSWORD,
                appUser.getEmail(),
                appUser.getEmail(),
                servletRequest.getRequestURI()
        ));

        return new PasswordSuccessfulChangeResponse(appUser.getEmail(), "The password has been updated successfully");
    }

    public UserResponse signUp(@Valid RegistrationRequest request, HttpServletRequest servletRequest) {
        log.info("signUp");
        if (breachedPasswords.contains(request.password())) {
            throw new BadRequestException("The password is in the hacker's database!");
        }
        if (this.userRepository.findAppUserByEmailIgnoreCase(request.email()).isPresent()) {
            throw new BadRequestException("User exist!");
        }

        Group group = userRepository.count() == 0
                ? groupRepository.findByNameIgnoreCase(ADMINISTRATOR.getRoleName()).orElseThrow()
                : groupRepository.findByNameIgnoreCase(USER.getRoleName()).orElseThrow();

        AppUser appUser = AppUser.builder()
                .name(request.name())
                .lastName(request.lastname())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .groups(List.of(group))
                .failedAttempt(0)
                .accountNonLocked(true)
                .build();
        log.info(appUser.toString());

        List<String> roles = appUser.getGroups().stream()
                .map(Group::getName)
                .sorted()
                .toList();



        this.userRepository.save(appUser);
        this.securityEventRepository.save(new SecurityEvent(
                LocalDate.now(),
                CREATE_USER,
                "Anonymous",
                appUser.getEmail(),
                servletRequest.getRequestURI()
        ));
        return new UserResponse(appUser.getId(), appUser.getName(), appUser.getLastName(), appUser.getEmail(), roles);
    }



}
