package js.template.xhtml;

import js.converter.ConverterRegistry;
import js.dom.Element;
import js.format.Format;
import js.template.TemplateException;

/**
 * Set <em>value</em> attribute value. Extract content value declared by this operator operand and set the element
 * <em>value</em>. Content value type is not constrained to string, this operator taking care to convert it. Note that this
 * operator uses context element format instance, if one was declared. See formatted content value
 * {@link Content#getString(Object, String, Format) getter}.
 * 
 * <pre>
 *  &lt;input data-value="name" data-format="js.format.LongDate" /&gt;
 * </pre>
 * 
 * Operand is the property path used to get content value.
 * 
 * @author Iulian Rotaru
 */
final class ValueOperator extends Operator {
	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct VALUE operator instance.
	 * 
	 * @param content dynamic content.
	 */
	ValueOperator(Content content) {
		this.content = content;
	}

	/**
	 * Execute VALUE operator. Uses property path to extract content value, convert it to string and set element <em>value</em>
	 * attribute.
	 * 
	 * @param element context element, unused,
	 * @param scope scope object,
	 * @param propertyPath property path,
	 * @param arguments optional arguments, {@link Format} instance in this case.
	 * @return always returns null for void.
	 * @throws TemplateException if requested content value is undefined.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String propertyPath, Object... arguments) throws TemplateException {
		if (!propertyPath.equals(".") && ConverterRegistry.hasType(scope.getClass())) {
			throw new TemplateException("Operand is property path but scope is not an object.");
		}
		String value = null;
		Format format = (Format) arguments[0];

		if (format != null) {
			value = this.content.getString(scope, propertyPath, format);
		} else {
			Object object = this.content.getObject(scope, propertyPath);
			if (object != null) {
				if (!ConverterRegistry.hasType(object.getClass())) {
					throw new TemplateException("Invalid element |%s|. Operand for VALUE operator without formatter should be convertible to string.", element);
				}
				value = ConverterRegistry.getConverter().asString(object);
			}
		}

		if (value == null) {
			return null;
		}
		return new AttrImpl("value", value);
	}
}
