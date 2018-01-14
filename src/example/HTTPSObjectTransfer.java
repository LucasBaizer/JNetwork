package example;

import java.io.File;

import org.jnetwork.AuthorizationException;
import org.jnetwork.HTTPAuthorization;
import org.jnetwork.HTTPBasicAuthorization;
import org.jnetwork.HTTPContentType;
import org.jnetwork.HTTPHeader;
import org.jnetwork.HTTPResponse;
import org.jnetwork.HTTPResponseCodes;
import org.jnetwork.HTTPSServer;
import org.jnetwork.Keystore;

public class HTTPSObjectTransfer {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			HTTPResponse.addDefaultHeader(new HTTPHeader(HTTPHeader.ACCESS_ALLOW_CONTROL_ORIGIN, "*"));
			HTTPResponse.addDefaultHeader(new HTTPHeader(HTTPHeader.CONTENT_TYPE, HTTPContentType.APPLICATION_JSON));

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
					return res.error(HTTPResponseCodes.UNAUTHORIZED, "requests to /test must be authorized");
				}

				HTTPBasicAuthorization auth = null;
				try {
					auth = HTTPAuthorization.fromHeader(authHeader);
				} catch (AuthorizationException e) {
					e.printStackTrace();
				}
				if (!auth.matches("username", "password")) {
					return res.error(HTTPResponseCodes.FORBIDDEN,
							"supplied username and password do not match server's credentials");
				}

				return res.contentType(HTTPContentType.TEXT_HTML).send("<html><body>Hello, world!</body></html>");
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
