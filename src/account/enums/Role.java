package account.enums;

public enum Role {

    ANONYMOUS("ANONYMOUS"),
    USER("ROLE_USER"),
    ADMINISTRATOR("ROLE_ADMINISTRATOR"),
    ACCOUNTANT("ROLE_ACCOUNTANT"),
    AUDITOR("ROLE_AUDITOR");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public boolean isBusiness() {
        switch (this) {
            case ACCOUNTANT, USER, AUDITOR -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

}
