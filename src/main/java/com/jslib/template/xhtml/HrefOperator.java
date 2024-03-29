package com.jslib.template.xhtml;

import java.net.URL;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.converter.ConverterRegistry;

/**
 * Set <em>href</em> attribute value.
 * 
 * <pre>
 *  &lt;a data-href="url"&gt;Follow the link...&lt;/a&gt;
 * </pre>
 * 
 * Operand is the property path used to get content value.
 * 
 * @author Iulian Rotaru
 */
final class HrefOperator extends Operator {
	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct HREF operator instance.
	 * 
	 * @param content dynamic content.
	 */
	HrefOperator(Content content) {
		this.content = content;
	}

	/**
	 * Execute HREF operator. Uses property path to extract content value, convert it to string and set <em>href</em> attribute.
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
		if (value instanceof URL) {
			value = ((URL) value).toExternalForm();
		}
		if (!(value instanceof String)) {
			throw new TemplateException("Invalid element |%s|. HREF operand should be URL or string.", element);
		}
		return new AttrImpl("href", (String) value);
	}
}
