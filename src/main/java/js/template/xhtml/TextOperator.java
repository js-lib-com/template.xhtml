package js.template.xhtml;

import java.io.IOException;

import js.converter.ConverterRegistry;
import js.dom.Element;
import js.format.Format;
import js.template.TemplateException;

/**
 * Set element text content. Extract content value declared by this operator operand and set context element text content.
 * Content value type is not constrained to string, this operator taking care to convert it. Note that this operator uses
 * context element format instance, if one was declared. See formatted content value
 * {@link Content#getString(Object, String, Format) getter}.
 * 
 * <pre>
 *  &lt;span data-text="birthday" data-format="js.format.LongDate"&gt;&lt;/span&gt;
 * </pre>
 * 
 * Operand is the property path used to get content value.
 * 
 * @author Iulian Rotaru
 */
final class TextOperator extends Operator {
	/** Parent serializer instance. */
	private Serializer serializer;

	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct TEXT operator instance.
	 * 
	 * @param serializer parent serializer instance,
	 * @param content dynamic content reference.
	 */
	TextOperator(Serializer serializer, Content content) {
		this.serializer = serializer;
		this.content = content;
	}

	/**
	 * Execute TEXT operator. Uses property path to extract content value, convert it to string and set element text content.
	 * Note that this operator operates on element without children. Failing to obey this constraint rise templates exception;
	 * anyway, validation tool catches this condition.
	 * 
	 * @param element context element,
	 * @param scope scope object,
	 * @param propertyPath property path,
	 * @param arguments optional arguments, {@link Format} instance in this case.
	 * @return always returns null to signal full processing.
	 * @throws IOException if underlying writer fails to write.
	 * @throws TemplateException if context element has children or requested content value is undefined.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String propertyPath, Object... arguments) throws IOException, TemplateException {
		if (!propertyPath.equals(".") && ConverterRegistry.hasType(scope.getClass())) {
			throw new TemplateException("Operand is property path but scope is not an object.");
		}
		if (element.hasChildren()) {
			throw new TemplateException("Illegal TEXT operator on element with children.");
		}
		Format format = (Format) arguments[0];
		String text = content.getString(scope, propertyPath, format);
		if (text != null) {
			serializer.writeTextContent(text);
		}
		return null;
	}
}
