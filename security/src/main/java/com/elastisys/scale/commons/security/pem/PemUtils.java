package com.elastisys.scale.commons.security.pem;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemReader;

/**
 * Utility class that uses the {@link BouncyCastleProvider} security provider to
 * parse and output PEM-encoded certificates, public and private RSA keys.
 * <p/>
 * <h1>Generating PEM-encoded RSA public and private keys</h1>
 *
 * To generate PEM-encoded RSA keys compatible with these methods one can follow
 * these instructions:
 * <h2>Using ssh-keygen</h2>
 *
 * Generate an RSA key pair for use with SSH (enter an empty passphrase).
 *
 * <pre>
 * ssh-keygen -t rsa -b 4096 -C "me@example.com" -f mykey
 * </pre>
 *
 * This generates a private PEM-encoded key called <code>mykey</code> and a
 * public key named <code>mykey.pub</code>. The public key file is not in PEM
 * format though, so we need to convert it. This can be done by using
 * <code>ssh-keygen</code> to export the public key to PKCS8 PEM format:
 *
 * <pre>
 * ssh-keygen -e -f mykey.pub -m PKCS8 > mykey.pub.pem
 * </pre>
 *
 * <h2>Using openssl</h2>
 *
 * Generating similar PEM-encoded public and private RSA keys using the
 * <code>openssl</code> tool can also be accomplished. The following steps shows
 * how this can be done:
 *
 * First, generate an RSA private key (or if you already have one proceed to the
 * next step):
 *
 * <pre>
 * openssl genrsa -out my_private.pem 4096
 * </pre>
 *
 * This actually creates a public-private key pair, but to make use of the
 * public key we need to extract it to a separate file:
 *
 * <pre>
 * openssl rsa -in my_private.pem -pubout > my_public.pem
 * </pre>
 *
 * <h1>Generating a PEM-encoded X.509 certificate</h1>
 *
 * To generate a PEM-encoded X.509 certificate and private key, one can follow
 * these steps:
 *
 * <pre>
 * openssl genrsa -out key.pem 2048
 * openssl req -new -x509 -key key.pem -out cert.pem -days 365 -subj '/C=SE/O=Elastisys/CN=Client'
 * </pre>
 *
 */
public class PemUtils {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Parses a PEM-encoded X509 certificate. Refer to the class
     * {@link PemUtils} javadoc for details on how to produce a certificate and
     * key encoded in a manner compatible with this class.
     *
     * @param x509CertPemFile
     * @return
     * @throws IOException
     * @throws CertificateException
     */
    public static X509Certificate parseX509Cert(File x509CertPemFile) throws IOException, CertificateException {
        return parseX509Cert(new FileReader(x509CertPemFile));
    }

    /**
     * Parses a PEM-encoded X509 certificate. Refer to the class
     * {@link PemUtils} javadoc for details on how to produce a certificate and
     * key encoded in a manner compatible with this class.
     *
     * @param x509CertPemReader
     * @return
     * @throws IOException
     * @throws CertificateException
     */
    public static X509Certificate parseX509Cert(Reader x509CertPemReader) throws IOException, CertificateException {
        try (PemReader reader = new PemReader(x509CertPemReader)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            byte[] certBytes = reader.readPemObject().getContent();
            X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
            return cert;
        }
    }

    /**
     * Parses a PEM-encoded public RSA key from a file. Refer to the class
     * {@link PemUtils} javadoc for details on how to produce keys encoded in a
     * manner compatible with this class.
     *
     * @param publicKeyPemFile
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public static RSAPublicKey parseRsaPublicKey(File publicKeyPemFile)
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        return parseRsaPublicKey(new FileReader(publicKeyPemFile));
    }

    /**
     * Parses a PEM-encoded public RSA key from a {@link Reader}. Refer to the
     * class {@link PemUtils} javadoc for details on how to produce keys encoded
     * in a manner compatible with this class.
     *
     * @param publicKeyPemReader
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public static RSAPublicKey parseRsaPublicKey(Reader publicKeyPemReader)
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

        try (PemReader reader = new PemReader(publicKeyPemReader)) {
            KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
            byte[] content = reader.readPemObject().getContent();
            X509EncodedKeySpec pubilcKeySpec = new X509EncodedKeySpec(content);
            return (RSAPublicKey) factory.generatePublic(pubilcKeySpec);
        }
    }

    /**
     * Parses a PEM-encoded private RSA key from a file. Refer to the class
     * {@link PemUtils} javadoc for details on how to produce keys encoded in a
     * manner compatible with this class.
     *
     * @param privateKeyPemFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public static RSAPrivateKey parseRsaPrivateKey(File privateKeyPemFile) throws FileNotFoundException, IOException,
            NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        return parseRsaPrivateKey(new FileReader(privateKeyPemFile));
    }

    /**
     * Parses a PEM-encoded private RSA key from a {@link Reader}. Refer to the
     * class {@link PemUtils} javadoc for details on how to produce keys encoded
     * in a manner compatible with this class.
     *
     * @param privateKeyPemReader
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     */
    public static RSAPrivateKey parseRsaPrivateKey(Reader privateKeyPemReader) throws FileNotFoundException,
            IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        try (PemReader reader = new PemReader(privateKeyPemReader)) {
            KeyFactory factory = KeyFactory.getInstance("RSA", "BC");
            byte[] content = reader.readPemObject().getContent();
            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(content);
            return (RSAPrivateKey) factory.generatePrivate(privKeySpec);
        }
    }

    /**
     * Produces a PEM-formatted representation for an RSA {@link Key} which, for
     * instance, could be written to a file.
     *
     * @param key
     *            The RSA key to serialize.
     * @return A PEM-encoded string representation of the key.
     * @throws IOException
     */
    public static String toPem(Key key) throws IOException {
        StringWriter writer = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(writer);
        pemWriter.writeObject(key);
        pemWriter.close();
        return writer.toString();
    }

    /**
     * Produces an in-memory {@link KeyStore} from a given certificate and key.
     * A new {@link KeyStore} is created and populated with the given
     * certificate and key.
     * <p/>
     * This method is, for instance, useful for servers or clients that wish to
     * authenticate with a given PEM-encoded certificate and key.
     *
     * @param cert
     *            An X.509 certificate.
     * @param key
     *            The private key that belongs to the certificate owner.
     * @param keyPassword
     *            The password that will be used to protect the key in the
     *            returned {@link KeyStore}.
     * @return A {@link KeyStore}.
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public static KeyStore keyStoreFromCertAndKey(Certificate cert, PrivateKey key, String keyPassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        // creates an empty keystore
        keyStore.load(null, null);
        keyStore.setKeyEntry("client", key, keyPassword.toCharArray(), new Certificate[] { cert });
        return keyStore;
    }

    /**
     * Produces an in-memory {@link KeyStore} from a given certificate. A new
     * {@link KeyStore} is created and populated with the given certificate and
     * password.
     * <p/>
     * This method is, for instance, useful for clients that need a trust store
     * in order to authenticate with a given PEM-encoded CA/server certificate.
     *
     * @param cert
     *            An X.509 CA/server certificate to include in the trust store.
     * @return The trust store in the form of a {@link KeyStore}.
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public static KeyStore keyStoreFromCert(Certificate cert)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        // creates an empty keystore
        keyStore.load(null, null);
        keyStore.setCertificateEntry("client", cert);
        return keyStore;
    }
}
