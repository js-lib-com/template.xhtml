package com.jslib.template.xhtml;

import java.util.HashSet;
import java.util.Set;

import com.jslib.api.dom.Attr;
import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.lang.Pair;
import com.jslib.lang.PairsList;

/**
 * Set one or more element's attributes values. This operator is the main means to set element attributes value. There are also
 * specialized, convenient operators for common HTML attributes: id, value, src, href and title.
 * 
 * <pre>
 *  &lt;img data-attr="src:picture;title:description;" /&gt;
 * </pre>
 * 
 * Operand is an expression describing attribute name/property path, with next syntax:
 * 
 * <pre>
 *    expression := attrProperty (';' attrProperty)* ';'?
 *    attrProperty : = attributeName ':' propertyPath
 *    propertyPath := used to extract content value
 * </pre>
 * 
 * @author Iulian Rotaru
 */
final class AttrOperator extends Operator
{
  /**
   * Dynamic content reference.
   */
  private Content content;

  /**
   * Construct ATTR operator instance.
   * 
   * @param content dynamic content.
   */
  AttrOperator(Content content)
  {
    this.content = content;
  }

  /**
   * Execute ATTR operator. Expression argument is set of attribute name / property path pairs. Property path is used to
   * retrieve content value that is converted to string and used as attribute value.
   * 
   * @param element context element, unused,
   * @param scope scope object,
   * @param expression set of attribute name / property path pairs,
   * @param arguments optional arguments, unused.
   * @return always returns null for void.
   * @throws TemplateException if expression is empty or not well formatted or if requested content value is undefined.
   */
  @Override
  protected Object doExec(Element element, Object scope, String expression, Object... arguments) throws TemplateException
  {
    if(expression.isEmpty()) {
      throw new TemplateException("Invalid ATTR operand. Attribute property path expression is empty.");
    }
    Set<Attr> syntheticAttributes = new HashSet<Attr>();

    PairsList pairs = new PairsList(expression);
    for(Pair pair : pairs) {
      // accordingly this operator expression syntax first value is attribute name and second is property path
      String value = content.getString(scope, pair.second());
      if(value != null) {
        syntheticAttributes.add(new AttrImpl(pair.first(), value));
      }
    }

    return syntheticAttributes;
  }
}
