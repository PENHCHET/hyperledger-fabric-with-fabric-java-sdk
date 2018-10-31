import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.security.CryptoPrimitives;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.StringWriter;
import java.security.PrivateKey;

public class RegisterNewUser {

    public static void main(String args[]) throws Exception{
        // Create a new CryptoSuite instance
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();

        // Create a new HFCAClient instance
        HFCAClient caClient = HFCAClient.createNewInstance("http://127.0.0.1:7054", null);

        // Set CryptoSuite
        caClient.setCryptoSuite(cryptoSuite);

        // Create a new Enrollment instance
        Enrollment adminEnrollment = caClient.enroll("admin", "admin");

        String newUsername = "PENHCHET";
        String newPassword = "123456";

        // User is not registered, Create a new RegistrationRequest
        RegistrationRequest registrationRequest = new  RegistrationRequest(newUsername, ".");
        registrationRequest.setSecret(newPassword);

        // Create the UserContext for Registrar
        UserContext registrarUserContext = new UserContext();
        registrarUserContext.setName("admin");
        registrarUserContext.setAccount("admin");
        registrarUserContext.setAffilation(".");
        registrarUserContext.setMspId("CooconMSP");
        registrarUserContext.setEnrollment(adminEnrollment);

        // Register New Username by Registrar Admin User
        String enrollSecret = caClient.register(registrationRequest, registrarUserContext);
        System.out.println(enrollSecret);

        // HFCA Client makes enrol call to ca server
        Enrollment enrollment = caClient.enroll(newUsername, enrollSecret);

        System.out.println("New User ==> " + newUsername + "  has been enrolled");

        UserContext newUser = new UserContext();
        newUser.setName(newUsername);
        newUser.setAccount(newUsername);
        newUser.setAffilation(".");
        newUser.setMspId("CooconMSP");
        newUser.setEnrollment(enrollment);

        // Convert the Private PEM Format
        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);
        pemWriter.writeObject(enrollment.getKey());
        pemWriter.close();

        // Can Store Enrollment in Database with Private Key with PEM/Binary Format and Certificate
        System.out.println("Name              ==> " + newUser.getName());
        System.out.println("Account           ==> " + newUser.getAccount());
        System.out.println("Affiliation       ==> " + newUser.getAffiliation());
        System.out.println("MspID             ==> " + newUser.getMspId());
        System.out.println("Private Key PEM   ==> " + pemStrWriter.toString());
        System.out.println("Private Key Bytes ==> " + pemStrWriter.toString().getBytes("UTF-8"));
        System.out.println("Certificate       ==> " + enrollment.getCert());

        CryptoPrimitives crypto = new CryptoPrimitives();

        // Convert PEM Private Key to PrivateKey Object
        PrivateKey privateKey = crypto.bytesToPrivateKey(pemStrWriter.toString().getBytes("UTF-8"));

        // Convert the Private PEM Format
        StringWriter pemStrWriter1 = new StringWriter();
        PEMWriter pemWriter1 = new PEMWriter(pemStrWriter1);
        pemWriter1.writeObject(privateKey);
        pemWriter1.close();

        // Can Store Enrollment in Database with Private Key with PEM/Binary Format and Certificate
        System.out.println("Name              ==> " + newUser.getName());
        System.out.println("Account           ==> " + newUser.getAccount());
        System.out.println("Affiliation       ==> " + newUser.getAffiliation());
        System.out.println("MspID             ==> " + newUser.getMspId());
        System.out.println("Private Key PEM   ==> " + pemStrWriter1.toString());
        System.out.println("Private Key Bytes ==> " + pemStrWriter1.toString().getBytes("UTF-8"));
        System.out.println("Certificate       ==> " + enrollment.getCert());


        String str= "-----BEGIN PRIVATE KEY-----\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg9kTuJitrC4I6QB+c\n" +
                "OwFZ/HGGt/kfAFmmUfD1goaUBuehRANCAAS1eQrnmGq1yk/Gh/gCqAn2IxbNMH3s\n" +
                "2OOXvYr9LDMCLowhmqCOlYLzvrptZwqPSK/vAEgRWc/D7MEzbuMkqqba\n" +
                "-----END PRIVATE KEY-----\n";


        PrivateKey privateKey1 = crypto.bytesToPrivateKey(str.getBytes("UTF-8"));
        String certification = "-----BEGIN CERTIFICATE-----\n" +
                "MIICJjCCAcygAwIBAgIRAPEVlPKiVQZzZNxoAuLVNBYwCgYIKoZIzj0EAwIwezEL\n" +
                "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
                "cmFuY2lzY28xHTAbBgNVBAoTFHdlYmNhc2gua3NocmQuY29tLmtoMSAwHgYDVQQD\n" +
                "ExdjYS53ZWJjYXNoLmtzaHJkLmNvbS5raDAeFw0xODA5MjcxNzM5MjVaFw0yODA5\n" +
                "MjQxNzM5MjVaMF8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYw\n" +
                "FAYDVQQHEw1TYW4gRnJhbmNpc2NvMSMwIQYDVQQDDBpVc2VyMUB3ZWJjYXNoLmtz\n" +
                "aHJkLmNvbS5raDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABB1aFK+JQ+V3Jw3J\n" +
                "FYCwr2q+PQq2u9y/UaokWzj070cdN1tC/7Z1SaHhdxFqDCPDWqtElaMPnM3RUCeN\n" +
                "jmDDThijTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1UdIwQk\n" +
                "MCKAIGMkiVq9Az4oo0HoFrVmEDPE7FprQoq2JvZ8vaQHkCjNMAoGCCqGSM49BAMC\n" +
                "A0gAMEUCIQC9Asgmy+O7yxrKLPxYtQuUvGpnXMD3YtgfkcfJZVFLCgIgLSINXj1B\n" +
                "x1WSYnHgmzv0gn4PjWFRr9GRUWMVJ11TIV4=\n" +
                "-----END CERTIFICATE-----\n";

        Enrollment enrollment1 = new UserEnrollment(privateKey1, certification);

        System.out.println(newUser.toString());

    }
}
