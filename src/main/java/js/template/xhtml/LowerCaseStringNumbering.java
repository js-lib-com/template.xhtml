package js.template.xhtml;

/**
 * Lower case string list index. Lower case variant of {@link UpperCaseStringNumbering}. Its format code is <b>s</b>.
 * 
 * <pre>
 *  &lt;ul data-olist="."&gt;
 *      &lt;li data-numbering="%s)"&gt;&lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * After templates rendering <em>li</em> elements text content will be a), b) ... . See {@link NumberingOperator} for details
 * about numbering format syntax.
 * 
 * @author Iulian Rotaru
 */
final class LowerCaseStringNumbering extends UpperCaseStringNumbering
{
  /**
   * Format index as lower case string.
   * 
   * @param index index value.
   * @return formatted index.
   */
  @Override
  public String format(int index)
  {
    return super.format(index).toLowerCase();
  }
}