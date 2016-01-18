package org.jnetwork.crypto;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * A utility class for encrypting and decrypting serializable objects.
 * 
 * @author Lucas Baizer
 * 
 * @see javax.network.crypto.ObjectEncryptor.EncryptionMethod
 * @see java.io.Serializable
 */
public class ObjectEncryptor implements Serializable {
	private static final long serialVersionUID = 3672795710800568432L;
	private Cipher encryptor;
	private Cipher decryptor;
	private SecretKey key;
	private String password;

	/**
	 * Constructs a new instance of <code>ObjectEncryptor</code> and an
	 * encrypting and decrypting <code>Cipher</code>.
	 * 
	 * @param key
	 *            - The <code>SecretKey</code> to initialize the
	 *            <code>Cipher</code> with.
	 * 
	 * @param encryptionMethod
	 *            - The method to encrypt with, either DES or AES.
	 * 
	 * @throws NoSuchAlgorithmException
	 *             If the encryption method is invalid.
	 * @throws NoSuchPaddingException
	 *             If the encryption method contains an unavailable padding.
	 * @throws InvalidKeyException
	 *             If the <code>SecretKey</code> is invalid.
	 */
	public ObjectEncryptor(SecretKey key, EncryptionMethod encryptionMethod)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		this.key = key;
		this.password = keyToString(key);

		this.encryptor = Cipher.getInstance(encryptionMethodToString(encryptionMethod));
		this.encryptor.init(Cipher.ENCRYPT_MODE, key);

