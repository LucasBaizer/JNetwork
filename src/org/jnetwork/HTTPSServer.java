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

public class HTTPSServer extends SSLServer {
	private HttpsServer server;

	public HTTPSServer(Keystore keystore) {
		this(443, keystore);
	}

	public HTTPSServer(int port, Keystore keystore) {
		super(port, null, keystore);
	}

	@Override
	public void start() throws IOException {
		server = HttpsServer.create(new InetSocketAddress(getBoundPort()), 0);

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
			server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
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

	public HTTPSServer get(String uri, HTTPConnectionCallback back) throws ServerException {
		if (server == null) {
			throw new ServerException("Server must be started before endpoints are configured");
		}

		server.createContext(uri, (http) -> {
			HTTPRequest req = new HTTPRequest(http);
			HTTPResponse res = new HTTPResponse(http);
			back.get(req, res);
		});
		return this;
	}

	@Override
	protected void launchNewThread() throws IOException {
		// this doesn't matter
	}

	@Override
	public void close() throws IOException {
	}
}
