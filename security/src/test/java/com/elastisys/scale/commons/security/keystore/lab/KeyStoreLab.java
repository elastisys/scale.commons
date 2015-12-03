package com.elastisys.scale.commons.security.keystore.lab;

import java.io.FileInputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

/**
 * Simple lab program that retrieves keys and certificates from different
 * {@link KeyStore}s.
 */
public class KeyStoreLab {

	private static final String KEYSTORES_DIR = "src/test/resources/keystores";

	public static void main(String[] args) throws Exception {

		// NOTE: apparently, the keystore password is not strictly required and
		// may be left out, as it is only used to check the integrity of the key
		// store, it is not required to read the contents of the keystore.

		String keystorePath = KEYSTORES_DIR + "/keystore.p12";
		System.out.println("Examining PKCS12 store " + keystorePath + " ...");
		// either correct or null works as keystore password
		String incorrectKeystorePass = "wrong";
		String correctKeystorePass = "pkcs12pass";
		String nullKeystorePass = null;
		printContents(keystorePath, "PKCS12", nullKeystorePass, "pkcs12pass");

		keystorePath = KEYSTORES_DIR + "/keystore.p12.jks";
		System.out.println("Examining JKS store " + keystorePath + " ...");
		printContents(keystorePath, "JKS", "jkspass", "pkcs12pass");

		keystorePath = KEYSTORES_DIR
				+ "/keystore_with_storepass_and_keypass.jks";
		System.out.println("Examining JKS store " + keystorePath + " ...");
		printContents(keystorePath, "JKS", "jkspass", "keypass");

	}

	private static void printContents(String keystorePath, String keystoreType,
			String keystorePassword, String keyPassword) throws Exception {

		System.out.println("loading key store ...");
		KeyStore keyStore = KeyStore.getInstance(keystoreType);
		char[] storePassword = keystorePassword == null ? null
				: keystorePassword.toCharArray();
		if (storePassword == null) {
			System.err.println("no keystore password provided");
		}
		keyStore.load(new FileInputStream(keystorePath), storePassword);

		Enumeration<String> aliases = keyStore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			System.out.println(
					"loading key for keystore alias '" + alias + "' ...");
			char[] keyPass = keyPassword == null ? null
					: keyPassword.toCharArray();
			if (keyStore.isCertificateEntry(alias)) {
				Certificate certificate = keyStore.getCertificate(alias);
				System.out.println("Certificate: " + certificate);
			}
			if (keyStore.isKeyEntry(alias)) {
				Key key = keyStore.getKey(alias, keyPass);
				System.out.println("Key: " + key);
			}
		}
		System.out.println();
	}

}
