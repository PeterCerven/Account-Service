package account.repository;

import account.models.AppUser;
import account.models.PaymentInformation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends CrudRepository<PaymentInformation, Long> {

    Optional<PaymentInformation> findByAppUserAndPeriod(AppUser appUser, String period);

    List<PaymentInformation> findAllByAppUserOrderByPeriodDesc(AppUser appUser);
}
