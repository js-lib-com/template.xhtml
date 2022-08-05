package com.jslib.template.xhtml;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.converter.ConverterRegistry;
import com.jslib.util.Types;

/**
 * Set <em>id</em> attribute value.
 * 
 * <pre>
 *  &lt;section data-id="id" /&gt;
 * </pre>
 * 
 * Operand is the property path used to get content value.
 * 
 * @author Iulian Rotaru
 */
final class IdOperator extends Operator {
	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct ID operator instance.
	 * 
	 * @param content dynamic content.
	 */
	IdOperator(Content content) {
		this.content = content;
	}

	/**
	 * Execute ID operator. Uses property path to extract content value, convert it to string and set <em>id</em> attribute.
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
		if (Types.isNumber(value)) {
			value = value.toString();
		}
		if (Types.isEnum(value)) {
			value = ((Enum<?>) value).name();
		}
		if (!(value instanceof String)) {
			throw new TemplateException("Invalid element |%s|. ID operand should be string, enumeration or numeric.", element);
		}
		return new AttrImpl("id", (String) value);
	}
}
