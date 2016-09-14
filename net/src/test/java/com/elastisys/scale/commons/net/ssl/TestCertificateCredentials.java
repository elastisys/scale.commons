package com.elastisys.scale.commons.net.ssl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Tests that exercise the {@link CertificateCredentials} class.
 */
public class TestCertificateCredentials {

    private static final String PKCS12_KEYSTORE = "src/test/resources/security/client/client_keystore.p12";
    private static final String JKS_KEYSTORE = "src/test/resources/security/client/client_keystore.jks";
    private static final String PKCS12_KEYSTORE_PASSWORD = "clientpass";
    private static final String JKS_KEYSTORE_PASSWORD = "jksclientpass";

    private static final String UNTRUSTED_PKCS12_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.p12";
    private static final String UNTRUSTED_PKCS12_KEYSTORE_PASSWORD = "untrustedpass";
    private static final String UNTRUSTED_JKS_KEYSTORE = "src/test/resources/security/untrusted/client_keystore.jks";
    private static final String UNTRUSTED_JKS_KEYSTORE_PASSWORD = "untrustedjkspass";

    /**
     * Check basic sanity of accessor methods, equals comparison and hashCode.
     */
    @Test
    public void testBasicSanity() {
        // field access check
        CertificateCredentials credentials = new CertificateCredentials(KeyStoreType.PKCS12, PKCS12_KEYSTORE,
                PKCS12_KEYSTORE_PASSWORD, PKCS12_KEYSTORE_PASSWORD);
        assertThat(credentials.getKeystoreType().name(), is("PKCS12"));
        assertThat(credentials.getKeystorePath(), is(PKCS12_KEYSTORE));
        assertThat(credentials.getKeystorePassword(), is(PKCS12_KEYSTORE_PASSWORD));
        assertThat(credentials.getKeyPassword(), is(PKCS12_KEYSTORE_PASSWORD));
        credentials.validate();

        // comparison of equivalent credentials
        CertificateCredentials copy = new CertificateCredentials(KeyStoreType.PKCS12, PKCS12_KEYSTORE,
                PKCS12_KEYSTORE_PASSWORD, PKCS12_KEYSTORE_PASSWORD);
        assertTrue(credentials.equals(copy));
        assertTrue(credentials.hashCode() == copy.hashCode());

        // comparison of non-equivalent credentials
        CertificateCredentials other = new CertificateCredentials(KeyStoreType.PKCS12, UNTRUSTED_PKCS12_KEYSTORE,
                UNTRUSTED_JKS_KEYSTORE_PASSWORD, UNTRUSTED_JKS_KEYSTORE_PASSWORD);
        assertFalse(credentials.equals(other));
        assertFalse(credentials.hashCode() == other.hashCode());
    }

    /**
     * Verify default values.
     */
    @Test
    public void testDefaults() {
        // leave out keystore type (should default to PKCS12)
        CertificateCredentials credentials = new CertificateCredentials(PKCS12_KEYSTORE, PKCS12_KEYSTORE_PASSWORD,
                PKCS12_KEYSTORE_PASSWORD);
        assertThat(credentials.getKeystoreType().name(), is("PKCS12"));
        credentials.validate();

        // leave out key password (should default to keystore password)
        credentials = new CertificateCredentials(PKCS12_KEYSTORE, PKCS12_KEYSTORE_PASSWORD);
        assertThat(credentials.getKeyPassword(), is(credentials.getKeystorePassword()));
        credentials.validate();
    }

