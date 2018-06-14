package js.template.xhtml;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.template.Template;
import js.template.TemplateEngine;
import js.util.Classes;

public class XhtmlTemplateEngine implements TemplateEngine {
	private DocumentBuilder documentBuilder;
	private Map<File, Document> documentsCache = new HashMap<File, Document>();

	public XhtmlTemplateEngine() {
		documentBuilder = Classes.loadService(DocumentBuilder.class);
	}

	@Override
	public void setProperty(String name, Object value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Template getTemplate(File file) throws IOException {
		Document document = documentsCache.get(file);
		if (document == null) {
			synchronized (this) {
				if (document == null) {
					document = preloadTemplateDocument(documentBuilder, file);
					documentsCache.put(file, document);
				}
			}
		}
		return new XhtmlTemplate(document);
	}

	/**
	 * Preload this view template document. Loads and parse template document then store it into {@link #template}.
	 * 
	 * @param builder DOM builder used to load document template,
	 * @param file template file.
	 * @throws IOException if template read operation fails.
	 */
	private static Document preloadTemplateDocument(DocumentBuilder builder, File file) throws IOException {
		final int READ_AHEAD_SIZE = 20;
		InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
		assert inputStream.markSupported();
		inputStream.mark(READ_AHEAD_SIZE);

		byte[] bytes = new byte[READ_AHEAD_SIZE];
		// excerpt from InputStream APIDOC:
		// The read(b, off, len) method for class InputStream simply calls the method read() repeatedly.
		for (int i = 0; i < READ_AHEAD_SIZE; ++i) {
			bytes[i] = (byte) inputStream.read();
		}
		String prolog = new String(bytes, "UTF-8");
		// trivial heuristic to determine file is XML or HTML: check if file has XML declaration, i.e. starts with <?xml
		boolean isXML = prolog.startsWith("<?xml");

		inputStream.reset();
		return isXML ? builder.loadXML(inputStream) : builder.loadHTML(inputStream);
		// do not attempt to close input stream because Builder.load[X|HT]ML() method closes it
	}
}
