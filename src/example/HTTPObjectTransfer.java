package example;

import org.jnetwork.HTTPConnection;
import org.jnetwork.HTTPContentType;
import org.jnetwork.HTTPHeader;
import org.jnetwork.HTTPResponse;
import org.jnetwork.HTTPResult;
import org.jnetwork.HTTPServer;

public class HTTPObjectTransfer {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		try {
			HTTPResponse.addDefaultHeader(new HTTPHeader(HTTPHeader.ACCESS_ALLOW_CONTROL_ORIGIN, "*"));

			HTTPServer server = new HTTPServer(9292);
			server.start();

			server.get("/test", (req, res) -> {
				for (HTTPHeader header : req.headers()) {
					System.out.println(header);
				}
				return res.contentType(HTTPContentType.TEXT_HTML).send("<html><body>Hello, world!</body></html>");
			});

			HTTPConnection.setKeepAliveEnabled(true);
			HTTPConnection connection = new HTTPConnection("localhost/test", 9292);
			HTTPResult result = connection.accept(HTTPContentType.TEXT_HTML).get();
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
