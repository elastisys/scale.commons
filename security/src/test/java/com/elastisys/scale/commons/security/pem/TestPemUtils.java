package com.elastisys.scale.commons.security.pem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

public class TestPemUtils {
	private static final File KEY_DIR = new File("src/test/resources/keys");

	/** Private RSA key generated with ssh-keygen. */
	private static final File sshkeygenPrivate = new File(KEY_DIR,
			"sshkeygen_key");
	/** Public RSA key generated with ssh-keygen. */
	private static final File sshkeygenPublic = new File(KEY_DIR,
			"sshkeygen_key.pub.pem");

	/** Private RSA key generated with openssl. */
	private static final File opensslPrivate = new File(KEY_DIR,
			"openssl_private.pem");
	/** Public RSA key generated with openssl. */
	private static final File opensslPublic = new File(KEY_DIR,
			"openssl_public.pem");

	/**
	 * Make sure that key pairs generated using {@code openssl} (as described in
	 * {@link PemUtils}), are properly parsed.
	 */
	@Test
	public void parseOpensslGeneratedKeys() throws Exception {
		RSAPublicKey rsaPublicKey = PemUtils.parseRsaPublicKey(opensslPublic);
		RSAPrivateKey rsaPrivateKey = PemUtils
				.parseRsaPrivateKey(opensslPrivate);
		assertNotNull(rsaPublicKey);
		assertNotNull(rsaPrivateKey);

		// make sure that the public key can be used to create a signature that
		// can be validated by the private key
		String message = "hello world";
		byte[] signatureBytes = sign(message, rsaPrivateKey);
		assertTrue(
				"failed to validate signature by public key with private key",
				verifySignature(message, signatureBytes, rsaPublicKey));
	}

	/**
	 * Make sure that key pairs generated using {@code ssh-keygen} (as described
	 * in {@link PemUtils}), are properly parsed.
	 */
	@Test
	public void parseSshkeygenGeneratedKeys() throws Exception {
		RSAPublicKey rsaPublicKey = PemUtils.parseRsaPublicKey(sshkeygenPublic);
		RSAPrivateKey rsaPrivateKey = PemUtils
				.parseRsaPrivateKey(sshkeygenPrivate);
		assertNotNull(rsaPublicKey);
		assertNotNull(rsaPrivateKey);

		// make sure that the public key can be used to create a signature that
		// can be validated by the private key
		String message = "hello world";
		byte[] signatureBytes = sign(message, rsaPrivateKey);
		assertTrue(
				"failed to validate signature by public key with private key",
				verifySignature(message, signatureBytes, rsaPublicKey));
	}

	@Test
	public void testToPem() throws Exception {
		// parse keys from file
		RSAPublicKey rsaPublicKey = PemUtils.parseRsaPublicKey(sshkeygenPublic);
		RSAPrivateKey rsaPrivateKey = PemUtils
				.parseRsaPrivateKey(sshkeygenPrivate);

		// verify that toPem produces the exact same strings as were in the
		// original files.
		String publicKeyFileContents = Files.toString(sshkeygenPublic,
				Charsets.UTF_8);
		assertThat(PemUtils.toPem(rsaPublicKey), is(publicKeyFileContents));

		String privateKeyFileContents = Files.toString(sshkeygenPrivate,
				Charsets.UTF_8);
		assertThat(PemUtils.toPem(rsaPrivateKey), is(privateKeyFileContents));

	}

	/**
	 * Signs a given message with a given RSA private key and returns the
	 * signature bytes.
	 *
	 * @param message
	 *            Message to be signed.
	 * @param rsaPrivateKey
	 *            The private RSA key used to sign.
	 * @return The signature in bytes.
	 * @throws Exception
	 */
	private static byte[] sign(String message, RSAPrivateKey rsaPrivateKey)
			throws Exception {
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initSign(rsaPrivateKey);
		signature.update(message.getBytes());
		byte[] signatureBytes = signature.sign();
		return signatureBytes;
	}

	/**
	 * Verifies a given signature with a public RSA key.
	 *
	 * @param signedMessage
	 *            The message that was signed.
	 * @param signatureBytes
	 *            The signature in bytes.
	 * @param rsaPublicKey
	 *            The public RSA key used to verify the signature.
	 * @return <code>true</code> if the private key corresponding to the public
	 *         key was used to produce the signature, <code>false</code>
	 *         otherwise.
	 * @throws Exception
	 */
	private boolean verifySignature(String signedMessage, byte[] signatureBytes,
			RSAPublicKey rsaPublicKey) throws Exception {
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initVerify(rsaPublicKey);
		signature.update(signedMessage.getBytes());
		return signature.verify(signatureBytes);
	}
}