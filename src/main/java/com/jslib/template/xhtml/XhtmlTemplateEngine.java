package com.jslib.template.xhtml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.DocumentBuilder;
import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;
import com.jslib.api.template.Template;
import com.jslib.api.template.TemplateEngine;
import com.jslib.api.template.TemplateException;
import com.jslib.util.Classes;

/**
 * X(HT)ML implementation for template engine interface. This implementation uses X(HT)ML documents to store templates.
 * Since parsing DOM document is costly this implementation uses internal cache of parsed DOM documents, for all used
 * templates. Anyway, template DOM document is loaded and parsed on the fly, at first usage.
 * <p>
 * This implementation depends on Simplified X(HT)ML DOM Interface and expected a service to provide instance for
 * {@link DocumentBuilder}.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public class XhtmlTemplateEngine implements TemplateEngine
{
  /** Class logger. */
  private static final Log log = LogFactory.getLog(XhtmlTemplateEngine.class);

  /** DOM document builder. */
  private DocumentBuilder documentBuilder;
  /** Cache for parsed DOM documents. */
  private Map<String, Document> cache = new HashMap<>();

  /**
   * Loads service instance for DOM document builder.
   */
  public XhtmlTemplateEngine()
  {
    documentBuilder = Classes.loadService(DocumentBuilder.class);
  }

  @Override
  public void setProperty(String name, Object value)
  {
  }

  @Override
  public Template getTemplate(String templateName, Reader reader) throws IOException
  {
    Document document = cache.get(templateName);
    if(document == null) {
      synchronized(this) {
        if(document == null) {
          document = loadTemplateDocument(templateName, documentBuilder, reader);
          cache.put(templateName, document);
        }
      }
    }
    return new XhtmlTemplate(templateName, document);
  }

  @Override
  public Template getTemplate(File file) throws IOException
  {
    return getTemplate(file.getAbsolutePath(), new FileReader(file));
  }

  /**
   * Loads and parses template document then returns it.
   * 
   * @param builder DOM builder used to load document template,
   * @param reader template document reader.
   * @throws IOException if read operation fails or premature EOF.
   * @throws TemplateException if template document is not XML or HTML.
   */
  private static Document loadTemplateDocument(String templateName, DocumentBuilder builder, Reader reader) throws IOException
  {
    final int READ_AHEAD_SIZE = 5;
    BufferedReader bufferedReader = new BufferedReader(reader);
    bufferedReader.mark(READ_AHEAD_SIZE);

    char[] cbuf = new char[READ_AHEAD_SIZE];
    for(int i = 0; i < READ_AHEAD_SIZE; ++i) {
      int c = bufferedReader.read();
      if(c == -1) {
        throw new IOException(String.format("Invalid X(HT)ML template |%s|. Premature EOF.", templateName));
      }
      cbuf[i] = (char)c;
    }
    String header = new String(cbuf);
    if(header.charAt(0) != '<') {
      throw new TemplateException("Invalid X(HT)ML template |%s|. Seems not XML like document.", templateName);
    }

    // trivial heuristic to determine file is XML or HTML; if is not explicitly HTML is considered XML
    boolean isXML = true;
    if(header.charAt(1) == '!') {
      // HTML DOCTYPE always starts with <!
      isXML = false;
    }
    else if(header.charAt(1) == '?') {
      // XML prolog always starts with <?
      isXML = true;
    }
    else {
      // if not explicitly found HTML DOCTYPE or XML prolog uses root element to detect if HTML
      // if anything else but <html document is considered XML
      log.warn("No prolog found for X(HT)ML template |{template_name}|. Uses root element to detect document type.", templateName);
      if(header.toLowerCase().startsWith("<html")) {
        isXML = false;
      }
    }

    bufferedReader.reset();
    try {
      return isXML ? builder.loadXML(bufferedReader) : builder.loadHTML(bufferedReader);
    }
    catch(SAXException e) {
      throw new IOException(String.format("Fail to load template |%s|. Root cause: %s", templateName, e.getMessage()));
    }
    // do not attempt to close reader because DocumentBuilder.load[X|HT]ML() method takes care of that
  }
}
