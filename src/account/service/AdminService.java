package account.service;

import account.dtos.AdminDTO.LockUserRequest;
import account.dtos.GeneralDTO.ResponseCustomStatus;
import account.enums.Role;
import account.enums.SecurityEventInfo;
import account.exceptions.BadRequestException;
import account.exceptions.NotFoundUserException;
import account.models.AppUser;
import account.models.Group;
import account.dtos.AdminDTO.DeleteUserResponse;
import account.dtos.GeneralDTO.UserResponse;
import account.dtos.AdminDTO.AdminOperationRequest;
import account.models.SecurityEvent;
import account.repository.AppUserRepository;
import account.repository.GroupRepository;
import account.enums.Operation;
import account.repository.SecurityEventRepository;
import account.security.AppUserAdapter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static account.enums.Role.ADMINISTRATOR;
import static account.enums.SecurityEventInfo.*;

@Service
@Slf4j
public class AdminService {

    private final AppUserRepository userRepository;
    private final GroupRepository groupRepository;
    private final SecurityEventRepository securityEventRepository;


    @Autowired
    public AdminService(AppUserRepository userRepository, GroupRepository groupRepository,
                        SecurityEventRepository securityEventRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.securityEventRepository = securityEventRepository;
    }

    public List<UserResponse> getUsersInfo() {
        log.info("getUsersInfo");
        List<AppUser> appUsers = (List<AppUser>) this.userRepository.findAll();
        return appUsers.stream()
                .map(this::getRoleResponseFromUser)
                .toList();
    }

    public DeleteUserResponse deletesUser(String email, HttpServletRequest servletRequest) {
        log.info("deletesUser");
        AppUser appUser = this.userRepository.findAppUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundUserException("User not found!", email));

        if (appUser.getGroups().contains(new Group(ADMINISTRATOR.getRoleName()))) {
            throw new BadRequestException("Can't remove ADMINISTRATOR role!");
        }

        AppUser currentUser = getCurrentUser();

        this.userRepository.delete(appUser);
        this.securityEventRepository.save(new SecurityEvent(
                LocalDate.now(),
                DELETE_USER,
                currentUser.getEmail(),
                email,
                servletRequest.getRequestURI()
        ));

        return new DeleteUserResponse(email, "Deleted successfully!");
    }

    public UserResponse changeUserRole(AdminOperationRequest request, HttpServletRequest servletRequest) {
        String operation = request.operation();
        String role = request.role();
        String userEmail = request.user();
        String preposition = "";
        log.info("changeUserRole: " + String.format("%s role %s to %s", operation, role,userEmail));

        SecurityEventInfo securityEventInfo = SecurityEventInfo.GRANT_ROLE;

        if (role.equalsIgnoreCase(ADMINISTRATOR.getRoleName())) {
            throw new BadRequestException("Cannot Remove Admin role!");
        }
        AppUser appUser = this.userRepository.findAppUserByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new NotFoundUserException("User not found!", userEmail));
        Group group = this.groupRepository.findByNameIgnoreCase("ROLE_" + role)
                .orElseThrow(() -> new NotFoundUserException("Role not found!", userEmail));

        if (Operation.valueOf(operation).equals(Operation.GRANT)) {
            preposition = "to";
            appUser.getGroups().add(group);
            if (appUser.getGroups().size() >= 2 && appUser.getGroups().contains(new Group(ADMINISTRATOR.getRoleName()))) {
                throw new BadRequestException("The user cannot combine administrative and business roles!");
            }

        } else if (Operation.valueOf(operation).equals(Operation.REMOVE)) {
            securityEventInfo = SecurityEventInfo.REMOVE_ROLE;
            preposition = "from";
            if (group.getName().equalsIgnoreCase(ADMINISTRATOR.getRoleName())) {
                throw new BadRequestException("Can't remove ADMINISTRATOR role!");
            }
            if (!appUser.getGroups().remove(group)) {
                throw new BadRequestException("The user does not have a role!");
            }
            if (appUser.getGroups().isEmpty()) {
                throw new BadRequestException("The user must have at least one role!");
            }

        } else {
            throw new BadRequestException("Wrong operation!");
        }
        this.userRepository.save(appUser);

        AppUser currentUser = getCurrentUser();
        this.securityEventRepository.save(new SecurityEvent(
                LocalDate.now(),
                securityEventInfo,
                currentUser.getEmail(),
                String.format("%s role %s %s %s", capitalize(operation), role, preposition,userEmail.toLowerCase()),
                servletRequest.getRequestURI()
        ));

        return getRoleResponseFromUser(appUser);
    }

    public ResponseCustomStatus changeLockUser(LockUserRequest request, HttpServletRequest servletRequest) {
        log.info(request.operation() + " " + request.user());
        AppUser appUser = this.userRepository.findAppUserByEmailIgnoreCase(request.user())
                .orElseThrow(() -> new NotFoundUserException("User not found!", request.user()));
        if (appUser.getGroups().contains(new Group(ADMINISTRATOR.getRoleName()))) {
            throw new BadRequestException("Can't lock the ADMINISTRATOR!");
        }
        switch (Operation.valueOf(request.operation())) {
            case LOCK -> lock(getCurrentUser(), appUser, servletRequest);
            case UNLOCK -> unLock(getCurrentUser(), appUser, servletRequest);
            default -> throw new BadRequestException("No such operation!");
        }

        return new ResponseCustomStatus(String.format("User %s %s!", request.user().toLowerCase(), request.operation().toLowerCase() + "ed"));
    }

    private UserResponse getRoleResponseFromUser(AppUser user) {
        List<String> roles = user.getGroups().stream()
                .map(Group::getName)
                .sorted()
                .toList();
        return new UserResponse(user.getId(), user.getName(), user.getLastName(), user.getEmail(), roles);
    }

    public void resetFailedAttempts(AppUser appUser) {
        log.info("resetFailedAttempts");
        appUser.setFailedAttempt(0);
        appUser.setAccountNonLocked(true);
        this.userRepository.save(appUser);
    }

    public void lock(AppUser userLocking,  AppUser toBeLocked, HttpServletRequest servletRequest) {
        log.info("lockUser");
        if (toBeLocked.getGroups().contains(new Group(ADMINISTRATOR.getRoleName()))) {
            log.info("Cant lock admin!");
            return;
        }

        toBeLocked.setAccountNonLocked(false);
        this.userRepository.save(toBeLocked);

        this.securityEventRepository.save(new SecurityEvent(
                LocalDate.now(),
                LOCK_USER,
                userLocking.getEmail(),
                "Lock user " + toBeLocked.getEmail(),
                servletRequest.getRequestURI()
        ));
    }

    public void unLock(AppUser userUnlocking,  AppUser locked, HttpServletRequest servletRequest) {
        log.info("unLockUser");
        resetFailedAttempts(locked);

        this.securityEventRepository.save(new SecurityEvent(
                LocalDate.now(),
                UNLOCK_USER,
                userUnlocking.getEmail(),
                "Unlock user " + locked.getEmail(),
                servletRequest.getRequestURI()
        ));
    }

    public AppUser increaseFailedAttempts(AppUser appUser) {
        log.info("increaseFailedAttempts");
        appUser.setFailedAttempt(appUser.getFailedAttempt() + 1);
        return this.userRepository.save(appUser);
    }


    private AppUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUserAdapter appUserAdapter = (AppUserAdapter) auth.getPrincipal();
        return appUserAdapter.getAppUser();
    }

    private String capitalize(String word) {
        String lowercase = word.toLowerCase();
        return lowercase.substring(0,1).toUpperCase() + lowercase.substring(1);
    }
}
