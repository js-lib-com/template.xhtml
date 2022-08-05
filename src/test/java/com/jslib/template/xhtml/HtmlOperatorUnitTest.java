package com.jslib.template.xhtml;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import org.xml.sax.SAXException;

import com.jslib.api.dom.Document;

public class HtmlOperatorUnitTest extends TestCaseEx
{
  public void testEscapeText() throws FileNotFoundException, IOException, SAXException
  {
    Document doc = getBuilder().parseHTML("<html><head></head><body data-text='.'></body></html>");
    XhtmlTemplate template = new XhtmlTemplate("test", doc);

    StringWriter writer = new StringWriter();
    template.serialize("<>&\"'", writer);
    assertTrue(writer.toString().contains("<BODY>&lt;&gt;&amp;&quot;&apos;</BODY>"));
  }
}
