package js.template.xhtml;

import js.converter.ConverterRegistry;
import js.dom.Element;
import js.template.TemplateException;

/**
 * Set <em>title</em> attribute value.
 * 
 * <pre>
 *  &lt;section data-title="description" /&gt;
 * </pre>
 * 
 * Operand is the property path used to get content value.
 * 
 * @author Iulian Rotaru
 */
final class TitleOperator extends Operator {
	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct TITLE operator instance.
	 * 
	 * @param content dynamic content.
	 */
	TitleOperator(Content content) {
		this.content = content;
	}

	/**
	 * Execute TITLE operator. Uses property path to extract content value, convert it to string and set <em>title</em>
	 * attribute.
	 * 
	 * @param element context element, unused,
	 * @param scope scope object,
	 * @param propertyPath property path,
	 * @param arguments optional arguments, unused.
	 * @return always returns null for void.
	 * @throws TemplateException if requested content value is undefined.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String propertyPath, Object... arguments) throws TemplateException {
		if (!propertyPath.equals(".") && ConverterRegistry.hasType(scope.getClass())) {
			throw new TemplateException("Operand is property path but scope is not an object.");
		}
		Object value = content.getObject(scope, propertyPath);
		if (value == null) {
			return null;
		}
		if (!(value instanceof String)) {
			throw new TemplateException("Invalid element |%s|. TITLE operand should be string.", element);
		}
		return new AttrImpl("title", (String) value);
	}
}
