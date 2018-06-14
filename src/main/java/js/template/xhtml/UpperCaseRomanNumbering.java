package js.template.xhtml;

/**
 * Upper case Roman number index. Transform index into its Roman number representation. Its format code is <b>I</b>.
 * 
 * <pre>
 *  &lt;ul data-olist="."&gt;
 *      &lt;li data-numbering="%I)"&gt;&lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * After templates rendering <em>li</em> elements text content will be I), II) ... . See {@link NumberingOperator} for
 * details about numbering format syntax.
 * 
 * @author Iulian Rotaru
 */
class UpperCaseRomanNumbering extends NumberingFormat
{
  /**
   * Format index as upper case Roman numeral.
   * 
   * @param index index value.
   * @return formatted index.
   */
  @Override
  public String format(int index)
  {
    StringBuilder sb = new StringBuilder();
    final RomanNumeral[] values = RomanNumeral.values();
    for(int i = values.length - 1; i >= 0; i--) {
      while(index >= values[i].weight) {
        sb.append(values[i]);
        index -= values[i].weight;
      }
    }
    return sb.toString();
  }

  /**
   * Roman numerals constants.
   * 
   * @author Iulian Rotaru
   */
  private enum RomanNumeral
  {
    /** 1 */
    I(1),

    /** 4 */
    IV(4),

    /** 5 */
    V(5),

    /** 9 */
    IX(9),

    /** 10 */
    X(10),

    /** 40 */
    XL(40),

    /** 50 */
    L(50),

    /** 90 */
    XC(90),

    /** 100 */
    C(100),

    /** 400 */
    CD(400),

    /** 500 */
    D(500),

    /** 900 */
    CM(900),

    /** 1000 */
    M(1000);

    /** Roman numeral weight. */
    int weight;

    /**
     * Construct Roman numeral constant.
     * 
     * @param weight
     */
    RomanNumeral(int weight)
    {
      this.weight = weight;
    }
  }
}