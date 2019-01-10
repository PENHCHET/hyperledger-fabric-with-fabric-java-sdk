import com.google.protobuf.ByteString;
import org.bouncycastle.asn1.x509.TBSCertList;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.HFCAIdentity;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.nio.charset.Charset;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.io.*;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;

public class ReadAttribute {

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

        registrationRequest.addAttribute(new Attribute("MSP_ID", "CooconMSP"));
        registrationRequest.addAttribute(new Attribute("NAME", "CooconMSP"));

        // Register New Username by Registrar Admin User
        String enrollSecret = caClient.register(registrationRequest, registrarUserContext);
        System.out.println("Password   : " + newPassword);
        System.out.println("Secret Key : " + enrollSecret);

        // HFCA Client makes enrol call to ca server
        Enrollment enrollment = caClient.enroll(newUsername, newPassword);

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

        int startedWithRevokes = -1;

        System.out.println("Start Revoked ==> " + startedWithRevokes);

        startedWithRevokes = ReadAttribute.getRevokes(null).length; //one more after we do this revoke.

        // revoke all enrollment of this user and request back a CRL
        String crl = caClient.revoke(registrarUserContext, newUser.getName(), null, true);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        InputStream inputStream = new ByteArrayInputStream(Charset.forName("UTF-8").encode(crl).array());
        X509CRL x509CRL = (X509CRL) cf.generateCRL(inputStream);

//        x509CRL.isRevoked()

        System.out.println("Start Revoked ==> " + startedWithRevokes);

        System.out.println(crl);
//        assertNotNull("Failed to get CRL using the Revoke API", crl);

        final int newRevokes = ReadAttribute.getRevokes(null).length;

        System.out.println(newRevokes);

        TBSCertList.CRLEntry crlEntries [] = ReadAttribute.getRevokes(null);

        for(TBSCertList.CRLEntry crlEntry: crlEntries){
            System.out.println(crlEntry);
            System.out.println(crlEntry.getExtensions());
            System.out.println(crlEntry.getRevocationDate());
            System.out.println(crlEntry.getUserCertificate());
            System.out.println(crlEntry.getEncoded());
            System.out.println(crlEntry.getEncoded("UTF-8"));
        }


//        String gencrl = caClient.revoke(registrarUserContext, newUsername, "Abnormally Action from this EID", true);

        HFCAIdentity hfcaIdentity = caClient.newHFCAIdentity(newUser.getName());

        hfcaIdentity.read(registrarUserContext);

        System.out.println("Enrollment ID==> " + hfcaIdentity.getEnrollmentId());
        System.out.println("Enrollment Secret ==> " + hfcaIdentity.getSecret());

        for(Attribute attribute : hfcaIdentity.getAttributes()){
            System.out.println(attribute.getName() + " = " + attribute.getValue());
        }

        System.out.println(crl);

        // Create a new HFClient instance
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(cryptoSuite);

        // HFClient needs to set the UserContext
        hfClient.setUserContext(newUser);

        // Create a new Channel instance
        Channel channel = hfClient.newChannel("mychannel");
        // Create a new Peer instance
        Peer peer = hfClient.newPeer("peer1.coocon.kshrd.com.kh", "grpc://127.0.0.1:8051");
        // Add a new Peer to Channel
        channel.addPeer(peer);

        // Create Orderer instances
        Orderer orderer2 = hfClient.newOrderer("orderer2.kshrd.com.kh", "grpc://127.0.0.1:8050");
        Orderer orderer3 = hfClient.newOrderer("orderer3.kshrd.com.kh", "grpc://127.0.0.1:9050");
        Orderer orderer1 = hfClient.newOrderer("orderer1.kshrd.com.kh", "grpc://127.0.0.1:7050");

        // Add Orderers instances to Channel
        channel.addOrderer(orderer1);
        channel.addOrderer(orderer2);
        channel.addOrderer(orderer3);

        // Initialized the Channel
        channel.initialize();

        // Create a new QueryByChaincodeRequest with newQueryProposalRequest
        QueryByChaincodeRequest qpr = hfClient.newQueryProposalRequest();

        // Build ChaincodID providing the ChaincodeName and Version.
        ChaincodeID ccID = ChaincodeID.newBuilder()
                .setName("kshrdsmartcontract")
                //.setVersion("1.1")
                .build();
        // Set ChaincodeID to QueryByChaincodeRequest
        qpr.setChaincodeID(ccID);

        // Set the Chaincode FunctionName to be called
        qpr.setFcn("queryHistoryByKey");

        // set Chaincode arguments to QueryByChaincodeRequest
        qpr.setArgs("a");

        // Query By Chaincode with QueryByChaincodeRequest
        Collection<ProposalResponse> responses = channel.queryByChaincode(qpr);

