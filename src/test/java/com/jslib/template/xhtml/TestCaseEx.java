package com.jslib.template.xhtml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.TimeZone;

import org.junit.Ignore;
import org.xml.sax.SAXException;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.DocumentBuilder;
import com.jslib.api.dom.EList;
import com.jslib.dom.DocumentBuilderImpl;

import junit.framework.TestCase;

@Ignore
class TestCaseEx extends TestCase
{
  private static boolean initialized;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    if(!initialized) {
      initialized = true;
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
  }

  protected static DocumentBuilder getBuilder()
  {
    return new DocumentBuilderImpl();
  }

  protected static Document run(String bodyFragment, Object model) throws SAXException
  {
    StringBuilder htmlBuilder = new StringBuilder();
    htmlBuilder.append("<html><head></head><body>");
    htmlBuilder.append(bodyFragment);
    htmlBuilder.append("</body></html>");

    DocumentBuilder builder = getBuilder();
    Document document = builder.parseHTML(htmlBuilder.toString());
    XhtmlTemplate template = new XhtmlTemplate("test", document);

    StringWriter writer = new StringWriter();
    try {
      template.serialize(model, writer);
    }
    catch(IOException e) {
      fail(e.getMessage());
    }
    return builder.parseHTML(writer.toString());
  }

  protected static void assertEquals(String expected, EList elist, int index)
  {
    assertEquals(expected, elist.item(index).getText().trim());
  }
}
