package app.nzyme.core.crypto.tls;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TLSUtils {

    private static final Pattern CERT_BASE64_BLOCKS = Pattern.compile(
            "-+BEGIN\\s+.*CERTIFICATE[^-]*-+(?:\\s|\\r|\\n)+" +
                    "([a-z0-9+/=\\r\\n]+)" +
                    "-+END\\s+.*CERTIFICATE[^-]*-+",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern KEY_BASE64_BLOCK = Pattern.compile(
            "-+BEGIN\\s+.*PRIVATE\\s+KEY[^-]*-+(?:\\s|\\r|\\n)+" +
                    "([a-z0-9+/=\\r\\n]+)" +
                    "-+END\\s+.*PRIVATE\\s+KEY[^-]*-+",
            Pattern.CASE_INSENSITIVE);

    public static List<X509Certificate> readCertificateChainFromPEM(String pem) throws PEMParserException {
        final Matcher m = CERT_BASE64_BLOCKS.matcher(pem);

        final List<X509Certificate> certs = Lists.newArrayList();
        int pos = 0;

        while (m.find(pos)) {
            byte[] bytes = BaseEncoding.base64().decode(
                    CharMatcher
                            .breakingWhitespace()
                            .removeFrom(m.group(1))
            );

            try {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                certs.add((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(bytes)));
            } catch(Exception e) {
                throw new PEMParserException("Could not construct certificate.", e);
            }

            pos = m.end();
        }

        if (certs.isEmpty()) {
            throw new PEMParserException("Could not find any certificates in certificate file.");
        }

        return certs;
    }

    public static PrivateKey readKeyFromPEM(String pem) throws PEMParserException {
        final Matcher m = KEY_BASE64_BLOCK.matcher(pem);

        if (!m.find()) {
            throw new PEMParserException("No key found in file.");
        }

        byte[] bytes = BaseEncoding.base64().decode(
                CharMatcher
                        .breakingWhitespace()
                        .removeFrom(m.group(1))
        );

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
        } catch(Exception e) {
            throw new PEMParserException("Could not construct private key.", e);
        }
    }

    public static String serializeCertificateChain(List<X509Certificate> certificates) throws CertificateEncodingException {
        List<String> strings = Lists.newArrayList();
        for (Certificate certificate : certificates) {
            strings.add(BaseEncoding.base64().encode(certificate.getEncoded()));
        }

        return Joiner.on(",").join(strings);
    }

    public static List<X509Certificate> deSerializeCertificateChain(String serialized)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        List<X509Certificate> certificates = Lists.newArrayList();

        for (String s : Splitter.on(",").split(serialized)) {
            byte[] certBytes = BaseEncoding.base64().decode(s);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            certificates.add((X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes)));
        }

        return certificates;
    }


    public static String calculateTLSCertificateFingerprint(X509Certificate certificate) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(certificate.getEncoded());
        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }
    
    public static final class PEMParserException extends Exception {

        public PEMParserException(String msg) {
            super(msg);
        }

        public PEMParserException(String msg, Throwable t) {
            super(msg, t);
        }

    }

}
