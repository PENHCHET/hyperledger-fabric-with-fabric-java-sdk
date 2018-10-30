import com.google.protobuf.ByteString;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.security.PrivateKey;
import java.util.Collection;

public class QueryChaincode {

    public static void main(String args[]) throws Exception {
        // --- For Query Chaincode

        // 1. Create a new CryptoSuite instance
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();

        // 2. Create a new HFCAClient instance
        HFCAClient caClient = HFCAClient.createNewInstance("http://127.0.0.1:7054", null);

        // 2.1. Set CryptoSuite
        caClient.setCryptoSuite(cryptoSuite); // Signature Algorithm, Verification,

        // 3. Create a new Enrollment instance
        Enrollment adminEnrollment = caClient.enroll("admin", "admin");

        // 4. Create the UserContext
        UserContext admin = new UserContext();
        admin.setName("admin");
        admin.setAccount("admin");
        admin.setMspId("CooconMSP");
        admin.setEnrollment(adminEnrollment);

        // 5. Create a new HFClient instance
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(cryptoSuite);

        // 6. HFClient needs to set the UserContext
        hfClient.setUserContext(admin);

        // 7. Create a new Channel instance
        Channel channel = hfClient.newChannel("mychannel");
        // 7.1.1. Create a new Peer instance
        Peer peer = hfClient.newPeer("peer1.coocon.kshrd.com.kh", "grpc://127.0.0.1:8051");
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
    }
}
