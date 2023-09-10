package account.security;

import account.models.AppUser;
import account.models.Group;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static account.enums.Role.ADMINISTRATOR;

@Slf4j
@Getter
public class AppUserAdapter implements UserDetails {

    private final AppUser appUser;

    public AppUserAdapter(AppUser appUser) {
        this.appUser = appUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info(appUser.getEmail() + ":");
        for (Group group : appUser.getGroups()) {
            SimpleGrantedAuthority sa = new SimpleGrantedAuthority(group.getName());
            log.info("Group name: "  + sa);
        }
        return appUser.getGroups().stream().map(g -> new SimpleGrantedAuthority(g.getName())).toList();
    }


    @Override
    public String getPassword() {
        return appUser.getPassword();
    }


    @Override
    public String getUsername() {
        return appUser.getEmail();
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }


    @Override
    public boolean isAccountNonLocked() {
        return appUser.isAccountNonLocked();
    }


    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }


    @Override
    public boolean isEnabled() {
        return true;
    }
}
