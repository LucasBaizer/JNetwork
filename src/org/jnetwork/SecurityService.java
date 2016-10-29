package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

/**
 * A bunch of cryptography utilities.
 * 
 * @author Lucas Baizer
 */
class SecurityService {
	private static final Random random = new SecureRandom();

	private PublicKey publicKey;
	private PrivateKey privateKey;
	private Cipher cipher;
	private IvParameterSpec iv;
	private boolean useParams = false;

	/**
	 * @return A random RSA public/private key pair.
	 */
	static KeyPair generateRSAKeyPair() throws CryptographyException {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			keyGen.initialize(2048);

			return keyGen.generateKeyPair();
		} catch (Exception e) {
			throw new CryptographyException(e);
		}
	}

	/**
	 * @return A new RSA SecurityService with a new random RSA key pair.
	 */
	static SecurityService generateRSASecurityService() throws CryptographyException {
		KeyPair pair = generateRSAKeyPair();
		return new SecurityService("RSA", pair.getPublic(), pair.getPrivate());
	}

	public SecurityService(String algorithm) throws CryptographyException {
		try {
			cipher = Cipher.getInstance(algorithm);

			byte[] bytes = new byte[16];
			random.nextBytes(bytes);

			iv = new IvParameterSpec(bytes);
		} catch (Exception e) {
			throw new CryptographyException(e);
		}
	}

	public SecurityService(String algorithm, PublicKey publicKey, PrivateKey privateKey) throws CryptographyException {
		this(algorithm);

		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	/**
	 * @param params
	 *            Whether or not to use IV parameters. This is required with an
	 *            AES SecurityService.
	 */
	public void setUseParameters(boolean params) {
		this.useParams = params;
	}

	/**
	 * @return The IV parameters.
	 */
	public IvParameterSpec getParameters() {
		return iv;
	}

	/**
	 * @param params
	 *            The IV Parameters to use, typically when encrypting/decrypting
	 *            AES>
	 */
	public void setParameters(IvParameterSpec params) {
		this.iv = params;
	}

	/**
	 * @return A KeyPair containing the SecurityService's public and private
	 *         key.
	 */
	public KeyPair getKeyPair() {
		return new KeyPair(publicKey, privateKey);
	}

	/**
	 * @param data
	 *            The data to encrypt.
	 * @return An encrypted copy of the parameter.
	 */
	public byte[] encrypt(byte[] data) throws CryptographyException {
		return encrypt(data, publicKey);
	}

	/**
	 * @param data
	 *            The data to encrypt.
	 * @param key
	 *            The key to use to encrypt the data.
	 * @return An encrypted copy of the parameter.
	 */
	public byte[] encrypt(byte[] data, Key key) throws CryptographyException {
		try {
			if (useParams)
				cipher.init(Cipher.ENCRYPT_MODE, key, iv);
			else
				cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new CryptographyException("Error encryping bytes", e);
		}
	}

	/**
	 * Encrypts an object.
	 */
	public byte[] encrypt(Serializable data) throws CryptographyException {
		try {
			return encrypt(UDPUtils.serializeObject(data));
		} catch (IOException e) {
			throw new CryptographyException(e);
		}
	}

	/**
	 * @param data
	 *            The data to decrypt.
	 * @return A decrypted copy of the parameter.
	 */
	public byte[] decrypt(byte[] data) throws CryptographyException {
		return decrypt(data, privateKey);
	}

	/**
	 * @param data
	 *            The data to decrypt.
	 * @param key
	 *            The key to decrypt the dat with.
	 * @return A decrypted copy of the parameter.
	 */
	public byte[] decrypt(byte[] data, Key key) throws CryptographyException {
		try {
			if (useParams)
				cipher.init(Cipher.DECRYPT_MODE, key, iv);
			else
				cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new CryptographyException("Error decrypting bytes", e);
		}
	}

	/**
	 * @return the public key.
	 */
	public Key getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey
	 *            The new public key.
	 */
	public SecurityService setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
		return this;
	}

	/**
	 * @return the private key.
	 */
	public Key getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey
	 *            the new private key.
	 */
	public SecurityService setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
		return this;
	}
}
