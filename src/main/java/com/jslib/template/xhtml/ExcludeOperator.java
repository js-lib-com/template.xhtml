package com.jslib.template.xhtml;

import com.jslib.api.dom.Element;

/**
 * Exclude element and its descendants from resulting document. What exclusion means is implementation dependent: one may choose
 * to hide somehow - maybe display:none, another to simple remove the branch completely from resulting document. The point is,
 * the marked branch must not be visible to end user. This operator is not so much a conditional one since test is performed on
 * a boolean literal rather than some content value. Branch exclusion is actually decided on development phase. A good usage
 * example may be email template: head meta is used for email initialization but not included into delivery.
 * 
 * <pre>
 *  &lt;head data-exclude="true"&gt;
 *      &lt;meta name="from" content="from@server.com" /&gt;
 *      &lt;meta name="subject" content="subject" /&gt;
 *  &lt;/head&gt;
 * </pre>
 * 
 * Operand is a boolean literal. Nothing special: <em>true</em> or <em>false</em>.
 * 
 * @author Iulian Rotaru
 */
public class ExcludeOperator extends Operator
{
  /**
   * Execute EXCLUDE operator. Returns branch enabled flag, that is, true to indicate branch is to be included in resulting
   * document. Since exclude operator has opposite logic we need to negate given boolean expression; so, if operand is 'true'
   * meaning the branch should be excluded this method returns false.
   * 
   * @param element context element, unused,
   * @param scope scope object, unused,
   * @param booleanExpression boolean expression, 'true' or 'false',
   * @param arguments optional arguments, not used.
   * @return branch enabled flag.
   */
  @Override
  protected Object doExec(Element element, Object scope, String booleanExpression, Object... arguments)
  {
    // returned value is interpreted as branch enabled
    // boolean expression argument is true if branch should be excluded, so we need to inverse it
    return !Boolean.valueOf(booleanExpression);
  }
}