		this.decryptor = Cipher.getInstance(encryptionMethodToString(encryptionMethod));
		this.decryptor.init(Cipher.DECRYPT_MODE, key);
	}

	public ObjectEncryptor(String password, EncryptionMethod encryptionMethod)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		this.password = password;

		KeyGenerator keyGen = KeyGenerator.getInstance(encryptionMethodToString(encryptionMethod));
		SecureRandom random = new SecureRandom();
		keyGen.init(random);
		key = keyGen.generateKey();

		this.encryptor = Cipher.getInstance(encryptionMethodToString(encryptionMethod));
		this.encryptor.init(Cipher.ENCRYPT_MODE, key);

		this.decryptor = Cipher.getInstance(encryptionMethodToString(encryptionMethod));
		this.decryptor.init(Cipher.DECRYPT_MODE, key);
	}

	public static ObjectEncryptor createRandomInstance(EncryptionMethod encryptionMethod)
			throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
		KeyGenerator keyGen = KeyGenerator.getInstance(encryptionMethodToString(encryptionMethod));
		SecureRandom random = new SecureRandom();
		keyGen.init(random);
		return new ObjectEncryptor(keyGen.generateKey(), encryptionMethod);
	}

	/**
	 * Encrypts a <code>Serializable</code> object.
	 * 
	 * @param obj
	 *            - The object to encrypt.
	 * @return SealedObject - The encrypted object.
	 * @throws IllegalBlockSizeException
	 *             If the given cipher is a block cipher, no padding has been
	 *             requested, and the total input length is not a multiple of
	 *             the cipher's block size.
	 * @throws IOException
	 *             If an error occurs during serialization.
	 * @throws UnencryptableException
	 *             If <code>obj</code> is unencryptable.
	 */
	public SealedObject encrypt(Serializable obj)
			throws IllegalBlockSizeException, IOException, UnencryptableException {
		if (obj instanceof Unencryptable)
			throw new UnencryptableException("obj is unencryptable");

		return new SealedObject(obj, encryptor);
	}

	/**
	 * Decrypts a previously encrypted object encrypted with the current
	 * instance of <code>ObjectEncryptor</code> or with the same key as the
	 * current instance of <code>ObjectEncryptor</code>.
	 * 
	 * @param obj
	 *            - The previously encrypted object.
	 * @return Object - The decrypted object.
	 * @throws ClassNotFoundException
	 *             If an error occurs during deserialization.
	 * @throws IllegalBlockSizeException
	 *             If the given cipher is a block cipher, no padding has been
	 *             requested, and the total input length is not a multiple of
	 *             the cipher's block size.
	 * @throws BadPaddingException
	 *             If the given cipher has been initialized for decryption, and
	 *             padding has been specified, but the input data does not have
	 *             proper expected padding bytes.
	 * @throws IOException
	 *             If an error occurs during deserialization.
	 */
	public Object decrypt(SealedObject obj)
			throws ClassNotFoundException, IllegalBlockSizeException, BadPaddingException, IOException {
		return obj.getObject(decryptor);
	}

	/**
	 * Gets the encrypting <code>Cipher</code>.
	 * 
	 * @return The <code>Cipher</code>.
	 */
	public Cipher getEncryptor() {
		return this.encryptor;
	}

	/**
	 * Gets the decrypting <code>Cipher</code>.
	 * 
	 * @return The <code>Cipher</code>.
	 */
	public Cipher getDecrpytor() {
		return this.decryptor;
	}

	/**
	 * Gets the <code>SecretKey</code>.
	 * 
	 * @return The key.
	 */
	public SecretKey getKey() {
		return this.key;
	}

	/**
	 * Gets the password set when the constructor was called. If no password was
	 * specified, it is the decoded encoding of the key. Otherwise, it is the
	 * originally set password.
	 * 
	 * @return String - The password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Turns a <code>SecretKey<code> into a String (like a password).
	 * 
	 * &#64;param key
	 *            - The key to retrieve the encoding of.
	 * &#64;return String - The encoded key.
	 * 
	 * @throws NullPointerException If <code>key</code> is <code>null</code>.
	 */
	public static final String keyToString(SecretKey key) {
		return new String(Base64.encodeBase64(key.getEncoded()));
	}

	/**
	 * Turns a String's byte array into a <code>SecretKey</code>.
	 * 
	 * @param key
	 *            - The String.
	 * @return SecretKey - The new key.
	 * 
	 * @throws NullPointerException
	 *             If <code>key</code> is <code>null</code>.
	 */
	public static final SecretKey stringToKey(String key, EncryptionMethod encryptionMethod) {
		byte[] decoded = Base64.decodeBase64(key);
		return new SecretKeySpec(decoded, 0, decoded.length, encryptionMethodToString(encryptionMethod));
	}

	/**
	 * Compares a given <code>ObjectEncryptor</code>'s key to the current
	 * instance's key.
	 * 
	 * @return boolean - If the key's encoded is equal to the current instance's
	 *         key's encoded.
	 * 
	 * @throws NullPointerException
	 *             If <code>obj</code is <code>null</code>.
	 * 
	 * @see javax.crypto.SecretKey#getEncoded()
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			throw new NullPointerException();

		if (obj instanceof ObjectEncryptor) {
			ObjectEncryptor enc = (ObjectEncryptor) obj;
			return Arrays.equals(enc.getKey().getEncoded(), getKey().getEncoded());
		}
		return false;
	}

	/**
	 * Turns an <code>EncryptionMode</code> into a corresponding String.
	 * 
	 * @param m
	 *            - The EncryptionMethod.
	 * @return String - The corresponding String.
	 */
	public static final String encryptionMethodToString(EncryptionMethod m) {
		switch (m) {
		case AES:
			return "AES";
		case BLOWFISH:
			return "Blowfish";
		case DES:
			return "DES";
		case DESEDE:
			return "DESede";
		case DIFFIEHELLMAN:
			return "DiffieHellman";
		case DSA:
			return "DSA";
		case OAEP:
			return "OAEP";
		case PBE:
			return "PBE";
		case RC2:
			return "RC2";
		default:
			return null;
		}
	}

	/**
	 * An enum containing the available encryption methods.
	 * 
	 * @author Lucas Baizer
	 */
	public static enum EncryptionMethod {
		AES, BLOWFISH, DES, DESEDE, DIFFIEHELLMAN, DSA, OAEP, PBE, RC2
	}
}