import sun.security.util.DerInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CertificateMain {

    public static void main(String args[]) throws Exception {
        String certification = "-----BEGIN CERTIFICATE-----\n" +
                "MIICaTCCAhCgAwIBAgIUPTEiLWYsapDDNM4i4taQjARQtY0wCgYIKoZIzj0EAwIw\n" +
                "eTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh\n" +
                "biBGcmFuY2lzY28xHDAaBgNVBAoTE2Nvb2Nvbi5rc2hyZC5jb20ua2gxHzAdBgNV\n" +
                "BAMTFmNhLmNvb2Nvbi5rc2hyZC5jb20ua2gwHhcNMTkwMTEwMDUxNDAwWhcNMjAw\n" +
                "MTEwMDUxOTAwWjAkMQ8wDQYDVQQLEwZjbGllbnQxETAPBgNVBAMTCFBFTkhDSEVU\n" +
                "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEHtFnn75OxMa89gBSHM7PRjPydEg7\n" +
                "kMAGdwcyafVge10xwIMSe/j7vubl04Rv5KekL58YD5h+4v7ov8kw1cyTa6OByjCB\n" +
                "xzAOBgNVHQ8BAf8EBAMCB4AwDAYDVR0TAQH/BAIwADAdBgNVHQ4EFgQUcLsAgDcS\n" +
                "nPNxtolCqn7Oj+wazncwKwYDVR0jBCQwIoAgdF47QBjtpcFXj4pLZvpvU/Dlb4Cb\n" +
                "0Hd1gWXDGhPzD5swWwYIKgMEBQYHCAEET3siYXR0cnMiOnsiaGYuQWZmaWxpYXRp\n" +
                "b24iOiIiLCJoZi5FbnJvbGxtZW50SUQiOiJQRU5IQ0hFVCIsImhmLlR5cGUiOiJj\n" +
                "bGllbnQifX0wCgYIKoZIzj0EAwIDRwAwRAIgNQfShvQfgTW8HGyorDTUlH6G3gKI\n" +
                "Hg/8H1Qz/YDWLM8CIAISCbhCz2MK0be7wxn7UNzubnBpzb/Xrqvv8M/+dAp/\n" +
                "-----END CERTIFICATE-----";

        X509Certificate certificate = getCert(certification.getBytes());

        System.out.println(certificate.getIssuerX500Principal());

        System.out.println(certificate.toString());

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

//        X509Certificate certificate = getCert(certification.getBytes());

        byte[] extVal = certificate.getExtensionValue("1.2.3.4.5.6.7.8.1");

        DerInputStream in = new DerInputStream(extVal);
        byte[] certSubjectKeyID = in.getOctetString();

        System.out.println(new String(certSubjectKeyID,"UTF-8"));



//        InputStream inputStream = new ByteArrayInputStream(Charset.forName("UTF-8").encode(crl).array());
//        X509CRL x509CRL = (X509CRL) cf.generateCRL(inputStream);
//
//        System.out.println(x509CRL.isRevoked(certificate));
    }

    private static X509Certificate getCert(byte[] certBytes) throws Exception {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new ByteArrayInputStream(certBytes));
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(bufferedInputStream);

        certificate.getEncoded();
        return certificate;
    }
}
