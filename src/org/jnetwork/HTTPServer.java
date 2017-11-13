package org.jnetwork;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.sun.net.httpserver.HttpServer;

public class HTTPServer extends TCPServer {
	private HttpServer server;

	public HTTPServer() {
		this(80);
	}

	public HTTPServer(int port) {
		super(port, null);
	}

	@Override
	public void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(getBoundPort()), 0);
		server.setExecutor(null);
		server.start();
	}

	public HTTPServer get(String uri, HTTPConnectionCallback back) throws ServerException {
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

	public HTTPServer serveDirectory(File dir) throws IOException {
		return serveDirectory(dir, "index.html");
	}

	public HTTPServer serveDirectory(File dir, String index) throws IOException {
		return serveDirectory(dir, new File(dir, index));
	}

	public HTTPServer serveDirectory(File dir, String index, boolean recurse) throws IOException {
		return serveDirectory(dir, new File(dir, index), recurse);
	}

	public HTTPServer serveDirectory(File dir, File index) throws IOException {
		return serveDirectory(dir, index, true);
	}

	public HTTPServer serveDirectory(File dir, File index, boolean recurse) throws IOException {
		return serveDirectory(dir, dir, index, recurse);
	}

	private HTTPServer serveDirectory(File root, File dir, File index, boolean recurse) throws IOException {
		if (!dir.exists()) {
			throw new FileNotFoundException(dir.toString());
		}
		if (!dir.isDirectory()) {
			throw new ServerException("supplied directory is not a directory");
		}
		if (!index.exists()) {
			throw new FileNotFoundException(index.toString());
		}
		if (!index.isFile()) {
			throw new ServerException("supplied index is not a file");
		}

		int len = root.getAbsolutePath().length();

		serveFile(dir.getAbsolutePath().substring(len) + "/", index);
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				serveFile(file.getAbsolutePath().substring(len), file);
			} else if (file.isDirectory() && recurse) {
				serveDirectory(root, file, new File(file, index.getName()), true);
			}
		}

		return this;
	}

	public HTTPServer serveFile(String path, File file) throws IOException {
		String contents = new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1);

		path = path.replace("\\", "/");
		server.createContext(path, (http) -> {
			HTTPRequest req = new HTTPRequest(http);
			HTTPResponse res = new HTTPResponse(http);

			HTTPHeader header = req.header(HTTPHeader.CONTENT_TYPE);
			if (!header.isValidHeader()) {
				header = new HTTPHeader(HTTPHeader.CONTENT_TYPE, getContentType(file));
			}
			res.contentType(header.getFirstValue()).send(contents);
		});

		return this;
	}

	private static String getContentType(File file) {
		String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
		switch (extension) {
		case "html":
			return HTTPContentTypes.TEXT_HTML;
		case "js":
			return HTTPContentTypes.TEXT_JAVASCRIPT;
		case "css":
			return HTTPContentTypes.TEXT_CSS;
		case "xml":
			return HTTPContentTypes.APPLICATION_XML;
		case "pdf":
			return HTTPContentTypes.APPLICATION_PDF;
		case "jpg":
		case "jpeg":
			return HTTPContentTypes.IMAGE_JPEG;
		case "png":
			return HTTPContentTypes.IMAGE_PNG;
		case "gif":
			return HTTPContentTypes.IMAGE_GIF;
		case "mp4":
			return HTTPContentTypes.VIDEO_MP4;
		case "json":
			return HTTPContentTypes.APPLICATION_JSON;
		case "zip":
			return HTTPContentTypes.APPLICATION_ZIP;
		default:
			return HTTPContentTypes.TEXT_PLAIN;
		}
	}

	@Override
	protected void launchNewThread() throws IOException {
		// this doesn't matter
	}

	@Override
	public void close() throws IOException {
	}
}
