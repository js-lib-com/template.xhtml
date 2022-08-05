package com.jslib.template.xhtml;

import java.io.File;
import java.net.URL;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.converter.ConverterRegistry;

/**
 * Set <em>src</em> attribute value.
 * 
 * <pre>
 *  &lt;img data-src="picture" /&gt;
 * </pre>
 * 
 * Operand is the property path used to get content value.
 * 
 * @author Iulian Rotaru
 */
final class SrcOperator extends Operator {
	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct SRC operator instance.
	 *
	 * @param content dynamic content.
	 */
	public SrcOperator(Content content) {
		this.content = content;
	}

	/**
	 * Execute SRC operator. Uses property path to extract content value, convert it to string and set <em>src</em> attribute.
	 * If property value is null uses current element <em>src</em> attribute value, if any.
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
		if (value instanceof File) {
			value = ((File) value).getPath();
		}
		if (!(value instanceof String)) {
			throw new TemplateException("Invalid element |%s|. SRC operand should be URL, file or string.", element);
		}
		return new AttrImpl("src", (String) value);
	}
}
