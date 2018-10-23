import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.StringWriter;

public class RegisterNewUser {

    public static void main(String args[]) throws Exception{
        // 1. Create a new CryptoSuite instance
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();

        // 2. Create a new HFCAClient instance
        HFCAClient caClient = HFCAClient.createNewInstance("http://127.0.0.1:7054", null);

        // 2.1. Set CryptoSuite
        caClient.setCryptoSuite(cryptoSuite);

        // 3. Create a new Enrollment instance
        Enrollment adminEnrollment = caClient.enroll("admin", "admin");

        String newUsername = "DARA_PENHCHET5";

        // 4. User is not registered, Create a new RegistrationRequest
        RegistrationRequest registrationRequest = new  RegistrationRequest(newUsername, ".");

        // 5. Create the UserContext for Registrar
        UserContext registrarUserContext = new UserContext();
        registrarUserContext.setName("admin");
        registrarUserContext.setAccount("admin");
        registrarUserContext.setAffilation(".");
        registrarUserContext.setMspId("CooconMSP");
        registrarUserContext.setEnrollment(adminEnrollment);

        // 6. Register New Username by Registrar Admin User
        String enrollSecret = caClient.register(registrationRequest, registrarUserContext);
        System.out.println(enrollSecret);

        // 7. HFCA Client makes enrol call to ca server
        Enrollment enrollment = caClient.enroll(newUsername, enrollSecret);

        System.out.println("New User ==> " + newUsername + "  has been enrolled");

        UserContext newUser = new UserContext();
        newUser.setName(newUsername);
        newUser.setAccount(newUsername);
        newUser.setAffilation(".");
        newUser.setMspId("CooconMSP");
        newUser.setEnrollment(enrollment);

        // 8. Convert the Private PEM Format
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);
        pemWriter.writeObject(enrollment.getKey());
        pemWriter.close();

        // 9. Can Store Enrollment in Database with Private Key with PEM/Binary Format and Certificate
        System.out.println("Name              ==> " + newUser.getName());
        System.out.println("Account           ==> " + newUser.getAccount());
        System.out.println("Affiliation       ==> " + newUser.getAffiliation());
        System.out.println("MspID             ==> " + newUser.getMspId());
        System.out.println("Private Key PEM   ==> " + pemStrWriter.toString());
        System.out.println("Private Key Bytes ==> " + pemStrWriter.toString().getBytes("UTF-8"));
        System.out.println("Certificate       ==> " + enrollment.getCert());

        System.out.println(newUser.toString());

    }
}
