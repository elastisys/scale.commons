package com.elastisys.scale.commons.security.pem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
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
 * parse and output PEM-encoded public and private RSA keys.
 * <p/>
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
 */
public class PemUtils {
	static {
		Security.addProvider(new BouncyCastleProvider());
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
			throws IOException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException {
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
			throws IOException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException {

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
	public static RSAPrivateKey parseRsaPrivateKey(File privateKeyPemFile)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException {
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
	public static RSAPrivateKey parseRsaPrivateKey(Reader privateKeyPemReader)
			throws FileNotFoundException, IOException, NoSuchAlgorithmException,
			NoSuchProviderException, InvalidKeySpecException {
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
}
