package com.jslib.template.xhtml;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.converter.ConverterRegistry;
import com.jslib.template.xhtml.Opcode.Type;

/**
 * Ordered variant of {@link MapOperator}. Ordered map operator works in tandem with {@link NumberingOperator} to create a
 * ordered map, i.e. every map key/value pairs has an index with specified {@link NumberingFormat format}. This class takes care
 * of index creation, increment index before every key/value pair processing while numbering operators deals only with format.
 * If numbering operator is missing this operator acts exactly as unordered map; anyway, validation tool warns this condition.
 * 
 * <pre>
 *  &lt;dl&gt;
 *      &lt;dt&gt;&lt;span data-numbering="%n."&gt;&lt;/span&gt; &lt;span data-text="term"&gt;&lt;/span&gt;&lt;/dt&gt;
 *      &lt;dd data-text="definition"&gt;&lt;/dd&gt;
 *  &lt;/dl&gt;
 * </pre>
 * 
 * Note that this operator belongs to CONTENT group and only one content operator is allowed per element. See the
 * {@link Type#CONTENT the list} of mutually excluding content operators.
 * 
 * @author Iulian Rotaru
 */
final class OMapOperator extends Operator {
	/** Parent serializer instance. */
	private Serializer serializer;

	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct OMAP operator instance.
	 * 
	 * @param serializer parent serializer,
	 * @param content dynamic content.
	 */
	OMapOperator(Serializer serializer, Content content) {
		this.serializer = serializer;
		this.content = content;
	}

	/**
	 * Execute OMAP operator. Behaves like {@link MapOperator#doExec(Element, Object, String, Object...)} counterpart but takes
	 * care to create index and increment it before every key / value pair processing.
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
		Stack<Index> indexes = serializer.getIndexes();
		Index index = new Index();
		indexes.push(index);
		Map<?, ?> map = content.getMap(scope, propertyPath);
		for (Object key : map.keySet()) {
			index.increment();
			serializer.writeItem(keyTemplate, key);
			serializer.writeItem(valueTemplate, map.get(key));
		}
		indexes.pop();
		return null;
	}
}
