package com.jslib.template.xhtml;

/**
 * Formatter used by ordered lists and maps for numbering. Simple put, a numbering formatter gets an index and transform it into
 * a related string. These are stock implementations:
 * <table>
 * <tr>
 * <td>name
 * <td>code
 * <td>sample
 * <tr>
 * <td>{@link ArabicNumeralNumbering arabic numeral}
 * <td><b>n
 * <td>1, 2, 3, 4 ... 10, 11 ... 27, 28
 * <tr>
 * <td>{@link LowerCaseRomanNumbering lower roman}
 * <td><b>i
 * <td>i, ii, iii, iv ... x, xi ... xxvii, xxviii
 * <tr>
 * <td>{@link UpperCaseRomanNumbering upper roman}
 * <td><b>I
 * <td>I, II, III, IV ... X, XI ... XXVII, XXVIII
 * <tr>
 * <td>{@link LowerCaseStringNumbering lower string}
 * <td><b>s
 * <td>a, b, c, d ... k, l ... aa, bb
 * <tr>
 * <td>{@link UpperCaseStringNumbering upper string}
 * <td><b>S
 * <td>A, B, C, D ... K, L ... AA, BB
 * </table>
 * Please note that, in order to keep things simple, there is no support for user defined numbering formatters.
 * 
 * @author Iulian Rotaru
 */
abstract class NumberingFormat
{
  /**
   * Generate string representation for given ordinal index.
   * 
   * @param index ordinal index.
   * @return index string representation.
   */
  abstract String format(int index);
}