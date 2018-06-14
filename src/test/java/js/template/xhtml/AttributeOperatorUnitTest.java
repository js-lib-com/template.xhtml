package js.template.xhtml;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import js.dom.Document;
import js.dom.EList;
import js.dom.Element;
import js.template.TemplateException;
import junit.framework.TestCase;

public class AttributeOperatorUnitTest extends TestCaseEx
{
  public void testAddCssClass()
  {
    String bodyFragment = "<div data-css-class='id=1964:birth-year'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("birth-year", el.getAttr("class"));
  }

  public void testRemoveCssClass()
  {
    String bodyFragment = "<div class='birth-year' data-css-class='!id=1964:birth-year'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("class"));
  }

  public void testAddRemoveCssClass()
  {
    String bodyFragment = "<div class='birth-year' data-css-class='!id=1964:birth-year;id=1964:birth-day'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("birth-day", el.getAttr("class"));
  }

  public void testPreserveClassIfDataCssClassIsNotPresent()
  {
    String bodyFragment = "<div class='birth-year'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("birth-year", el.getAttr("class"));
  }

  public void testAttr()
  {
    String bodyFragment = "<div data-attr='title:description;id:id;'></div>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("1964", el.getAttr("id"));
    assertEquals("some description", el.getAttr("title"));
  }

  public void testBothStaticAndOperatorAttr()
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

  public void testBadAttrExpression()
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

  public void testNullAttr()
  {
    String html = "<div data-attr='title:description'></div>";
    Pojo model = new Pojo();
    model.description = null;
    Document doc = run(html, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("title"));
  }

  public void testUndefinedAttr()
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

  public void testId()
  {
    String bodyFragment = "<div data-id='id' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("1964", el.getAttr("id"));
  }

  public void testBadIdType()
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

  public void testNullId()
  {
    String bodyFragment = "<div data-id='id' />";
    Pojo model = new Pojo();
    model.id = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("id"));
  }

  public void testBothStaticAndOperatorId()
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

  public void testSrc()
  {
    String bodyFragment = "<img data-src='picture' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("img");
    assertEquals("images/user.png", el.getAttr("src"));
  }

  public void testBadSrcType()
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

  public void testNullSrc()
  {
    String bodyFragment = "<img data-src='picture' />";
    Pojo model = new Pojo();
    model.picture = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("img");
    assertNull(el.getAttr("src"));
  }

  public void testBothStaticAndOperatorSrc()
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

  public void testHref()
  {
    String bodyFragment = "<a data-href='link'>link</a>";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("a");
    assertEquals("http://server.com/app/index.xsp", el.getAttr("href"));
  }

  public void testBadHrefType()
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

  public void testNullHref()
  {
    String bodyFragment = "<a data-href='link'>link</a>";
    Pojo model = new Pojo();
    model.link = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("a");
    assertNull(el.getAttr("src"));
  }

  public void testBothStaticAndOperatorHref()
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

  public void testTitle()
  {
    String bodyFragment = "<div data-title='description' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertEquals("some description", el.getAttr("title"));
  }

  public void testBadTitleType()
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

  public void testNullTitle()
  {
    String bodyFragment = "<div data-title='description' />";
    Pojo model = new Pojo();
    model.description = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("div");
    assertNull(el.getAttr("title"));
  }

  public void testBothStaticAndOperatorTitle()
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

  public void testValue()
  {
    String bodyFragment = "<input data-value='description' />";
    Pojo model = new Pojo();
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("input");
    assertEquals("some description", el.getAttr("value"));
  }

  public void testNullValue()
  {
    String bodyFragment = "<input data-value='description' />";
    Pojo model = new Pojo();
    model.description = null;
    Document doc = run(bodyFragment, model);

    Element el = doc.getByTag("input");
    assertNull(el.getAttr("value"));
  }

  public void testBothStaticAndOperatorValue()
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

  public void testFormattedValue()
  {
    String html = "<input data-value='description' data-format='js.template.xhtml.AttributeOperatorUnitTest$Format' />";
    Pojo model = new Pojo();
    Document doc = run(html, model);

    Element el = doc.getByTag("input");
    assertEquals("SOME DESCRIPTION", el.getAttr("value"));
  }

  public void testBadTypeValueWithoutFormat()
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

  public void testAttrOnObject()
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
  private static class Format implements js.format.Format
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
