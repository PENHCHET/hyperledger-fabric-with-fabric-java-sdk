import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InvokeChaincode {
    public static void main(String args[]) throws Exception {
        // --- For Invoke Chaincode

        // Create a new CryptoSuite instance
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFCAClient caClient = HFCAClient.createNewInstance("http://127.0.0.1:7054", null);
        caClient.setCryptoSuite(cryptoSuite);

        // Create a new Enrollment instance
        Enrollment adminEnrollment = caClient.enroll("admin", "admin");

        // Create the UserContext
        UserContext admin = new UserContext();
        admin.setName("admin");
        admin.setAccount("admin");
        admin.setMspId("CooconMSP");
        admin.setEnrollment(adminEnrollment);

        // Create a new HFClient instance
        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(cryptoSuite);

        // HFClient needs to set the UserContext
        hfClient.setUserContext(admin);

        // Create a new Channel instance
        Channel channel = hfClient.newChannel("mychannel");
        Peer peer = hfClient.newPeer("peer1.coocon.kshrd.com.kh", "grpc://127.0.0.1:8051");
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

        // Create a new TransactionProposalRequest
        TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();

        // Build ChaincodID providing the ChaincodeName and Version.
        ChaincodeID ccID = ChaincodeID.newBuilder()
                .setName("kshrdsmartcontract")
                .setVersion("1.0")
                .build();

        // Set ChaincodeID to TransactionProposalRequest
        transactionProposalRequest.setChaincodeID(ccID);

        // Set the Chaincode FunctionName to be called
        transactionProposalRequest.setFcn("invoke");

        // set Chaincode arguments to TransactionProposalRequest
        transactionProposalRequest.setArgs("b","a","50");

        // Send TransactionProposal
        Collection<ProposalResponse> responses = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());

        // Loop Through Invalid ProposalResponse
        Collection<ProposalResponse> invalid = responses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());

        if (!invalid.isEmpty()) {
            invalid.forEach(response -> {
                System.out.println(response.getMessage());
            });
            throw new RuntimeException("invalid response(s) found");
        }

        // Transaction Event to wait the Transaction Until Completed
        BlockEvent.TransactionEvent event = channel.sendTransaction(responses).get(100, TimeUnit.SECONDS);

        // Check Transaction Event is Valid or not
        if (event.isValid()) {

            // Show the Transaction ID is completed
            System.out.println("Transacion tx: " + event.getTransactionID() + " is completed.");


        } else {
            // Show the Transaction ID is invalid
            System.out.println("Transaction tx: " + event.getTransactionID() + " is invalid.");
        }
    }

}
