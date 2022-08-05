package com.jslib.template.xhtml;

import java.io.IOException;
import java.util.Stack;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;

/**
 * Set element text content accordingly numbering format and item index. It is usable inside elements with ordered lists and
 * maps operators which are responsible for index creation and increment; this operators deals only with formatting. Trying to
 * use it outside index scope will rise exception.
 * 
 * <pre>
 *  &lt;ul data-olist="."&gt;
 *      &lt;li data-numbering="D.2.%s)"&gt&lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * After template execution <em>li</em> elements text content will be: D.2.a) D.2.b) ... D.2.i). Operand is an format describing
 * numbering format with next syntax:
 * 
 * <pre>
 *  numberingFormat := character* (indexFormat character*)+
 *  character := any less "%"
 *  indexFormat := "%" formatCode
 *  formatCode := "s" | "S" | "i" | "I" | "n"
 * </pre>
 * 
 * As observe, syntax allows for multiple index formating usable for nested list. Below sample will expand in a series like:
 * A.1, A.2 ... , B.1, B.2 ... .
 * 
 * <pre>
 *  &lt;section data-olist="outer"&gt;
 *      &lt;ul data-olist="inner"&gt;
 *          &lt;li data-numbering="%S.%n"&gt;&lt;/li&gt;
 *      &lt;/ul&gt;
 *  &lt;/section&gt;
 * </pre>
 * 
 * Because only ordered list have index, mixing order and unordered list is supported, although not really a use case.
 * 
 * <pre>
 *  &lt;ul data-olist="."&gt;
 *      &lt;li&gt;
 *          &lt;h1 data-numbering="%I"&gt;&lt;/h1&gt;
 *          &lt;ul data-list="."&gt; // unordered list between two with numbering
 *              &lt;li&gt;
 *                  &lt;h2&gt;&lt;/h2&gt;
 *                  &lt;ul data-olist="."&gt;
 *                      &lt;li&gt;
 *                          &lt;h3 data-numbering="%I.%S"&gt;&lt;/h3&gt;
 *                      &lt;/li&gt;
 *                  &lt;/ul&gt;
 *              &lt;/li&gt;
 *          &lt;/ul&gt;
 *      &lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * Because of mixed unordered list, h3 elements will have a series like: I.A, I.B, I.A, I.B, II.A, II.B, II.A, II.B ... .
 * 
 * @author Iulian Rotaru
 */
final class NumberingOperator extends Operator
{
  /**
   * Operator serializer.
   */
  private Serializer serializer;

  /**
   * Construct numbering operator instance.
   * 
   * @param serializer parent serializer.
   */
  NumberingOperator(Serializer serializer)
  {
    this.serializer = serializer;
  }

  /**
   * Insert formatted numbering as element text content. If serializer indexes stack is empty throws templates exception;
   * anyway, validation tool running on build catches numbering operator without surrounding ordered list.
   * 
   * @param element element declaring numbering operator,
   * @param scope scope object,
   * @param format numbering format, see class description for syntax,
   * @param arguments optional arguments, not used.
   * @return always returns null to signal full processing.
   * @throws TemplateException if serializer indexes stack is empty.
   * @throws IOException if underlying writer fails to write.
   */
  @Override
  protected Object doExec(Element element, Object scope, String format, Object... arguments) throws IOException
  {
    Stack<Index> indexes = this.serializer.getIndexes();
    if(indexes.size() == 0) {
      throw new TemplateException("Required ordered collection index is missing. Numbering operator cancel execution.");
    }
    this.serializer.writeTextContent(getNumbering(this.serializer.getIndexes(), format));
    return null;
  }

  /**
   * Parse numbering format and inject index values. First argument,the stack of indexes, is global per serializer and format is
   * the operand literal value. For nested numbering, format may contain more than one single format code; this is the reason
   * first argument is the entire indexes stack, not only current index. Given a stack with four indexes those values are 1, 2,
   * 3 and 4 and "%S - %I.%n-%s)" the format, resulting formatted string is "A - II.3-d)".
   * 
   * @param format numbering format.
   * @return formatted numbering.
   */
  private static String getNumbering(Stack<Index> indexes, String format)
  {
    StringBuilder sb = new StringBuilder();
    int i = format.length();
    int j = i;
    int indexPosition = indexes.size() - 1;
    for(;;) {
      i = format.lastIndexOf('%', i);
      if(i == -1 && j > 0) {
        sb.insert(0, format.substring(0, j));
        break;
      }
      // if(i > 0 && format.charAt(i - 1) == '%') continue;
      if(i + 2 < format.length()) sb.insert(0, format.substring(i + 2, j));
      if(i + 1 == format.length()) continue;

      NumberingFormat numberingFormat = getNumberingFormat(format.charAt(i + 1));
      sb.insert(0, numberingFormat.format(indexes.get(indexPosition--).value));
      if(i == 0) break;
      j = i;
      i--;
    }
    return sb.toString();
  }

  /**
   * Factory method for numbering format implementations. If format code is not recognized throws templates exception; anyway
   * validation tool catches this condition. See {@link NumberingFormat} for the list of valid format codes.
   * 
   * @param formatCode single char format code.
   * @return requested numbering format instance.
   * @throws TemplateException if format code is not recognized.
   */
  public static NumberingFormat getNumberingFormat(char formatCode)
  {
    switch(formatCode) {
    case 'n':
      return new ArabicNumeralNumbering();
    case 's':
      return new LowerCaseStringNumbering();
    case 'S':
      return new UpperCaseStringNumbering();
    case 'i':
      return new LowerCaseRomanNumbering();
    case 'I':
      return new UpperCaseRomanNumbering();
    }
    throw new TemplateException("Invalid numbering format code |%s|.", formatCode);
  }
}
