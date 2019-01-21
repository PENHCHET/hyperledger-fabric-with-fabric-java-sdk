import com.google.protobuf.ByteString;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.Collection;

public class QueryChaincode {

    public static void main(String args[]) throws Exception {
        // --- For Query Chaincode

        // Create a new CryptoSuite instance
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();

        // Create a new HFCAClient instance
        HFCAClient caClient = HFCAClient.createNewInstance("http://127.0.0.1:7054", null);

        // Set CryptoSuite
        caClient.setCryptoSuite(cryptoSuite); // Signature Algorithm, Verification,

        // Create a new Enrollment instance
//        Enrollment adminEnrollment = caClient.enroll("admin", "admin");

        // Create the UserContext
//        UserContext admin = new UserContext();
//        admin.setName("admin");
//        admin.setAccount("admin");
//        admin.setMspId("CooconMSP");
//        admin.setEnrollment(adminEnrollment);

        String newUsername = "PENHCHET4";
        String newPassword = "123456";

        Enrollment enrollment = caClient.enroll(newUsername, newPassword);

        System.out.println("New User ==> " + newUsername + "  has been enrolled");

        UserContext newUser = new UserContext();
        newUser.setName(newUsername);
        newUser.setAccount(newUsername);
        newUser.setAffilation(".");
        newUser.setMspId("CooconMSP");
        newUser.setEnrollment(enrollment);

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
    }
}
