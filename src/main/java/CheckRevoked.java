import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import java.io.*;
import java.nio.charset.Charset;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;

public class CheckRevoked {

    public static void main(String args[]) throws Exception{

        System.out.println(CheckRevoked.getRevokes(null));
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream inputStream = new ByteArrayInputStream(Charset.forName("UTF-8").encode(CheckRevoked.getRevokes(null)).array());
        X509CRL x509CRL = (X509CRL) cf.generateCRL(inputStream);

        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "MIICbTCCAhSgAwIBAgIUC1m62uVYuqcQT/TZs75qYBmqG78wCgYIKoZIzj0EAwIw\n" +
                "eTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh\n" +
                "biBGcmFuY2lzY28xHDAaBgNVBAoTE2Nvb2Nvbi5rc2hyZC5jb20ua2gxHzAdBgNV\n" +
                "BAMTFmNhLmNvb2Nvbi5rc2hyZC5jb20ua2gwHhcNMTkwMTEwMDg0NDAwWhcNMjAw\n" +
                "MTEwMDg0OTAwWjAmMQ8wDQYDVQQLEwZjbGllbnQxEzARBgNVBAMTClBFTkhDSEVU\n" +
                "MTkwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQqnXNAxNQ952MzhsPRUVWXAyYu\n" +
                "bqpUXz+txWObWPrFJzte/Y2Iik868yFRy/GRX/nNQiUcwOUTswtxf0bzEINzo4HM\n" +
                "MIHJMA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBQxSBp2\n" +
                "oCmPDVSX7GDlrZ6LyOBiATArBgNVHSMEJDAigCB0XjtAGO2lwVePiktm+m9T8OVv\n" +
                "gJvQd3WBZcMaE/MPmzBdBggqAwQFBgcIAQRReyJhdHRycyI6eyJoZi5BZmZpbGlh\n" +
                "dGlvbiI6IiIsImhmLkVucm9sbG1lbnRJRCI6IlBFTkhDSEVUMTkiLCJoZi5UeXBl\n" +
                "IjoiY2xpZW50In19MAoGCCqGSM49BAMCA0cAMEQCIGzqBVoAkL65tGcPkP4hg0uG\n" +
                "+NqNJlwOvU8hOsD8sQ3JAiBPbXIegEyuNdvLUs8SMFHDmhcvjm6JrFZw/2oTcDb6\n" +
                "rQ==\n" +
                "-----END CERTIFICATE-----";

        BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(cert.getBytes()));
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(bufferedInputStream);

        System.out.println(x509CRL.isRevoked(certificate));
    }

    static String  getRevokes(Date r) throws Exception {

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

        return parseX509CRL(crl);
    }

    static String parseX509CRL(String crl) throws Exception {

        Base64.Decoder b64dec = Base64.getDecoder();
        final byte[] decode = b64dec.decode(crl.getBytes("UTF-8"));

        PEMParser pem = new PEMParser(new StringReader(new String(decode)));

        X509CRLHolder holder = (X509CRLHolder) pem.readObject();

        StringWriter pemStrWriter = new StringWriter();
        PEMWriter pemWriter = new PEMWriter(pemStrWriter);
        pemWriter.writeObject(holder);
        pemWriter.close();

        System.out.println(pemStrWriter.toString());

        return pemStrWriter.toString();
    }
}
