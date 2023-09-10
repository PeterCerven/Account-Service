package account.repository;

import account.models.SecurityEvent;
import org.springframework.data.repository.CrudRepository;

public interface SecurityEventRepository extends CrudRepository<SecurityEvent, Long> {
}
