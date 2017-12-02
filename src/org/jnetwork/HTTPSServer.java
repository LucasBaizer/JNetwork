package org.jnetwork;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public class HTTPSServer extends HTTPServer {
	private Keystore keystore;

	public HTTPSServer(Keystore keystore) {
		this(443, keystore);
	}

	public HTTPSServer(int port, Keystore keystore) {
		super(port);

		SSLServer.ensureProvider();
		SSLServer.setStaticKeystore(keystore);
		this.keystore = keystore;
	}

	@Override
	public void start() throws IOException {
		server = HttpsServer.create(boundAddress == null ? new InetSocketAddress(getBoundPort())
				: new InetSocketAddress(boundAddress, getBoundPort()), 0);

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");

			FileInputStream fis = new FileInputStream(keystore.getKeystoreFile());

			KeyStore ks = KeyStore.getInstance("JKS");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			ks.load(fis, keystore.getPasswordArray());
			kmf.init(ks, keystore.getPasswordArray());
			tmf.init(ks);

			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			((HttpsServer) server).setHttpsConfigurator(new HttpsConfigurator(sslContext) {
				public void configure(HttpsParameters params) {
					try {
						SSLContext c = SSLContext.getDefault();
						SSLEngine engine = c.createSSLEngine();
						params.setNeedClientAuth(false);
						params.setCipherSuites(engine.getEnabledCipherSuites());
						params.setProtocols(engine.getEnabledProtocols());

						SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
						params.setSSLParameters(defaultSSLParameters);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (Exception e) {
			throw new IOException(e);
		}

		server.setExecutor(null);
		server.start();
	}
}
