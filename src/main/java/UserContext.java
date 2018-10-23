import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.util.HashSet;
import java.util.Set;

public class UserContext implements User {

    private String name;
    private Set<String> roles = new HashSet<String>();
    private String account;
    private String affilation;
    private Enrollment enrollment;
    private String mspId;

    public void setName(String name) {
        this.name = name;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setAffilation(String affilation) {
        this.affilation = affilation;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public void setMspId(String mspId) {
        this.mspId = mspId;
    }

    public String getName() {
        return this.name;
    }

    public Set<String> getRoles() {
        return this.roles;
    }

    public String getAccount() {
        return this.account;
    }

    public String getAffiliation() {
        return this.affilation;
    }

    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    public String getMspId() {
        return this.mspId;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "name='" + name + '\'' +
                ", roles=" + roles +
                ", account='" + account + '\'' +
                ", affilation='" + affilation + '\'' +
                ", enrollment=" + enrollment +
                ", mspId='" + mspId + '\'' +
                '}';
    }
}
