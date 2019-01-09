import com.google.protobuf.ByteString;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoPrimitives;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.security.PrivateKey;
import java.util.Collection;

public class UserEnrollmentRevoke {

    public static void main(String args[]) throws Exception {

        CryptoPrimitives cryptoPrimitives = new CryptoPrimitives();
        String str= "-----BEGIN EC PRIVATE KEY-----\n" +
                "MHcCAQEEIBo1rVjEmmZD7DwutCKc2A7413Eu+thbL6CihlkgCyVXoAoGCCqGSM49\n" +
                "AwEHoUQDQgAEycWMCRVwAGYl+FGR2L8hwpKkq329Ptn+JoW1OLeAhOJGaSJj0qN/\n" +
                "0D6rFfCxaHIiw+UqtzutdbW3a02YzrwSig==\n" +
                "-----END EC PRIVATE KEY-----";


        PrivateKey privateKey1 = cryptoPrimitives.bytesToPrivateKey(str.getBytes());
        String certification = "-----BEGIN CERTIFICATE-----\n" +
                "MIICazCCAhKgAwIBAgIUD5jLCjsproJyR2/BT/jZgLjFV6MwCgYIKoZIzj0EAwIw\n" +
                "eTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh\n" +
                "biBGcmFuY2lzY28xHDAaBgNVBAoTE2Nvb2Nvbi5rc2hyZC5jb20ua2gxHzAdBgNV\n" +
                "BAMTFmNhLmNvb2Nvbi5rc2hyZC5jb20ua2gwHhcNMTkwMTA4MTA0MjAwWhcNMjAw\n" +
                "MTA4MTA0NzAwWjAlMQ8wDQYDVQQLEwZjbGllbnQxEjAQBgNVBAMTCVBFTkhDSEVU\n" +
                "NzBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABMnFjAkVcABmJfhRkdi/IcKSpKt9\n" +
                "vT7Z/iaFtTi3gITiRmkiY9Kjf9A+qxXwsWhyIsPlKrc7rXW1t2tNmM68Eoqjgcsw\n" +
                "gcgwDgYDVR0PAQH/BAQDAgeAMAwGA1UdEwEB/wQCMAAwHQYDVR0OBBYEFBuX+0Uf\n" +
                "x0Q3b0aLDa1m2aRQ2TjNMCsGA1UdIwQkMCKAIHReO0AY7aXBV4+KS2b6b1Pw5W+A\n" +
                "m9B3dYFlwxoT8w+bMFwGCCoDBAUGBwgBBFB7ImF0dHJzIjp7ImhmLkFmZmlsaWF0\n" +
                "aW9uIjoiIiwiaGYuRW5yb2xsbWVudElEIjoiUEVOSENIRVQ3IiwiaGYuVHlwZSI6\n" +
                "ImNsaWVudCJ9fTAKBggqhkjOPQQDAgNHADBEAiAwUP+jcVsg3zBp0GabGy/mNoTa\n" +
                "wNqvYK4spe/fEOY/0QIgH3+rV1y1UNAJvmKNpa577g6o6Gi5SEGqDdYLDIb752g=\n" +
                "-----END CERTIFICATE-----";

        Enrollment userEnrollment = new UserEnrollment(privateKey1, certification);

        // Create the UserContext for Registrar
        UserContext userContext = new UserContext();
        userContext.setName("PENHCHET7");
        userContext.setAccount("PENHCHET7");
        userContext.setAffilation(".");
        userContext.setMspId("CooconMSP");
        userContext.setEnrollment(userEnrollment);

        HFClient hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        hfClient.setUserContext(userContext);

        // Create a new Channel instance
        Channel channel = hfClient.newChannel("mychannel");
        Peer peer = hfClient.newPeer("peer0.coocon.kshrd.com.kh", "grpc://127.0.0.1:7051");
        channel.addPeer(peer);
        Orderer orderer2 = hfClient.newOrderer("orderer2.kshrd.com.kh", "grpc://127.0.0.1:8050");
        Orderer orderer3 = hfClient.newOrderer("orderer3.kshrd.com.kh", "grpc://127.0.0.1:9050");
        Orderer orderer1 = hfClient.newOrderer("orderer1.kshrd.com.kh", "grpc://127.0.0.1:7050");
        channel.addOrderer(orderer1);
        channel.addOrderer(orderer2);
        channel.addOrderer(orderer3);
        channel.initialize();

        // Create a new QueryByChaincodeRequest with newQueryProposalRequest
        QueryByChaincodeRequest qpr = hfClient.newQueryProposalRequest();

        // Build ChaincodID providing the ChaincodeName and Version.
        ChaincodeID ccID = ChaincodeID.newBuilder()
                .setName("kshrdsmartcontract")
                .setVersion("1.0.1")
                .build();
        qpr.setChaincodeID(ccID);
        qpr.setFcn("queryHistoryByKey");
        qpr.setArgs("a");

        Collection<ProposalResponse> responses = channel.queryByChaincode(qpr);
        for (ProposalResponse response : responses) {
            if (response.isVerified() && response.getStatus() == ChaincodeResponse.Status.SUCCESS) {
                ByteString payload = response.getProposalResponse().getResponse().getPayload();
                System.out.println(payload.toStringUtf8());
            } else {
                System.err.println("response failed. status: " + response.getStatus().getStatus());
            }
        }
    }
}
