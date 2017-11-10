package example;

import java.io.File;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.jnetwork.HTTPAuthorization;
import org.jnetwork.HTTPBasicAuthorization;
import org.jnetwork.HTTPConnection;
import org.jnetwork.HTTPContentTypes;
import org.jnetwork.HTTPHeader;
import org.jnetwork.HTTPMethodTypes;
import org.jnetwork.HTTPResponse;
import org.jnetwork.HTTPResponseCodes;
import org.jnetwork.HTTPResult;
import org.jnetwork.HTTPSConnection;
import org.jnetwork.HTTPSServer;
import org.jnetwork.Keystore;

public class HTTPSObjectTransfer {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			HTTPResponse.addDefaultHeader(new HTTPHeader(HTTPHeader.ACCESS_ALLOW_CONTROL_ORIGIN, "*"));

			HTTPSServer server = new HTTPSServer(9191,
					new Keystore(new File("keystore.jks"), "password", "alias", "password"));
			server.start();

			server.get("/test", (req, res) -> {
				System.out.println("<-- Request -->");
				for (HTTPHeader header : req.headers()) {
					System.out.println(header);
				}
				System.out.println();

				HTTPHeader authHeader = req.header(HTTPHeader.AUTHORIZATION);
				if (!authHeader.isValidHeader()) {
					res.code(HTTPResponseCodes.UNAUTHORIZED).send();
					return;
				}

				HTTPBasicAuthorization auth = HTTPAuthorization.fromHeader(authHeader);
				if (!auth.matches("username", "password")) {
					res.code(HTTPResponseCodes.FORBIDDEN).send();
					return;
				}

				res.contentType(HTTPContentTypes.TEXT_HTML).send("<html><body>Hello, world!</body></html>");
			});

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
			HTTPResult result = connection.accept(HTTPContentTypes.TEXT_HTML).method(HTTPMethodTypes.GET)
					.authorization(new HTTPBasicAuthorization("username", "password")).send();
			System.out.println("<-- Response -->");
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
