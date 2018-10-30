import org.hyperledger.fabric.sdk.Enrollment;

import java.security.PrivateKey;

public class UserEnrollment implements Enrollment {

    private PrivateKey privateKey;
    private String cert;

    public UserEnrollment(PrivateKey privateKey, String cert) {
        this.privateKey = privateKey;
        this.cert = cert;
    }

    @Override
    public PrivateKey getKey() {
        return privateKey;
    }

    @Override
    public String getCert() {
        return cert;
    }
}
