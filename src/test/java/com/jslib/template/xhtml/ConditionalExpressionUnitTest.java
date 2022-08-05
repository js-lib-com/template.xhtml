package com.jslib.template.xhtml;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.jslib.api.template.TemplateException;
import com.jslib.lang.InvocationException;
import com.jslib.util.Classes;

public class ConditionalExpressionUnitTest extends TestCaseEx
{
  private Data data;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    data = new Data();
  }

  public void testPositiveNotEmpty() throws Throwable
  {
    exercise("booleanValue", true);
    exercise("byteValue", true);
    exercise("shortValue", true);
    exercise("intValue", true);
    exercise("longValue", true);
    exercise("floatValue", true);
    exercise("doubleValue", true);
    exercise("date", true);
    exercise("string", true);
    exercise("state", true);
    exercise("array.0", true);
  }

  public void testNegativeNotEmpty() throws Throwable
  {
    exercise("emptyString", false);
    exercise("emptyList", false);
  }

  public void testPositiveEmpty() throws Throwable
  {
    exercise("!emptyString", true);
    exercise("!emptyList", true);
  }

  public void testNegativeEmpty() throws Throwable
  {
    exercise("!booleanValue", false);
    exercise("!byteValue", false);
    exercise("!shortValue", false);
    exercise("!intValue", false);
    exercise("!longValue", false);
    exercise("!floatValue", false);
    exercise("!doubleValue", false);
    exercise("!date", false);
  }

  public void testPositiveEqualsBoolean() throws Throwable
  {
    exercise("booleanValue=true", true);
  }

  public void testNegativeEqualsBoolean() throws Throwable
  {
    exercise("booleanValue=false", false);
  }

  public void testPositiveEqualsString() throws Throwable
  {
    exercise("string=String value.", true);
  }

  public void testNegativeEqualsString() throws Throwable
  {
    exercise("string=String value;", false);
  }

  public void testPositiveNotEqualsString() throws Throwable
  {
    exercise("!string=String value;", true);
  }

  public void testNegativeNotEqualsString() throws Throwable
  {
    exercise("!string=String value.", false);
  }

  public void testPositiveEqualsEnum() throws Throwable
  {
    exercise("state=ACTIVE", true);
  }

  public void testNegativeEqualsEnum() throws Throwable
  {
    exercise("state=DISABLED", false);
  }

  public void testPositiveNotEqualsEnum() throws Throwable
  {
    exercise("!state=DISABLED", true);
  }

  public void testNegativeNotEqualsEnum() throws Throwable
  {
    exercise("!state=ACTIVE", false);
  }

  public void testPositiveEqualsNumber() throws Throwable
  {
    exercise("byteValue=19", true);
    exercise("shortValue=1964", true);
    exercise("intValue=19640315", true);
    exercise("longValue=1964031514", true);
    exercise("floatValue=1.23", true);
    exercise("doubleValue=3.14", true);
  }

  public void testNegativeEqualsNumber() throws Throwable
  {
    data.byteValue = 18;
    data.shortValue = 1963;
    data.intValue = 19640314;
    data.longValue = 1964031513;
    data.floatValue = 1.22F;
    data.doubleValue = 3.13;

    exercise("byteValue=19", false);
    exercise("shortValue=1964", false);
    exercise("intValue=19640315", false);
    exercise("longValue=1964031514", false);
    exercise("floatValue=1.23", false);
    exercise("doubleValue=3.14", false);
  }

  public void testPositiveNotEqualsNumber() throws Throwable
  {
    exercise("!byteValue=18", true);
    exercise("!shortValue=1963", true);
    exercise("!intValue=19640314", true);
    exercise("!longValue=1964031513", true);
    exercise("!floatValue=1.22", true);
    exercise("!doubleValue=3.13", true);
  }

  public void testPositiveLessThanNumber() throws Throwable
  {
    exercise("byteValue<20", true);
    exercise("shortValue<1965", true);
    exercise("intValue<19640316", true);
    exercise("longValue<1964031515", true);
    exercise("floatValue<1.24", true);
    exercise("doubleValue<3.15", true);
  }

  public void testNegativeLessThanNumber() throws Throwable
  {
    exercise("byteValue<19", false);
    exercise("shortValue<1964", false);
    exercise("intValue<19640315", false);
    exercise("longValue<1964031514", false);
    exercise("floatValue<1.23", false);
    exercise("doubleValue<3.14", false);
  }

  public void testPositiveGreaterThanNumber() throws Throwable
  {
    exercise("byteValue>18", true);
    exercise("shortValue>1963", true);
    exercise("intValue>19640314", true);
    exercise("longValue>1964031513", true);
    exercise("floatValue>1.22", true);
    exercise("doubleValue>3.13", true);
  }

  public void testNegativeGreaterThanNumber() throws Throwable
  {
    exercise("byteValue>19", false);
    exercise("shortValue>1964", false);
    exercise("intValue>19640315", false);
    exercise("longValue>1964031514", false);
    exercise("floatValue>1.23", false);
    exercise("doubleValue>3.14", false);
  }

  public void testPositiveEqualsDate() throws Throwable
  {
    exercise("date=1964-03-15T13:40:00Z", true);

    exercise("date=1964", true);
    exercise("date=1964-03", true);
    exercise("date=1964-03-15", true);
    exercise("date=1964-03-15T13", true);
    exercise("date=1964-03-15T13:40", true);
    exercise("date=1964-03-15T13:40:00", true);
  }

  public void testPositiveLessThanDate() throws Throwable
  {
    exercise("date<1964-03-15T13:40:01Z", true);

    exercise("date<1965", true);
    exercise("date<1964-04", true);
    exercise("date<1964-03-16", true);
    exercise("date<1964-03-15T14", true);
    exercise("date<1964-03-15T13:41", true);
    exercise("date<1964-03-15T13:40:01", true);
  }

  public void testPositiveGreaterThanDate() throws Throwable
  {
    exercise("date>1964-03-15T13:39:59Z", true);

    exercise("date>1963", true);
    exercise("date>1964-02", true);
    exercise("date>1964-03-14", true);
    exercise("date>1964-03-15T12", true);
    exercise("date>1964-03-15T13:39", true);
    exercise("date>1964-03-15T13:39:59", true);
  }

  public void testInvalidLessThanString() throws Throwable
  {
    try {
      exercise("string<some value", true);
    }
    catch(InvocationException e) {
      assertTrue(e.getCause() instanceof TemplateException);
      return;
    }
    fail("Using string with LESS_THAN operator should rise templates exception.");
  }

  public void testInvalidGreaterThanString() throws Throwable
  {
    try {
      exercise("string>some value", true);
    }
    catch(InvocationException e) {
      assertTrue(e.getCause() instanceof TemplateException);
      return;
    }
    fail("Using string with GREATER_THAN operator should rise templates exception.");
  }

  public void testInvalidEqualsNoOperand() throws Throwable
  {
    try {
      exercise("intValue=", true);
    }
    catch(InvocationException e) {
      assertTrue(e.getCause() instanceof TemplateException);
      return;
    }
    fail("Missing operand from EQUALS operator should rise templates exception.");
  }

  public void testBadDateFormat() throws Throwable
  {
    try {
      exercise("date=1964-03-15 14:30:00", true);
    }
    catch(InvocationException e) {
      assertTrue(e.getCause() instanceof TemplateException);
      return;
    }
    fail("Invalid date format should rise templates exception.");
  }

  public void testBadBooleanFormat() throws Throwable
  {
    try {
      exercise("booleanValue=yes", true);
    }
    catch(InvocationException e) {
      assertTrue(e.getCause() instanceof TemplateException);
      return;
    }
    fail("Invlaid boolean format should rise templates exception.");
  }

  public void testInvalidOperator() throws Throwable
  {
    try {
      exercise("string;value", true);
    }
    catch(InvocationException e) {
      assertTrue(e.getCause() instanceof TemplateException);
      return;
    }
    fail("Invlaid opcode should rise templates exception.");
  }

  public void testMissingOperand() throws Throwable
  {
    try {
      exercise("string=", true);
    }
    catch(InvocationException e) {
      assertTrue(e.getCause() instanceof TemplateException);
      return;
    }
    fail("Invlaid opcode should rise templates exception.");
  }

  private void exercise(String expression, boolean expected) throws Throwable
  {
    Content content = new Content(data);
    Object conditionalExpression = Classes.newInstance("com.jslib.template.xhtml.ConditionalExpression", content, data, expression);
    assertEquals(expected, (boolean)Classes.invoke(conditionalExpression, "value"));
  }

  private static enum State
  {
    NONE, ACTIVE, DISABLED
  }

  @SuppressWarnings("unused")
  private class Data
  {
    boolean booleanValue = true;
    byte byteValue = 19;
    short shortValue = 1964;
    int intValue = 19640315;
    long longValue = 1964031514;
    float floatValue = 1.23F;
    double doubleValue = 3.14;
    String string = "String value.";
    State state = State.ACTIVE;
    Date date;
    Date year;
    Date month;
    Date day;
    Date hour;
    Date minute;
    String emptyString;
    List<String> emptyList;
    String[] array = new String[2];

    private DateFormat df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss Z");

    public Data() throws ParseException
    {
      date = df.parse("1964-03-15 13:40:00 UTC");

      Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      calendar.setTime(date);

      calendar.set(Calendar.SECOND, 0);
      minute = calendar.getTime();

      calendar.set(Calendar.MINUTE, 0);
      hour = calendar.getTime();

      calendar.set(Calendar.HOUR_OF_DAY, 0);
      day = calendar.getTime();

      calendar.set(Calendar.DAY_OF_MONTH, 0);
      month = calendar.getTime();

      calendar.set(Calendar.MONTH, 0);
      year = calendar.getTime();

      array[0] = "zero";
      array[1] = "one";
    }
  }
}
