package com.jslib.template.xhtml;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import org.xml.sax.SAXException;

import com.jslib.api.dom.Document;
import com.jslib.api.dom.EList;
import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;

import junit.framework.TestCase;

public class AttributeOperatorUnitTest extends TestCaseEx
{
  public void testAddCssClass() throws SAXException
  {
    String bodyFragment = "<div data-css-class='id=1964:birth-year'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("birth-year", el.getAttr("class"));
  }

  public void testRemoveCssClass() throws SAXException
  {
    String bodyFragment = "<div class='birth-year' data-css-class='!id=1964:birth-year'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("class"));
  }

  public void testAddRemoveCssClass() throws SAXException
  {
    String bodyFragment = "<div class='birth-year' data-css-class='!id=1964:birth-year;id=1964:birth-day'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("birth-day", el.getAttr("class"));
  }

  public void testPreserveClassIfDataCssClassIsNotPresent() throws SAXException
  {
    String bodyFragment = "<div class='birth-year'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("birth-year", el.getAttr("class"));
  }

  public void testAttr() throws SAXException
  {
    String bodyFragment = "<div data-attr='title:description;id:id;'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("1964", el.getAttr("id"));
    assertEquals("some description", el.getAttr("title"));
  }

  public void testBothStaticAndOperatorAttr() throws SAXException
  {
    String bodyFragment = "<div id='1' data-attr='title:description;id:id;'></div>";
    Pojo model = new Pojo();
    try {
      run(bodyFragment, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Bad id element should throw templates exception.");
  }

  public void testBadAttrExpression() throws SAXException
  {
    Pojo model = new Pojo();
    for(String badExpression : new String[]
    {
        "", ";", ":", "title;description", "title:;", "title;description:"
    }) {
      String bodyFragment = String.format("<div data-attr='%s'></div>", badExpression);
      try {
        run(bodyFragment, model);
      }
      catch(TemplateException e) {
        return;
      }
      fail("Bad expression should throw templates exception.");
    }
  }

  public void testNullAttr() throws SAXException
  {
    String html = "<div data-attr='title:description'></div>";
    Pojo model = new Pojo();
    model.description = null;
    Document doc = run(html, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("title"));
  }

  public void testUndefinedAttr() throws SAXException
  {
    String html = "<div data-attr='title:undefinedDescription' title='title'></div>";
    Pojo model = new Pojo();
    try {
      run(html, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Undefind attribute value should throw templates exception.");
  }

  public void testId() throws SAXException
  {
    String bodyFragment = "<div data-id='id' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("1964", el.getAttr("id"));
  }

  public void testBadIdType() throws SAXException
  {
    for(String propertyPath : new String[]
    {
        "flag", "date", "link", "inner"
    }) {
      String bodyFragment = String.format("<div data-id='%s' />", propertyPath);
      Pojo model = new Pojo();
      try {
        run(bodyFragment, model);
      }
      catch(TemplateException e) {
        continue;
      }
      fail("Bad 'id' type should throw templates exception.");
    }
  }

  public void testNullId() throws SAXException
  {
    String bodyFragment = "<div data-id='id' />";
    Pojo model = new Pojo();
    model.id = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("id"));
  }

  public void testBothStaticAndOperatorId() throws SAXException
  {
    String bodyFragment = "<div id='1' data-id='id' />";
    Pojo model = new Pojo();
    try {
      run(bodyFragment, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Both static and operand ID should throw templates exception.");
  }

  public void testSrc() throws SAXException
  {
    String bodyFragment = "<img data-src='picture' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("img");
    assertEquals("images/user.png", el.getAttr("src"));
  }

  public void testBadSrcType() throws SAXException
  {
    for(String propertyPath : new String[]
    {
        "flag", "date", "id", "inner"
    }) {
      String bodyFragment = String.format("<img data-src='%s' />", propertyPath);
      Pojo model = new Pojo();
      try {
        run(bodyFragment, model);
      }
      catch(TemplateException e) {
        continue;
      }
      fail("Bad 'src' type should throw templates exception.");
    }
  }

  public void testNullSrc() throws SAXException
  {
    String bodyFragment = "<img data-src='picture' />";
    Pojo model = new Pojo();
    model.picture = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("img");
    assertNull(el.getAttr("src"));
  }

  public void testBothStaticAndOperatorSrc() throws SAXException
  {
    String bodyFragment = "<img src='image.png' data-src='picture' />";
    Pojo model = new Pojo();
    try {
      run(bodyFragment, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Both static and operator SRC should throw templates exception.");
  }

  public void testHref() throws SAXException
  {
    String bodyFragment = "<a data-href='link'>link</a>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("a");
    assertEquals("http://server.com/app/index.xsp", el.getAttr("href"));
  }

  public void testBadHrefType() throws SAXException
  {
    for(String propertyPath : new String[]
    {
        "flag", "date", "id", "inner"
    }) {
      String bodyFragment = String.format("<a data-href='%s'>link</a>", propertyPath);
      Pojo model = new Pojo();
      try {
        run(bodyFragment, model);
      }
      catch(TemplateException e) {
        continue;
      }
      fail("Bad 'href' type should throw templates exception.");
    }
  }

  public void testNullHref() throws SAXException
  {
    String bodyFragment = "<a data-href='link'>link</a>";
    Pojo model = new Pojo();
    model.link = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("a");
    assertNull(el.getAttr("src"));
  }

  public void testBothStaticAndOperatorHref() throws SAXException
  {
    String bodyFragment = "<a href='#' data-href='link'>link</a>";
    Pojo model = new Pojo();
    try {
      run(bodyFragment, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Both static and operator HREF should throw templates exception.");
  }

  public void testTitle() throws SAXException
  {
    String bodyFragment = "<div data-title='description' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("some description", el.getAttr("title"));
  }

  public void testBadTitleType() throws SAXException
  {
    for(String propertyPath : new String[]
    {
        "flag", "date", "id", "inner"
    }) {
      String bodyFragment = String.format("<div data-title='%s' />", propertyPath);
      Pojo model = new Pojo();
      try {
        run(bodyFragment, model);
      }
      catch(TemplateException e) {
        continue;
      }
      fail("Bad 'title' type should throw templates exception.");
    }
  }

  public void testNullTitle() throws SAXException
  {
    String bodyFragment = "<div data-title='description' />";
    Pojo model = new Pojo();
    model.description = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("title"));
  }

  public void testBothStaticAndOperatorTitle() throws SAXException
  {
    String bodyFragment = "<div title='title' data-title='description' />";
    Pojo model = new Pojo();
    try {
      run(bodyFragment, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Bad id element should throw templates exception.");
  }

  public void testValue() throws SAXException
  {
    String bodyFragment = "<input data-value='description' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("input");
    assertEquals("some description", el.getAttr("value"));
  }

  public void testNullValue() throws SAXException
  {
    String bodyFragment = "<input data-value='description' />";
    Pojo model = new Pojo();
    model.description = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("input");
    assertNull(el.getAttr("value"));
  }

  public void testBothStaticAndOperatorValue() throws SAXException
  {
    String bodyFragment = "<input value='value' data-value='description' />";
    Pojo model = new Pojo();
    try {
      run(bodyFragment, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Both static and operator VALUE should throw templates exception.");
  }

  public void testFormattedValue() throws SAXException
  {
    String html = "<input data-value='description' data-format='com.jslib.template.xhtml.AttributeOperatorUnitTest$Format' />";
    Pojo model = new Pojo();
    Document doc = run(html, model);

    Element el = doc.getByTag("input");
    assertEquals("SOME DESCRIPTION", el.getAttr("value"));
  }

  public void testBadTypeValueWithoutFormat() throws SAXException
  {
    for(String propertyPath : new String[]
    {
        "inner", "array"
    }) {
      String bodyFragment = String.format("<input data-value='%s' />", propertyPath);
      Pojo model = new Pojo();
      try {
        run(bodyFragment, model);
      }
      catch(TemplateException e) {
        continue;
      }
      fail("Bad 'value' type whitout formatter should throw templates exception.");
    }
  }

  public void testAttrOnObject() throws SAXException
  {
    String bodyFragment = "" + //
        "<div data-attr='title:description;'></div>" + //
        "<div data-attr='title:description;' data-object='inner'></div>" + //
        "<div data-attr='title:description;'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    EList elist = doc.findByTag("div");
    assertEquals("some description", elist.item(0).getAttr("title"));
    assertEquals("some description", elist.item(1).getAttr("title"));
    assertEquals("some description", elist.item(2).getAttr("title"));
  }

  // ------------------------------------------------------
  // fixture initialization and helpers

  @SuppressWarnings("unused")
  private static class Pojo
  {
    Integer id = 1964;
    String description = "some description";
    String picture = "images/user.png";
    URL link;
    InnerPojo inner = new InnerPojo();
    boolean flag = true;
    Date date = new Date();
    String[] array = new String[1];

    Pojo()
    {
      try {
        this.link = new URL("http://server.com/app/index.xsp");
      }
      catch(MalformedURLException e) {
        e.printStackTrace();
      }
    }

    private static class InnerPojo
    {
      int id = 2012;
      String description = "inner description";
    }
  }

  @SuppressWarnings("unused")
  private static class Format implements com.jslib.format.Format
  {
    @Override
    public String format(Object object)
    {
      TestCase.assertTrue(object instanceof String);
      return ((String)object).toUpperCase();
    }

    @Override
    public Object parse(String value) throws ParseException
    {
      throw new UnsupportedOperationException();
    }
  }
}
