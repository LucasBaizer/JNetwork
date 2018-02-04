package example;

import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jnetwork.HTTPBasicAuthorization;
import org.jnetwork.HTTPConnection;
import org.jnetwork.HTTPContentType;
import org.jnetwork.HTTPResult;
import org.jnetwork.HTTPSConnection;

import com.sun.net.ssl.internal.ssl.Provider;

public class HTTPSObjectTransferClient {
	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		Security.addProvider(new Provider());
		System.setProperty("javax.net.ssl.trustStore", "keystore.jks");

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier((x, y) -> true);

		HTTPConnection.setKeepAliveEnabled(true);
		HTTPSConnection connection = new HTTPSConnection("localhost/test", 9191);
		HTTPResult result = connection.accept(HTTPContentType.TEXT_HTML)
				.authorization(new HTTPBasicAuthorization("username", "password")).get();
		System.out.println("<-- Response -->");
		System.out.println(result);
	}
}
