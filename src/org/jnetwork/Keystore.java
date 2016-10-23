package org.jnetwork;

import java.io.File;
import java.io.Serializable;

/**
 * A class used to reference a keystore file on the computer. To create a new
 * keystore, run the following command in the JDK/bin directory: <br>
 * <code>keytool -genkey -alias {MY_ALIAS} -keyalg RSA -keystore {MY_KEYSTORE_NAME}.jks -keysize 2048</code><br>
 * <br>
 * For more information, see the <a href=
 * "http://docs.oracle.com/javase/6/docs/technotes/tools/solaris/keytool.html">official
 * documentation on keytool</a>.
 * 
 * @author Lucas Baizer
 */
public class Keystore implements Serializable {
	private static final long serialVersionUID = 1765662863452096789L;

	private File keystoreLocation;
	private String password;

	/**
	 * Creates a new reference to a keystore on the computer.
	 * 
	 * @param location
	 *            - The path to the keystore, i.e. <code>/foo/bar/key.jks</code>
	 * @param alias
	 *            - The keystore alias, which was specified when the keystore
	 *            was created with an external tool.
	 * @param password
	 *            - The keystore password, which was specified when the keystore
	 *            was created with an external tool.
	 */
	public Keystore(File location, String password) {
		this.keystoreLocation = location;
		this.password = password;
	}

	/**
	 * @return the location of the keystore on the file system.
	 */
	public File getKeystoreLocation() {
		return keystoreLocation;
	}

	/**
	 * @return the password to the keystore.
	 */
	public String getPassword() {
		return password;
	}
}