        // Loop through the ProposalResponses
        for (ProposalResponse response : responses) {
            // Check the Response is verified and ChaincodeResponse is Success
            if (response.isVerified() && response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                // Get the Payload from the Response()
                ByteString payload = response.getProposalResponse().getResponse().getPayload();
                // Show the Payload
                System.out.println(payload.toStringUtf8());
            } else {
                System.err.println("response failed. status: " + response.getStatus().getStatus());
            }
        }

//        CryptoPrimitives crypto = new CryptoPrimitives();
//
//        // Convert PEM Private Key to PrivateKey Object
//        PrivateKey privateKey = crypto.bytesToPrivateKey(pemStrWriter.toString().getBytes("UTF-8"));
//
//        // Convert the Private PEM Format
//        StringWriter pemStrWriter1 = new StringWriter();
//        PEMWriter pemWriter1 = new PEMWriter(pemStrWriter1);
//        pemWriter1.writeObject(privateKey);
//        pemWriter1.close();
//
//        // Can Store Enrollment in Database with Private Key with PEM/Binary Format and Certificate
//        System.out.println("Name              ==> " + newUser.getName());
//        System.out.println("Account           ==> " + newUser.getAccount());
//        System.out.println("Affiliation       ==> " + newUser.getAffiliation());
//        System.out.println("MspID             ==> " + newUser.getMspId());
//        System.out.println("Private Key PEM   ==> " + pemStrWriter1.toString());
//        System.out.println("Private Key Bytes ==> " + pemStrWriter1.toString().getBytes("UTF-8"));
//        System.out.println("Certificate       ==> " + enrollment.getCert());


//        String str= "-----BEGIN PRIVATE KEY-----\n" +
//                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQg9kTuJitrC4I6QB+c\n" +
//                "OwFZ/HGGt/kfAFmmUfD1goaUBuehRANCAAS1eQrnmGq1yk/Gh/gCqAn2IxbNMH3s\n" +
//                "2OOXvYr9LDMCLowhmqCOlYLzvrptZwqPSK/vAEgRWc/D7MEzbuMkqqba\n" +
//                "-----END PRIVATE KEY-----\n";
//
//
//        PrivateKey privateKey1 = crypto.bytesToPrivateKey(str.getBytes("UTF-8"));
//        String certification = "-----BEGIN CERTIFICATE-----\n" +
//                "MIICJjCCAcygAwIBAgIRAPEVlPKiVQZzZNxoAuLVNBYwCgYIKoZIzj0EAwIwezEL\n" +
//                "MAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNhbiBG\n" +
//                "cmFuY2lzY28xHTAbBgNVBAoTFHdlYmNhc2gua3NocmQuY29tLmtoMSAwHgYDVQQD\n" +
//                "ExdjYS53ZWJjYXNoLmtzaHJkLmNvbS5raDAeFw0xODA5MjcxNzM5MjVaFw0yODA5\n" +
//                "MjQxNzM5MjVaMF8xCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYw\n" +
//                "FAYDVQQHEw1TYW4gRnJhbmNpc2NvMSMwIQYDVQQDDBpVc2VyMUB3ZWJjYXNoLmtz\n" +
//                "aHJkLmNvbS5raDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABB1aFK+JQ+V3Jw3J\n" +
//                "FYCwr2q+PQq2u9y/UaokWzj070cdN1tC/7Z1SaHhdxFqDCPDWqtElaMPnM3RUCeN\n" +
//                "jmDDThijTTBLMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMCsGA1UdIwQk\n" +
//                "MCKAIGMkiVq9Az4oo0HoFrVmEDPE7FprQoq2JvZ8vaQHkCjNMAoGCCqGSM49BAMC\n" +
//                "A0gAMEUCIQC9Asgmy+O7yxrKLPxYtQuUvGpnXMD3YtgfkcfJZVFLCgIgLSINXj1B\n" +
//                "x1WSYnHgmzv0gn4PjWFRr9GRUWMVJ11TIV4=\n" +
//                "-----END CERTIFICATE-----\n";
//
//        Enrollment enrollment1 = new UserEnrollment(privateKey1, certification);
//
//        System.out.println(newUser.toString());

    }

    static TBSCertList.CRLEntry[]  getRevokes(Date r) throws Exception {

        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();

        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(cryptoSuite);


        // Create a new HFCAClient instance
        HFCAClient caClient = HFCAClient.createNewInstance("http://127.0.0.1:7054", null);

        // Set CryptoSuite
        caClient.setCryptoSuite(cryptoSuite);

        // Create a new Enrollment instance
        Enrollment adminEnrollment = caClient.enroll("admin", "admin");

        // Create the UserContext for Registrar
        UserContext admin = new UserContext();
        admin.setName("admin");
        admin.setAccount("admin");
        admin.setAffilation(".");
        admin.setMspId("CooconMSP");
        admin.setEnrollment(adminEnrollment);

        hfClient.setUserContext(admin);



        // HFClient needs to set the UserContext


        // Create a new Channel instance
        Channel channel = hfClient.newChannel("mychannel");
        // Create a new Peer instance
        Peer peer = hfClient.newPeer("peer1.coocon.kshrd.com.kh", "grpc://127.0.0.1:8051");
        // Add a new Peer to Channel
        channel.addPeer(peer);

        // Create Orderer instances
        Orderer orderer2 = hfClient.newOrderer("orderer2.kshrd.com.kh", "grpc://127.0.0.1:8050");
        Orderer orderer3 = hfClient.newOrderer("orderer3.kshrd.com.kh", "grpc://127.0.0.1:9050");
        Orderer orderer1 = hfClient.newOrderer("orderer1.kshrd.com.kh", "grpc://127.0.0.1:7050");

        // Add Orderers instances to Channel
        channel.addOrderer(orderer1);
        channel.addOrderer(orderer2);
        channel.addOrderer(orderer3);

        // Initialized the Channel
        channel.initialize();

        String crl = caClient.generateCRL(admin, r, null, null, null);

        return parseCRL(crl);
    }

    static TBSCertList.CRLEntry[] parseCRL(String crl) throws Exception {

        Base64.Decoder b64dec = Base64.getDecoder();
        final byte[] decode = b64dec.decode(crl.getBytes("UTF-8"));

        PEMParser pem = new PEMParser(new StringReader(new String(decode)));
        X509CRLHolder holder = (X509CRLHolder) pem.readObject();

        return holder.toASN1Structure().getRevokedCertificates();
    }

    private X509Certificate getCert(byte[] certBytes) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(certBytes));
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(bufferedInputStream);

        certificate.getEncoded();
        return certificate;
    }
}
