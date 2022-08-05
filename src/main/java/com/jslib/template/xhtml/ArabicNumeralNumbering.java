package com.jslib.template.xhtml;

/**
 * Arabic numeral numbering format. It is the most simple numbering formatter and maybe the most common; it just displays index
 * value. Its format code is <b>n</b>.
 * 
 * <pre>
 *  &lt;ul data-olist="."&gt;
 *      &lt;li data-numbering="%n)"&gt;&lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * As with any numbering formatter this also allows for additional text; above example will render 1), 2) ... . See
 * {@link NumberingOperator} for details about numbering format syntax.
 * 
 * @author Iulian Rotaru
 */
final class ArabicNumeralNumbering extends NumberingFormat
{
  /**
   * Format index as arabic numeral.
   * 
   * @param index index value.
   * @return formatted index.
   */
  @Override
  public String format(int index)
  {
    return Integer.toString(index);
  }
}