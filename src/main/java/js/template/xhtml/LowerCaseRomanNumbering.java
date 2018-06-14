package js.template.xhtml;

/**
 * Roman numeric index, lower case. Lower case variant of {@link UpperCaseRomanNumbering}. Its format code is <b>i</b>.
 * 
 * <pre>
 *  &lt;ul data-olist="."&gt;
 *      &lt;li data-numbering="%i)"&gt;&lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * After templates rendering <em>li</em> elements text content will be i), ii) ... . See {@link NumberingOperator} for details
 * about numbering format syntax.
 * 
 * @author Iulian Rotaru
 */
final class LowerCaseRomanNumbering extends UpperCaseRomanNumbering
{
  /**
   * Format index as lower case Roman numeral.
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