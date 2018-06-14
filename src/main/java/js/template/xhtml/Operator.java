package js.template.xhtml;

import java.io.IOException;

import js.dom.Element;
import js.log.Log;
import js.log.LogFactory;
import js.template.TemplateException;

/**
 * Templates operator. A DOM bases template define operators in an element context; an element may have none, one or more
 * declared operators. An operator declaration consist of operator code, its opcode, and exactly one operand - more formally,
 * all operators arity is one. In example below <em>src</em> is first operator opcode and <em>picture</em> its operand. When
 * operator implementation is {@link #exec(Element, Object, String, Object[]) executed} both element and the operand are passed.
 * 
 * <pre>
 *  &lt;img data-src="picture" data-title="description" /&gt;
 * </pre>
 * 
 * This templates implementation in its essence is a serializer: DOM is parsed using depth-first algorithm and every element
 * serialized to a given writer. Operators contribute to serialization process adding specific content and selecting branches.
 * All operators are fully defined, that is, they do not depends on a context created by another operator. In a sense the are
 * context free: yields always the same result no matter evaluation context.
 * 
 * @author Iulian Rotaru
 */
abstract class Operator
{
  /**
   * Operator implementations logger.
   */
  protected static final Log log = LogFactory.getLog(Operator.class);

  /**
   * Execute operator. Execute operator logic into element context and returns a value; depending on specific operator
   * implementation not all declared parameters may be used and returned type may vary, including void when return null. Operand
   * string can denote a property path, an expression or a qualified class name, see every operator description.
   * <p>
   * A property path is used to access content values and can be absolute, prefixed with dot or relative to scope object. An
   * expression is operator specific and should contain also some means to identify a content values. The point is,
   * <em>scope</em>, <em>operand</em> tuple is used to somehow identify the content value(s) that will be injected by operator.
   * 
   * @param element element on which operator is declared,
   * @param scope scope object, used when operand is a property path,
   * @param operand declared operand,
   * @param arguments optional, operator type specific, argument(s).
   * @return operator specific value or null.
   * @throws IOException if underlying writer fails to write.
   * @throws TemplateException if content value not found or of bad type.
   */
  Object exec(Element element, Object scope, String operand, Object... arguments) throws IOException, TemplateException
  {
    try {
      return doExec(element, scope, operand, arguments);
    }
    catch(TemplateException exception) {
      log.warn("Templates exception:\r\n" + //
          "\t- element: %s\r\n" + //
          "\t- operator: %s\r\n" + //
          "\t- operand: %s\r\n" + // " +
          "\t- scope: %s\r\n" + // " +
          "\t- cause: %s", element.trace(), getClass(), operand, scope, exception);
      throw exception;
    }
  }

  /**
   * Operator internal workhorse. Delegated by {@link #exec(Element, Object, String, Object[])}; it has the same parameters list
   * and return value.
   * 
   * @throws IOException if underlying writer fails to write.
   * @throws TemplateException if operator tries to access content using a bad property path.
   */
  protected abstract Object doExec(Element element, Object scope, String operand, Object... arguments) throws IOException, TemplateException;
}
