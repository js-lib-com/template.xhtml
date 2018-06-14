package js.template.xhtml;

import js.dom.Document;
import js.dom.DocumentBuilder;
import js.dom.Element;
import js.template.TemplateException;

public class ConditionalOperatorUnitTest extends TestCaseEx
{
  public void testIfOperatorOnTrue()
  {
    String bodyFragment = "<div data-if='flag'></div>";
    Pojo model = new Pojo();
    model.flag = true;
    Document doc = run(bodyFragment, model);
    assertNotNull(doc.getByTag("div"));
  }

  public void testIfOperatorOnFalse()
  {
    String bodyFragment = "<div data-if='flag'></div>";
    Pojo model = new Pojo();
    model.flag = false;
    Document doc = run(bodyFragment, model);
    assertNull(doc.getByTag("div"));
  }

  public void testIfOperatorWithEmptyOperand()
  {
    String bodyFragment = "<div data-if=''></div>";
    Pojo model = new Pojo();
    try {
      run(bodyFragment, model);
      fail("Empty operand should rise templates exception.");
    }
    catch(TemplateException e) {
      assertTrue("Invalid error message.", e.getMessage().contains("Empty operand"));
    }
  }

  public void testNullIfValue()
  {
    String bodyFragment = "<div data-if='fruit=APPLE'>apple</div>";
    Pojo model = new Pojo();
    model.fruit = null;

    Document doc = run(bodyFragment, model);
    assertNull(doc.getByTag("div"));
  }

  public void testNotIfOperatorOnTrue()
  {
    String bodyFragment = "<div data-if='!flag'></div>";
    Pojo model = new Pojo();
    model.flag = true;
    Document doc = run(bodyFragment, model);
    assertNull(doc.getByTag("div"));
  }

  public void testNotIfOperatorOnFalse()
  {
    String bodyFragment = "<div data-ifnot='flag'></div>";
    Pojo model = new Pojo();
    model.flag = false;
    Document doc = run(bodyFragment, model);
    assertNotNull(doc.getByTag("div"));
  }

  public void testIfElse()
  {
    String bodyFragment = "" + //
        "<div data-if='flag'>IF</div>" + //
        "<div data-if='!flag'>ELSE</div>";
    Pojo model = new Pojo();

    model.flag = true;
    Document doc = run(bodyFragment, model);
    assertEquals(1, doc.findByTag("div").size());
    Element div = doc.getByTag("div");
    assertEquals("IF", div.getText());

    model.flag = false;
    doc = run(bodyFragment, model);
    assertEquals(1, doc.findByTag("div").size());
    div = doc.getByTag("div");
    assertEquals("ELSE", div.getText());
  }

  public void testCase()
  {
    String bodyFragment = "" + //
        "<div data-if='fruit=APPLE'>apple</div>" + //
        "<div data-if='fruit=ORANGE'>orange</div>" + //
        "<div data-if='fruit=BANANA'>banana</div>";
    Pojo model = new Pojo();

    model.fruit = Fruit.APPLE;
    Document doc = run(bodyFragment, model);
    assertEquals(1, doc.findByTag("div").size());
    Element div = doc.getByTag("div");
    assertEquals("apple", div.getText());

    model.fruit = Fruit.ORANGE;
    doc = run(bodyFragment, model);
    assertEquals(1, doc.findByTag("div").size());
    div = doc.getByTag("div");
    assertEquals("orange", div.getText());

    model.fruit = Fruit.NONE;
    doc = run(bodyFragment, model);
    assertNull(doc.getByTag("div"));
  }

  public void testExcludeOperatorOnTrue()
  {
    String bodyFragment = "" + //
        "<div data-exclude='true'>" + //
        "   <p data-text='fruit'></p>" + //
        "</div>";
    Pojo model = new Pojo();
    model.fruit = Fruit.APPLE;

    Document doc = run(bodyFragment, model);
    assertNull(doc.getByTag("div"));
    assertNull(doc.getByTag("p"));
  }

  public void testExcludeOperatorOnFalse()
  {
    String bodyFragment = "" + //
        "<div data-exclude='false'>" + //
        "   <p data-text='fruit'></p>" + //
        "</div>";
    Pojo model = new Pojo();
    model.fruit = Fruit.APPLE;

    Document doc = run(bodyFragment, model);
    assertNotNull(doc.getByTag("div"));
    assertNotNull(doc.getByTag("p"));
  }

  public void testGotoOperator()
  {
    String html = "" + //
        "<div data-goto='section-one-id'>" + //
        "   <div>" + //
        "       <h1 data-text='fruit'></h1>" + //
        "   </div>" + //
        "   <div>" + //
        "       <section id='section-one-id'>" + //
        "           <h2 data-text='fruit'></h2>" + //
        "       </section>" + //
        "       <section>" + //
        "           <h3 data-text='fruit'></h3>" + //
        "       </section>" + //
        "   </div>" + //
        "</div>";
    Pojo model = new Pojo();
    model.fruit = Fruit.APPLE;

    DocumentBuilder builder = getBuilder();
    Document doc = builder.parseHTML(html);
    XhtmlTemplate template = new XhtmlTemplate(doc);

    doc = builder.parseHTML(template.serialize(model));

    assertEquals("APPLE", doc.getByTag("h1").getText());
    assertEquals("APPLE", doc.getByTag("h2").getText());
    assertEquals("APPLE", doc.getByTag("h3").getText());
  }

  // ------------------------------------------------------
  // fixture initialization and helpers

  @SuppressWarnings("unused")
  private static class Pojo
  {
    boolean flag;
    Fruit fruit;
  }

  private static enum Fruit
  {
    NONE, APPLE, ORANGE, BANANA
  }
}
