package js.template.xhtml;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import js.dom.Document;
import js.dom.EList;
import js.format.Format;
import js.template.TemplateException;
import js.util.Classes;

public class FormattingOperatorUnitTest extends TestCaseEx
{
  public void testArabicNumeralNumberinc() throws Throwable
  {
    String className = "js.template.xhtml.ArabicNumeralNumbering";
    String expectedValues = "1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20";
    runNumberingFormatTest(className, expectedValues);
  }

  public void testLowerCaseRomanNumbering() throws Throwable
  {
    String className = "js.template.xhtml.LowerCaseRomanNumbering";
    String expectedValues = "i ii iii iv v vi vii viii ix x xi xii xiii xiv xv xvi xvii xviii xix xx xxi xxii xxiii xxiv xxv xxvi xxvii xxviii xxix xxx";
    runNumberingFormatTest(className, expectedValues);

    String[] indexes = "49 50 51 99 100 101 399 400 401 499 500 501 899 900 901 999 1000 1001".split(" ");
    String[] values = "xlix l li xcix c ci cccxcix cd cdi cdxcix d di dcccxcix cm cmi cmxcix m mi".split(" ");
    Object indexFormat = Classes.newInstance(className);
    for(int i = 0; i < indexes.length; ++i) {
      assertEquals(values[i], Classes.invoke(indexFormat, "format", Integer.parseInt(indexes[i])));
    }
  }

  public void testUpperCaseRomanNumbering() throws Throwable
  {
    String className = "js.template.xhtml.UpperCaseRomanNumbering";
    String expectedValues = "I II III IV V VI VII VIII IX X XI XII XIII XIV XV XVI XVII XVIII XIX XX XXI XXII XXIII XXIV XXV XXVI XXVII XXVIII XXIX XXX";
    runNumberingFormatTest(className, expectedValues);

    String[] indexes = "49 50 51 99 100 101 399 400 401 499 500 501 899 900 901 999 1000 1001".split(" ");
    String[] values = "XLIX L LI XCIX C CI CCCXCIX CD CDI CDXCIX D DI DCCCXCIX CM CMI CMXCIX M MI".split(" ");
    Object indexFormat = Classes.newInstance(className);
    for(int i = 0; i < indexes.length; ++i) {
      assertEquals(values[i], Classes.invoke(indexFormat, "format", Integer.parseInt(indexes[i])));
    }
  }

  public void testLowerCaseStringNumbering() throws Throwable
  {
    String className = "js.template.xhtml.LowerCaseStringNumbering";
    String superClassName = "js.template.xhtml.UpperCaseStringNumbering";
    String expectedValues = "a b c d e f g h i j k l m n o p q r s t u v w x y z aa bb cc dd ee ff gg hh ii jj kk ll mm nn oo pp qq rr ss tt uu vv ww xx yy zz aaa bbb";
    runNumberingFormatTest(className, expectedValues, superClassName);
  }

  public void testUpperCaseStringNumbering() throws Throwable
  {
    String className = "js.template.xhtml.UpperCaseStringNumbering";
    String expectedValues = "A B C D E F G H I J K L M N O P Q R S T U V W X Y Z AA BB CC DD EE FF GG HH II JJ KK LL MM NN OO PP QQ RR SS TT UU VV WW XX YY ZZ AAA BBB";
    runNumberingFormatTest(className, expectedValues);
  }

  private void runNumberingFormatTest(String className, String expectedValues, String... optSuperClassName) throws Throwable
  {
    Object indexFormat = Classes.newInstance(className);
    int index = 1;
    if(optSuperClassName.length > 0) {
      Class<?> superClass = Class.forName(optSuperClassName[0]);
      for(String expected : expectedValues.split(" ")) {
        assertEquals(expected, Classes.invoke(indexFormat, superClass, "format", index++));
      }
    }
    else {
      for(String expected : expectedValues.split(" ")) {
        assertEquals(expected, Classes.invoke(indexFormat, "format", index++));
      }
    }
  }

