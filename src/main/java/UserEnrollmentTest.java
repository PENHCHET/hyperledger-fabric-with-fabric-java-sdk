import com.google.protobuf.ByteString;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoPrimitives;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.security.PrivateKey;
import java.util.Collection;

public class UserEnrollmentTest {

    public static void main(String args[]) throws Exception {

        CryptoPrimitives cryptoPrimitives = new CryptoPrimitives();
        String str= "-----BEGIN PRIVATE KEY-----\n" +
                "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgEou4Uh8frVJSFY4o\n" +
                "U0PmqEuFwmYALdbp3JspFMV1+c+hRANCAAQdWhSviUPldycNyRWAsK9qvj0Ktrvc\n" +
                "v1GqJFs49O9HHTdbQv+2dUmh4XcRagwjw1qrRJWjD5zN0VAnjY5gw04Y\n" +
                "-----END PRIVATE KEY-----\n";


        PrivateKey privateKey1 = cryptoPrimitives.bytesToPrivateKey(str.getBytes());
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

        Enrollment userEnrollment = new UserEnrollment(privateKey1, certification);

        // 5. Create the UserContext for Registrar
        UserContext userContext = new UserContext();
        userContext.setName("User1");
        userContext.setAccount("User1");
        userContext.setAffilation(".");
        userContext.setMspId("WebcashMSP");
        userContext.setEnrollment(userEnrollment);

        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        hfClient.setUserContext(userContext);

        // 7. Create a new Channel instance
        Channel channel = hfClient.newChannel("mychannel");
        // 7.1.1. Create a new Peer instance
        Peer peer = hfClient.newPeer("peer0.coocon.kshrd.com.kh", "grpc://127.0.0.1:7051");
        // 7.1.2 Add a new Peer to Channel
        channel.addPeer(peer);

        // 7.2.1. Create Orderer instances
        Orderer orderer2 = hfClient.newOrderer("orderer2.kshrd.com.kh", "grpc://127.0.0.1:8050");
        Orderer orderer3 = hfClient.newOrderer("orderer3.kshrd.com.kh", "grpc://127.0.0.1:9050");
        Orderer orderer1 = hfClient.newOrderer("orderer1.kshrd.com.kh", "grpc://127.0.0.1:7050");

        // 7.2.2. Add Orderers instances to Channel
        channel.addOrderer(orderer1);
        channel.addOrderer(orderer2);
        channel.addOrderer(orderer3);

        // 8. Initialized the Channel
        channel.initialize();

        // 9. Create a new QueryByChaincodeRequest with newQueryProposalRequest
        QueryByChaincodeRequest qpr = hfClient.newQueryProposalRequest();

        // 10. Build ChaincodID providing the ChaincodeName and Version.
        ChaincodeID ccID = ChaincodeID.newBuilder()
                .setName("kshrdsmartcontract")
                .setVersion("1.0.1")
                .build();
        // 11. Set ChaincodeID to QueryByChaincodeRequest
        qpr.setChaincodeID(ccID);

        // 12. Set the Chaincode FunctionName to be called
        qpr.setFcn("queryHistoryByKey");

        // 13. set Chaincode arguments to QueryByChaincodeRequest
        qpr.setArgs("a");

        // 14. Query By Chaincode with QueryByChaincodeRequest
        Collection<ProposalResponse> responses = channel.queryByChaincode(qpr);

        // 15. Loop through the ProposalResponses
        for (ProposalResponse response : responses) {

            // 15.1. Check the Response is verified and ChaincodeResponse is Success
            if (response.isVerified() && response.getStatus() == ChaincodeResponse.Status.SUCCESS) {

                // 15.1.1. Get the Payload from the Response()
                ByteString payload = response.getProposalResponse().getResponse().getPayload();

                // 15.1.2. Show the Payload
                System.out.println(payload.toStringUtf8());

                // 15.2
            } else {
                // 15.1. Show the Response Status
                System.err.println("response failed. status: " + response.getStatus().getStatus());
            }
        }

        //System.out.println(newUser.toString());
    }
}
