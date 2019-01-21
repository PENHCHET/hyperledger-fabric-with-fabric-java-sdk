import sun.security.util.DerInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateMain {

    public static void main(String args[]) throws Exception {
        String certification = "-----BEGIN CERTIFICATE-----\n" +
                "MIICkTCCAjegAwIBAgIUdXwaCwLmCYKxOSITH2OGB6hHO/IwCgYIKoZIzj0EAwIw\n" +
                "eTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFjAUBgNVBAcTDVNh\n" +
                "biBGcmFuY2lzY28xHDAaBgNVBAoTE2Nvb2Nvbi5rc2hyZC5jb20ua2gxHzAdBgNV\n" +
                "BAMTFmNhLmNvb2Nvbi5rc2hyZC5jb20ua2gwHhcNMTkwMTEwMDcwNDAwWhcNMjAw\n" +
                "MTEwMDcwOTAwWjAjMQ8wDQYDVQQLEwZjbGllbnQxEDAOBgNVBAMTB1BJUkFORzIw\n" +
                "WTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATcNdUJaBrZeOZE6/lHBwJtJ2ykcIxD\n" +
                "fsnwA7YGYaTAfu9gWQ0cAAp0H6VChWN4BarGX7KOZX5lEJfq0w9rgeOco4HyMIHv\n" +
                "MA4GA1UdDwEB/wQEAwIHgDAMBgNVHRMBAf8EAjAAMB0GA1UdDgQWBBRyHY2GQIxb\n" +
                "fwYRQh9Tv0pYwmMATjArBgNVHSMEJDAigCB0XjtAGO2lwVePiktm+m9T8OVvgJvQ\n" +
                "d3WBZcMaE/MPmzCBggYIKgMEBQYHCAEEdnsiYXR0cnMiOnsiTVNQX0lEIjoiQ29v\n" +
                "Y29uTVNQIiwiTkFNRSI6IkNvb2Nvbk1TUCIsImhmLkFmZmlsaWF0aW9uIjoiIiwi\n" +
                "aGYuRW5yb2xsbWVudElEIjoiUElSQU5HMiIsImhmLlR5cGUiOiJjbGllbnQifX0w\n" +
                "CgYIKoZIzj0EAwIDSAAwRQIhALWCmn6giLNrG1LMyGPhdG+g8hGy5Ffg03EY3zNe\n" +
                "gTLRAiAdHolS1iiQStRqh4oD594AR0wnB9k/yGJM1zdWDruwJg==\n" +
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
