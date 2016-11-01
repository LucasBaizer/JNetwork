package org.jnetwork;

public interface SecureServer {
	public void setKeystore(Keystore keystore) throws CryptographyException;

	public Keystore getKeystore();

	public void useRandomKeystore() throws CryptographyException;
}
