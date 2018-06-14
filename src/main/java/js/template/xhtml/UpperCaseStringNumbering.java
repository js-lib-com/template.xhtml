package js.template.xhtml;

/**
 * Upper case string list index. This class uses the set of English upper case characters to represent given index. If index
 * overflow the {@link #dictionary} repeat the character, e.g. 1 is A, 27 is AA, 53 is AAA and so on, where 26 is dictionary
 * size. Its format code is <b>S</b>.
 * 
 * <pre>
 *  &lt;ul data-olist="."&gt;
 *      &lt;li data-numbering="%S)"&gt;&lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * After templates rendering <em>li</em> elements text content will be A), B) ... . See {@link NumberingOperator} for details
 * about numbering format syntax.
 * 
 * @author Iulian Rotaru
 */
class UpperCaseStringNumbering extends NumberingFormat
{
  /**
   * Set of English upper case letters.
   */
  private static final String dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /**
   * Format index as upper case string. Transform index into a upper case string following next rules:
   * <ul>
   * <li>be dictionary the set of English upper case characters,
   * <li>if index is smaller that dictionary length uses index directly to extract the character and return it,
   * <li>divide index by dictionary length,
   * <li>be chars count the quotient plus one,
   * <li>be index equals remainder,
   * <li>extract char from dictionary using index and return it repeated chars count times.
   * </ul>
   * 
   * @param index index value.
   * @return formatted index.
   */
  @Override
  public String format(int index)
  {
    --index; // lists index value starts with 1
    int charsCount = index / dictionary.length() + 1;
    index = index % dictionary.length();
    char c = dictionary.charAt(index);
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < charsCount; ++i) {
      sb.append(c);
    }
    return sb.toString();
  }
}