package com.jslib.template.xhtml;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;

/**
 * Include DOM branch if conditional expression evaluates to true. Operand is a conditional expression. If evaluates to true, in
 * our example <code>type</code> value is <code>DIRECTORY</code>, <code>div</code> element and all its descendants are excluded
 * from resulting document. See {@link ConditionalExpression} for valid syntax supported by <code>if</code> operator.
 * 
 * <pre>
 *  &lt;div data-if="type=DIRECTORY"&gt;  
 *  . . .
 *  &lt;/div&gt;
 * </pre>
 * 
 * This operator can combine conditional expression and <code>not</code> flag to emulate <code>if/else</code>. Please note
 * exclamation mark from condition expression start on <code>else</code> branch.
 * 
 * <pre>
 *  &lt;div data-if="type=DIRECTORY"&gt;  
 *      // this branch is included if type is DIRECTORY
 *  &lt;/div&gt;
 *  &lt;div data-if="!type=DIRECTORY"&gt;  
 *      // this branch is included if type is not DIRECTORY
 *  &lt;/div&gt;
 * </pre>
 * 
 * Similarly a <code>switch/case</code> branch can be emulated like in sample code below:
 * 
 * <pre>
 *  &lt;div data-if="fruit=APPLE"&gt;
 *      // enable this branch if fruit is APPLE
 *  &lt;/div&gt;
 *  &lt;div data-if="fruit=ORANGE"&gt;
 *      // enable this branch if fruit is ORANGE
 *  &lt;/div&gt;
 *  &lt;div data-if="fruit=BANANA"&gt;
 *      // enable this branch if fruit is BANANA
 *  &lt;/div&gt;
 * </pre>
 * 
 * @author Iulian Rotaru
 */
final class IfOperator extends Operator {
	/** Dynamic content. */
	private Content content;

	/**
	 * Construct IF operator instance.
	 * 
	 * @param content dynamic content.
	 */
	IfOperator(Content content) {
		this.content = content;
	}

	/**
	 * Execute IF operator. Evaluate given <code>expression</code> and return evaluation result. Returned value acts as branch
	 * enabled flag.
	 * 
	 * @param element context element, unused,
	 * @param scope scope object,
	 * @param expression conditional expression,
	 * @param arguments optional arguments, not used.
	 * @return true if <code>element</code> and all its descendants should be included in processed document.
	 * @throws TemplateException if content value is undefined.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String expression, Object... arguments) throws TemplateException {
		ConditionalExpression conditionalExpression = new ConditionalExpression(content, scope, expression);
		return conditionalExpression.value();
	}
}
