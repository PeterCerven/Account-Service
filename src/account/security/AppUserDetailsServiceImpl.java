package account.security;

import account.exceptions.NotFoundUserException;
import account.models.AppUser;
import account.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppUserDetailsServiceImpl implements UserDetailsService {

    private final AppUserRepository repository;

    @Autowired
    public AppUserDetailsServiceImpl(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws NotFoundUserException {
        log.info("in AppUserDetailsServiceImpl");
        AppUser user = repository
                .findAppUserByEmailIgnoreCase(username)
                .orElseThrow(() -> new NotFoundUserException("Not found", username));

        log.info(user.toString());

        return new AppUserAdapter(user);
    }
}
