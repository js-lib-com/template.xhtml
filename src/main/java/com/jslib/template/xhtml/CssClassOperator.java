package com.jslib.template.xhtml;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.lang.Pair;
import com.jslib.lang.PairsList;

/**
 * Add or remove element CSS class considering conditional expression value. In sample code there are two class expressions
 * evaluated in natural order. If conditional expression is true add related class otherwise remove it. In example, if
 * <code>type</code> value is <code>DIRECTORY</code> add CSS class <code>directory</code> to <code>div</code> element otherwise
 * add <code>file</code> class.
 * <p>
 * 
 * <pre>
 *  &lt;div data-css-class="type=DIRECTORY:directory;!type=DIRECTORY:file;" /&gt;
 * </pre>
 * 
 * Operand is a list of semicolon separated CSS class expressions, see syntax below. A class expression has a conditional
 * expression as described by {@link ConditionalExpression} class description and a CSS class name separated by colon.
 * <p>
 * 
 * <pre>
 *    operand = class-expression *( ';' class-expression ) [ ';' ]
 *    class-expression = conditional-expression ':' class-name
 *    
 *    ; conditional-expression = see conditional expression class description
 *    ; class-name = CSS class name
 * </pre>
 * 
 * @author Iulian Rotaru
 */
final class CssClassOperator extends Operator {
	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct CSS_CLASS operator instance.
	 * 
	 * @param serializer parent serializer,
	 * @param content dynamic content.
	 */
	CssClassOperator(Serializer serializer, Content content) {
		this.content = content;
	}

	@Override
	protected Object doExec(Element element, Object scope, String expression, Object... arguments) throws TemplateException {
		if (expression.isEmpty()) {
			throw new TemplateException("Invalid CSS_CLASS operand. Expression is empty.");
		}

		CssClass cssClass = new CssClass(element);
		PairsList pairs = new PairsList(expression);

		for (Pair pair : pairs) {
			// accordingly CSS_CLASS operator syntax first pair value is a conditional expression and the second is the CSS
			// class name

			ConditionalExpression conditionalExpression = new ConditionalExpression(content, scope, pair.first());
			String className = pair.second();

			if (conditionalExpression.value()) {
				log.debug("True conditional expression |%s|. Add CSS class |%s| to element |%s|.", pair.first(), className, element.trace());
				cssClass.add(className);
			} else {
				log.debug("False conditional expression |%s|. Remove CSS class |%s| from element |%s|.", pair.first(), className, element.trace());
				cssClass.remove(className);
			}
		}

		return cssClass;
	}
}
