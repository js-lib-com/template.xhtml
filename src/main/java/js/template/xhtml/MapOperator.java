package js.template.xhtml;

import java.io.IOException;
import java.util.Map;

import js.converter.ConverterRegistry;
import js.dom.Element;
import js.template.TemplateException;
import js.template.xhtml.Opcode.Type;

/**
 * Populate element using first two children as key/value templates. Extract content map designated by declared property path
 * then uses first two elements as key/value templates and repeat them for every map entry. Missing child element templates is
 * fatal error so context element should have at least two child elements. If more, they are simple ignored. When processing map
 * entries this operator does a temporary scope object switch. Every child element is processed into respective object scope.
 * Map key/value pair can be primitives, arbitrary complex object or nested lists or maps. There is no restriction on nesting
 * level.
 * 
 * <pre>
 *  &lt;dl data-map="map"&gt;
 *      &lt;dt data-text="."&gt;&lt;/dt&gt;
 *      &lt;dd data-text="."&gt;&lt;/dd&gt;
 *  &lt;/dl&gt;
 *  
 *  Map&lt;String, String&gt; map;
 * </pre>
 * 
 * This operator operand is the property path designating the map.
 * <p>
 * Map entry templates operators are processed recursively by engine logic. Anyway, child element operator can miss, in which
 * case default is applied as follows: if template element has children is assumed to have {@link Opcode#OBJECT} operator,
 * otherwise {@link Opcode#TEXT}. So above sample can be rewritten:
 * 
 * <pre>
 *  &lt;dl data-map="map"&gt;
 *      &lt;dt&gt;&lt;/dt&gt;
 *      &lt;dd&gt;&lt;/dd&gt;
 *  &lt;/dl&gt;
 * </pre>
 * <p>
 * Note that this operator belongs to {@link Type#CONTENT} group and only one content operator is allowed per element. See the
 * {@link Type#CONTENT the list} of mutually excluding content operators.
 * 
 * @author Iulian Rotaru
 */
final class MapOperator extends Operator {
	/** Parent serializer instance. */
	private Serializer serializer;

	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct MAP operator instance.
	 *
	 * @param serializer parent serializer,
	 * @param content dynamic content.
	 */
	MapOperator(Serializer serializer, Content content) {
		this.serializer = serializer;
		this.content = content;
	}

	/**
	 * Execute MAP operator. Extract content map using given property path and serialize every map entry using first two child
	 * elements as key/value templates.
	 * 
	 * @param element context element,
	 * @param scope scope object,
	 * @param propertyPath property path,
	 * @param arguments optional arguments, not used.
	 * @return always returns null to signal full processing.
	 * @throws IOException if underlying writer fails to write.
	 * @throws TemplateException if element has not at least two children or content map is undefined.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String propertyPath, Object... arguments) throws IOException, TemplateException {
		if (!propertyPath.equals(".") && ConverterRegistry.hasType(scope.getClass())) {
			throw new TemplateException("Operand is property path but scope is not an object.");
		}
		Element keyTemplate = element.getFirstChild();
		if (keyTemplate == null) {
			throw new TemplateException("Invalid map element |%s|. Missing key template.", element);
		}
		Element valueTemplate = keyTemplate.getNextSibling();
		if (valueTemplate == null) {
			throw new TemplateException("Invalid map element |%s|. Missing value template.", element);
		}
		Map<?, ?> map = content.getMap(scope, propertyPath);
		for (Object key : map.keySet()) {
			serializer.writeItem(keyTemplate, key);
			serializer.writeItem(valueTemplate, map.get(key));
		}
		return null;
	}
}
