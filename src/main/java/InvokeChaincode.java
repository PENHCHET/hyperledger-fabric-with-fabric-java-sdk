import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InvokeChaincode {
    public static void main(String args[]) throws Exception {
        // --- For Invoke Chaincode

        // 1. Create a new CryptoSuite instance
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // 2. Create a new HFCAClient instance
        HFCAClient caClient = HFCAClient.createNewInstance("http://127.0.0.1:7054", null);
        // 2.1. Set CryptoSuite
        caClient.setCryptoSuite(cryptoSuite);

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

        // 9. Create a new TransactionProposalRequest
        TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();

        // 10. Build ChaincodID providing the ChaincodeName and Version.
        ChaincodeID ccID = ChaincodeID.newBuilder()
                .setName("kshrdsmartcontract")
                .setVersion("1.0")
                .build();

        // 11. Set ChaincodeID to TransactionProposalRequest
        transactionProposalRequest.setChaincodeID(ccID);

        // 12. Set the Chaincode FunctionName to be called
        transactionProposalRequest.setFcn("invoke");

        // 13. set Chaincode arguments to TransactionProposalRequest
        transactionProposalRequest.setArgs("b","a","50");

        // 14. Send TransactionProposal
        Collection<ProposalResponse> responses = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());

        // 15. Loop Through Invalid ProposalResponse
        Collection<ProposalResponse> invalid = responses.stream().filter(r -> r.isInvalid()).collect(Collectors.toList());

        if (!invalid.isEmpty()) {
            invalid.forEach(response -> {
                System.out.println(response.getMessage());
            });
            throw new RuntimeException("invalid response(s) found");
        }

        // 16. Transaction Event to wait the Transaction Until Completed
        BlockEvent.TransactionEvent event = channel.sendTransaction(responses).get(100, TimeUnit.SECONDS);

        // 17. Check Transaction Event is Valid or not
        if (event.isValid()) {

            // 17.1 Show the Transaction ID is completed
            System.out.println("Transacion tx: " + event.getTransactionID() + " is completed.");


        } else {
            // 17.1 Show the Transaction ID is invalid
            System.out.println("Transaction tx: " + event.getTransactionID() + " is invalid.");
        }
    }

}