    /**
     * Test loading a client certificate key from a PKCS12 store with a
     * {@link CertificateCredentials} object.
     * <p/>
     * <i>Note: this test is not strictly necessary from a unit testing
     * perspective, but is mostly here for illustrate the principles of its
     * use.</i>
     */
    @Test
    public void loadKeyFromPkcs12Keystore() throws Exception {
        CertificateCredentials credentials = new CertificateCredentials(KeyStoreType.PKCS12, PKCS12_KEYSTORE,
                PKCS12_KEYSTORE_PASSWORD, PKCS12_KEYSTORE_PASSWORD);

        // open the keystore with the keystore password to verify the integrity
        // of the store
        KeyStore keyStore = KeyStore.getInstance(credentials.getKeystoreType().name());
        keyStore.load(new FileInputStream(credentials.getKeystorePath()),
                credentials.getKeystorePassword().toCharArray());

        // get certificate from keystore
        List<String> entries = Collections.list(keyStore.aliases());
        assertThat(entries.size(), is(1));
        Certificate certificate = keyStore.getCertificate(entries.get(0));
        X509Certificate x509Certificate = X509Certificate.class.cast(certificate);
        assertThat(x509Certificate.getSubjectDN().getName(), is("CN=Client, O=Elastisys, C=SE"));

        // get private key from keystore
        String keyPassword = credentials.getKeyPassword();
        Key key = keyStore.getKey(entries.get(0), keyPassword.toCharArray());
        assertThat(key.getAlgorithm(), is("RSA"));
        assertThat(key.getFormat(), is("PKCS#8"));
    }

    /**
     * Test loading a client certificate key from a JKS store. Here we need to
     * explicitly specify the {@code keyPassword}, since the PKCS12 keystore was
     * imported to the JKS keystore.
     * <p/>
     * <i>Note: this test is not strictly necessary from a unit testing
     * perspective, but is mostly here for illustrate the principles of its
     * use.</i>
     */
    @Test
    public void loadKeyFromJksKeystore() throws Exception {
        // NOTE: need to use PKCS12 password as "key password" (since the PKCS12
        // keystore was imported to the JKS keystore)
        CertificateCredentials credentials = new CertificateCredentials(KeyStoreType.JKS, JKS_KEYSTORE,
                JKS_KEYSTORE_PASSWORD, PKCS12_KEYSTORE_PASSWORD);

        // open the keystore with the keystore password
        KeyStore keyStore = KeyStore.getInstance(credentials.getKeystoreType().name());
        keyStore.load(new FileInputStream(credentials.getKeystorePath()),
                credentials.getKeystorePassword().toCharArray());

        // get certificate from keystore
        List<String> entries = Collections.list(keyStore.aliases());
        assertThat(entries.size(), is(1));
        Certificate certificate = keyStore.getCertificate(entries.get(0));
        X509Certificate x509Certificate = X509Certificate.class.cast(certificate);
        assertThat(x509Certificate.getSubjectDN().getName(), is("CN=Client, O=Elastisys, C=SE"));

        // get private key from JKS keystore (needs the key password)
        String keyPassword = credentials.getKeyPassword();
        Key key = keyStore.getKey(entries.get(0), keyPassword.toCharArray());
        assertThat(key.getAlgorithm(), is("RSA"));
        assertThat(key.getFormat(), is("PKCS#8"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNullKeystoreType() {
        new CertificateCredentials(null, PKCS12_KEYSTORE, PKCS12_KEYSTORE_PASSWORD, PKCS12_KEYSTORE_PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNullKeystorePath() {
        new CertificateCredentials(KeyStoreType.PKCS12, null, PKCS12_KEYSTORE_PASSWORD, PKCS12_KEYSTORE_PASSWORD);
    }

    /**
     * Should fail since the keystore file doesn't exist.
     */
    @Test(expected = IllegalArgumentException.class)
    public void withNonExistantKeystoreFile() {
        new CertificateCredentials(KeyStoreType.PKCS12, "/some/path/keystore.p12", PKCS12_KEYSTORE_PASSWORD,
                PKCS12_KEYSTORE_PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void withNullKeystorePassword() {
        new CertificateCredentials(KeyStoreType.PKCS12, PKCS12_KEYSTORE, null, PKCS12_KEYSTORE_PASSWORD);
    }

    /**
     * Should be allowed to set a <code>null</code> key password. In this case,
     * the keystore password is used as key password.
     */
    @Test
    public void withNullKeyPassword() {
        CertificateCredentials credentials = new CertificateCredentials(KeyStoreType.PKCS12, PKCS12_KEYSTORE,
                PKCS12_KEYSTORE_PASSWORD, null);
        credentials.validate();
        assertThat(credentials.getKeyPassword(), is(PKCS12_KEYSTORE_PASSWORD));
    }

}