  public void testListNumberingWithArabicNumeral()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li>" + //
        "       <h1 data-numbering='%n'></h1>" + //
        "       <h2 data-text='.'></h2>" + //
        "   </li>" + //
        "</ul>";
    List<String> model = new ArrayList<String>();
    model.add("item1");
    model.add("item2");
    model.add("item3");
    Document doc = run(html, model);
    EList elist = doc.findByTag("h1");
    assertEquals(3, elist.size());
    assertEquals("1", elist, 0);
    assertEquals("2", elist, 1);
    assertEquals("3", elist, 2);
  }

  public void testMapNumberingWithArabicNumeral()
  {
    String html = "" + //
        "<dl data-omap='.'>" + //
        "   <dt><span data-numbering='%n.'></span> <span data-text='.'></span></dt>" + //
        "   <dd></dd>" + //
        "</dl>";
    Map<String, String> model = new HashMap<String, String>();
    model.put("key0", "value0");
    model.put("key1", "value1");
    model.put("key2", "value2");
    Document doc = run(html, model);
    EList elist = doc.findByXPath("//DT/SPAN[position()=1]");
    assertEquals(3, elist.size());
    assertEquals("1.", elist, 0);
    assertEquals("2.", elist, 1);
    assertEquals("3.", elist, 2);
  }

  public void testListNumberingWithLowerCaseRoman()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li>" + //
        "       <h1 data-numbering='%i'></h1>" + //
        "       <h2 data-text='.'></h2>" + //
        "   </li>" + //
        "</ul>";
    List<String> model = new ArrayList<String>();
    model.add("item1");
    model.add("item2");
    model.add("item3");
    Document doc = run(html, model);
    EList elist = doc.findByTag("h1");
    assertEquals(3, elist.size());
    assertEquals("i", elist, 0);
    assertEquals("ii", elist, 1);
    assertEquals("iii", elist, 2);
  }

  public void testMapNumberingWithLowerCaseRoman()
  {
    String html = "" + //
        "<dl data-omap='.'>" + //
        "   <dt><span data-numbering='%i'></span> <span data-text='.'></span></dt>" + //
        "   <dd></dd>" + //
        "</dl>";
    Map<String, String> model = new HashMap<String, String>();
    model.put("key0", "value0");
    model.put("key1", "value1");
    model.put("key2", "value2");
    Document doc = run(html, model);
    EList elist = doc.findByXPath("//DT/SPAN[position()=1]");
    assertEquals(3, elist.size());
    assertEquals("i", elist, 0);
    assertEquals("ii", elist, 1);
    assertEquals("iii", elist, 2);
  }

  public void testListNumberingWithUpperCaseRoman()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li>" + //
        "       <h1 data-numbering='%I'></h1>" + //
        "       <h2 data-text='.'></h2>" + //
        "   </li>" + //
        "</ul>";
    List<String> model = new ArrayList<String>();
    model.add("item1");
    model.add("item2");
    model.add("item3");
    Document doc = run(html, model);
    EList elist = doc.findByTag("h1");
    assertEquals(3, elist.size());
    assertEquals("I", elist, 0);
    assertEquals("II", elist, 1);
    assertEquals("III", elist, 2);
  }

  public void testMapNumberingWithUpperCaseRoman()
  {
    String html = "" + //
        "<dl data-omap='.'>" + //
        "   <dt><span data-numbering='%I'></span> <span data-text='.'></span></dt>" + //
        "   <dd></dd>" + //
        "</dl>";
    Map<String, String> model = new HashMap<String, String>();
    model.put("key0", "value0");
    model.put("key1", "value1");
    model.put("key2", "value2");
    Document doc = run(html, model);
    EList elist = doc.findByXPath("//DT/SPAN[position()=1]");
    assertEquals(3, elist.size());
    assertEquals("I", elist, 0);
    assertEquals("II", elist, 1);
    assertEquals("III", elist, 2);
  }

  public void testListNumberingWithLowerCaseString()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li>" + //
        "       <h1 data-numbering='%s'></h1>" + //
        "       <h2 data-text='.'></h2>" + //
        "   </li>" + //
        "</ul>";
    List<String> model = new ArrayList<String>();
    model.add("item1");
    model.add("item2");
    model.add("item3");
    Document doc = run(html, model);
    EList elist = doc.findByTag("h1");
    assertEquals(3, elist.size());
    assertEquals("a", elist, 0);
    assertEquals("b", elist, 1);
    assertEquals("c", elist, 2);
  }

  public void testMapNumberingWithLowerCaseString()
  {
    String html = "" + //
        "<dl data-omap='.'>" + //
        "   <dt><span data-numbering='%s'></span> <span data-text='.'></span></dt>" + //
        "   <dd></dd>" + //
        "</dl>";
    Map<String, String> model = new HashMap<String, String>();
    model.put("key0", "value0");
    model.put("key1", "value1");
    model.put("key2", "value2");
    Document doc = run(html, model);
    EList elist = doc.findByXPath("//DT/SPAN[position()=1]");
    assertEquals(3, elist.size());
    assertEquals("a", elist, 0);
    assertEquals("b", elist, 1);
    assertEquals("c", elist, 2);
  }

  public void testListNumberingWithUpperCaseString()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li>" + //
        "       <h1 data-numbering='%S'></h1>" + //
        "       <h2 data-text='.'></h2>" + //
        "   </li>" + //
        "</ul>";
    List<String> model = new ArrayList<String>();
    model.add("item1");
    model.add("item2");
    model.add("item3");
    Document doc = run(html, model);
    EList elist = doc.findByTag("h1");
    assertEquals(3, elist.size());
    assertEquals("A", elist, 0);
    assertEquals("B", elist, 1);
    assertEquals("C", elist, 2);
  }

  public void testMapNumberingWithUpperCaseString()
  {
    String html = "" + //
        "<dl data-omap='.'>" + //
        "   <dt><span data-numbering='%S'></span> <span data-text='.'></span></dt>" + //
        "   <dd></dd>" + //
        "</dl>";
    Map<String, String> model = new HashMap<String, String>();
    model.put("key0", "value0");
    model.put("key1", "value1");
    model.put("key2", "value2");
    Document doc = run(html, model);
    EList elist = doc.findByXPath("//DT/SPAN[position()=1]");
    assertEquals(3, elist.size());
    assertEquals("A", elist, 0);
    assertEquals("B", elist, 1);
    assertEquals("C", elist, 2);
  }

  public void testThreeLevelsNestedNumbering()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li>" + //
        "       <h1 data-numbering='%I'></h1>" + //
        "       <ul data-olist='.'>" + //
        "           <li>" + //
        "               <h2 data-numbering='%I.%S'></h2>" + //
        "               <ul data-olist='.'>" + //
        "                   <li>" + //
        "                       <h3 data-numbering='%I.%S.%n'></h3>" + //
        "                   </li>" + //
        "               </ul>" + //
        "           </li>" + //
        "       </ul>" + //
        "   </li>" + //
        "</ul>";
    List<List<List<Integer>>> model = new ArrayList<List<List<Integer>>>();

    List<Integer> nephew1 = new ArrayList<Integer>();
    nephew1.add(0);
    nephew1.add(1);

    List<Integer> nephew2 = new ArrayList<Integer>();
    nephew2.add(2);
    nephew2.add(3);

    List<Integer> nephew3 = new ArrayList<Integer>();
    nephew3.add(4);
    nephew3.add(5);

    List<Integer> nephew4 = new ArrayList<Integer>();
    nephew4.add(6);
    nephew4.add(7);

    List<List<Integer>> child1 = new ArrayList<List<Integer>>();
    child1.add(nephew1);
    child1.add(nephew2);

    List<List<Integer>> child2 = new ArrayList<List<Integer>>();
    child2.add(nephew3);
    child2.add(nephew4);

    model.add(child1);
    model.add(child2);

    Document doc = run(html, model);

    EList elist = doc.findByXPath("//H1");
    assertEquals(2, elist.size());
    assertEquals("I", elist, 0);
    assertEquals("II", elist, 1);
    elist = doc.findByXPath("//H2");
    assertEquals(4, elist.size());
    assertEquals("I.A", elist, 0);
    assertEquals("I.B", elist, 1);
    assertEquals("II.A", elist, 2);
    assertEquals("II.B", elist, 3);
    elist = doc.findByXPath("//H3");
    assertEquals(8, elist.size());
    assertEquals("I.A.1", elist, 0);
    assertEquals("I.A.2", elist, 1);
    assertEquals("I.B.1", elist, 2);
    assertEquals("I.B.2", elist, 3);
    assertEquals("II.A.1", elist, 4);
    assertEquals("II.A.2", elist, 5);
    assertEquals("II.B.1", elist, 6);
    assertEquals("II.B.2", elist, 7);
  }

  public void testUnorderListBetweenOrdered()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li>" + //
        "       <h1 data-numbering='%I'></h1>" + //
        "       <ul data-list='.'>" + //
        "           <li>" + //
        "               <h2></h2>" + //
        "               <ul data-olist='.'>" + //
        "                   <li>" + //
        "                       <h3 data-numbering='%I.%S'></h3>" + //
        "                   </li>" + //
        "               </ul>" + //
        "           </li>" + //
        "       </ul>" + //
        "   </li>" + //
        "</ul>";
    List<List<List<Integer>>> model = new ArrayList<List<List<Integer>>>();

    List<Integer> nephew1 = new ArrayList<Integer>();
    nephew1.add(0);
    nephew1.add(1);

    List<Integer> nephew2 = new ArrayList<Integer>();
    nephew2.add(2);
    nephew2.add(3);

    List<Integer> nephew3 = new ArrayList<Integer>();
    nephew3.add(4);
    nephew3.add(5);

    List<Integer> nephew4 = new ArrayList<Integer>();
    nephew4.add(6);
    nephew4.add(7);

    List<List<Integer>> child1 = new ArrayList<List<Integer>>();
    child1.add(nephew1);
    child1.add(nephew2);

    List<List<Integer>> child2 = new ArrayList<List<Integer>>();
    child2.add(nephew3);
    child2.add(nephew4);

    model.add(child1);
    model.add(child2);

    Document doc = run(html, model);

    EList elist = doc.findByXPath("//H1");
    assertEquals(2, elist.size());
    assertEquals("I", elist, 0);
    assertEquals("II", elist, 1);
    elist = doc.findByXPath("//H2");
    assertEquals(4, elist.size());
    elist = doc.findByXPath("//H3");
    assertEquals(8, elist.size());
    assertEquals("I.A", elist, 0);
    assertEquals("I.B", elist, 1);
    assertEquals("I.A", elist, 2);
    assertEquals("I.B", elist, 3);
    assertEquals("II.A", elist, 4);
    assertEquals("II.B", elist, 5);
    assertEquals("II.A", elist, 6);
    assertEquals("II.B", elist, 7);
  }

  public void testComplexNumbering()
  {
    String html = "" + //
        "<h1 data-value='title'></h1>" + //
        "<table>" + //
        "    <tbody data-olist='list'>" + //
        "        <tr>" + //
        "            <td>" + //
        "                <h2 data-numbering='D.2.%s)'></h2>" + //
        "                <p data-text='title'></p>" + //
        "                <ul data-olist='list'>" + //
        "                    <li>" + //
        "                        <h3 data-numbering='%S.%I'></h3>" + //
        "                        <p data-text='title'></p>" + //
        "                        <h4 data-numbering='%n'></h4>" + //
        "                    </li>" + //
        "                </ul>" + //
        "            </td>" + //
        "        </tr>" + //
        "    </tbody>" + //
        "</table>";
    NestedLists model = new NestedLists();
    model.title = "title";
    model.list.add(new NestedLists.Nested("title0"));
    model.list.add(new NestedLists.Nested("title1"));
    model.list.get(0).list.add(new Pojo("title00"));
    model.list.get(0).list.add(new Pojo("title01"));
    model.list.get(1).list.add(new Pojo("title10"));
    model.list.get(1).list.add(new Pojo("title11"));
    model.list.get(1).list.add(new Pojo("title12"));
    model.list.get(1).list.add(new Pojo("title13"));

    Document doc = run(html, model);

    EList elist = doc.findByTag("h2");
    assertEquals(2, elist.size());
    assertEquals("D.2.a)", elist, 0);
    assertEquals("D.2.b)", elist, 1);

    elist = doc.findByTag("h3");
    assertEquals(6, elist.size());
    assertEquals("A.I", elist, 0);
    assertEquals("A.II", elist, 1);
    assertEquals("B.I", elist, 2);
    assertEquals("B.II", elist, 3);
    assertEquals("B.III", elist, 4);
    assertEquals("B.IV", elist, 5);

    elist = doc.findByTag("h4");
    assertEquals(6, elist.size());
    assertEquals("1", elist, 0);
    assertEquals("2", elist, 1);
    assertEquals("1", elist, 2);
    assertEquals("2", elist, 3);
    assertEquals("3", elist, 4);
    assertEquals("4", elist, 5);
  }

  public void testTextFormat()
  {
    String html = "<div data-text='title' data-format='js.template.xhtml.FormattingOperatorUnitTest$UpperDecorator'></div>";
    Pojo model = new Pojo("John Doe");
    Document doc = run(html, model);
    assertEquals("JOHN DOE", doc.getByTag("div").getText());
  }

  public void testValueFormat()
  {
    String html = "<input data-value='title' data-format='js.template.xhtml.FormattingOperatorUnitTest$UpperDecorator' />";
    Pojo model = new Pojo("John Doe");
    Document doc = run(html, model);
    assertEquals("JOHN DOE", doc.getByTag("input").getAttr("value"));
  }

  public void testListItemFormat()
  {
    String html = "" + //
        "<ul data-list='.'>" + //
        "    <li data-format='js.template.xhtml.FormattingOperatorUnitTest$UpperDecorator'></li>" + //
        "</ul>";
    List<String> model = new ArrayList<String>();
    model.add("John Doe");
    model.add("Maximus Decimus Meridius");
    Document doc = run(html, model);
    EList elist = doc.findByTag("li");
    assertEquals(2, elist.size());
    assertEquals("JOHN DOE", elist, 0);
    assertEquals("MAXIMUS DECIMUS MERIDIUS", elist, 1);
  }

  public void testMapValueFormat()
  {
    String html = "" + //
        "<dl data-map='.'>" + //
        "   <dd></dd>" + //
        "   <dt data-format='js.template.xhtml.FormattingOperatorUnitTest$UpperDecorator'></dt>" + //
        "</dl>";
    Map<String, String> model = new TreeMap<String, String>();
    model.put("key0", "John Doe");
    model.put("key1", "Maximus Decimus Meridius");
    Document doc = run(html, model);
    EList elist = doc.findByTag("dt");
    assertEquals(2, elist.size());
    assertEquals("JOHN DOE", elist, 0);
    assertEquals("MAXIMUS DECIMUS MERIDIUS", elist, 1);
  }

  public void testFormatClassNotFound()
  {
    String html = "<input data-value='title' data-format='js.template.test.FakeFormat' />";
    try {
      run(html, new Pojo("John Doe"));
    }
    catch(TemplateException e) {
      assertTrue("Unexpected error message", e.getMessage().contains("not found"));
      return;
    }
    fail("Formatting class not found should rise templates exception.");
  }

  public void testNumberingInsideUnorderedList()
  {
    String html = "" + //
        "<ul data-list='.'>" + //
        "   <li data-numbering='%n'></li>" + //
        "</ul>";
    int[] model = new int[]
    {
        1, 2, 3, 4
    };
    try {
      run(html, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Numbering inside unordered list should rise templates exception.");
  }

  public void testNumberingOutsideList()
  {
    String html = "" + //
        "<ul>" + //
        "   <li data-numbering='%n'></li>" + //
        "</ul>";
    int[] model = new int[] {};
    try {
      run(html, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Numbering outside list should rise templates exception.");
  }

  public void testNumberingWithoutFormatCode()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li data-numbering='abc'></li>" + //
        "</ul>";
    int[] model = new int[]
    {
        1, 2
    };
    Document doc = run(html, model);

    EList elist = doc.findByTag("li");
    assertEquals(2, elist.size());
    assertEquals("abc", elist, 0);
    assertEquals("abc", elist, 1);
  }

  public void testInvalidNumberingFormatCode()
  {
    String html = "" + //
        "<ul data-olist='.'>" + //
        "   <li data-numbering='%Q'></li>" + //
        "</ul>";
    int[] model = new int[]
    {
        1, 2, 3, 4
    };
    try {
      run(html, model);
    }
    catch(TemplateException e) {
      return;
    }
    fail("Invalid numbering format code should rise templates exception.");
  }

  // ------------------------------------------------------
  // fixture initialization and helpers

  @SuppressWarnings("unused")
  private static class FormatObject
  {
    String title;
    List<String> names = new ArrayList<String>();
  }

  @SuppressWarnings("unused")
  private static class UpperDecorator implements Format
  {
    @Override
    public String format(Object object)
    {
      return ((String)object).toUpperCase();
    }

    @Override
    public Object parse(String value) throws ParseException
    {
      throw new UnsupportedOperationException();
    }
  }

  @SuppressWarnings("unused")
  private static class Pojo
  {
    String title;

    Pojo(String title)
    {
      this.title = title;
    }
  }

  @SuppressWarnings("unused")
  private static class NestedLists
  {
    String title;
    List<Nested> list = new ArrayList<Nested>();

    static class Nested
    {
      String title;
      List<Pojo> list = new ArrayList<Pojo>();

      Nested(String title)
      {
        this.title = title;
      }
    }
  }
}
