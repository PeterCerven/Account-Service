package account.utility;

import account.models.Group;
import account.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static account.enums.Role.*;

@Component
public class DataLoader {

    private final GroupRepository groupRepository;

    @Autowired
    public DataLoader(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
        if (this.groupRepository.count() == 0) {
            createRoles();
        }
    }

    private void createRoles() {
        try {
            groupRepository.save(new Group(ADMINISTRATOR.getRoleName()));
            groupRepository.save(new Group(USER.getRoleName()));
            groupRepository.save(new Group(ACCOUNTANT.getRoleName()));
            groupRepository.save(new Group(AUDITOR.getRoleName()));
        } catch (Exception e) {
            System.out.println("ss");
            System.out.println(e.getMessage());
        }
    }
}
