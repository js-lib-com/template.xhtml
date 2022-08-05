package com.jslib.template.xhtml;

import java.io.IOException;
import java.util.Stack;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.converter.ConverterRegistry;
import com.jslib.template.xhtml.Opcode.Type;

/**
 * Ordered variant of {@link ListOperator}. Ordered list operator works in tandem with {@link NumberingOperator} to create a
 * ordered list, i.e. every list item has an index with specified {@link NumberingFormat format}. This class takes care of index
 * creation, increment index before every item processing while numbering operators deals only with format. If numbering
 * operator is missing this operator acts exactly as unordered list; anyway, validation tool warns this condition.
 * 
 * <pre>
 *  &lt;section data-olist="chapters"&gt;
 *      &lt;h1&gt;&lt;span data-numbering="%n."&gt;&lt;/span&gt; &lt;span data-text="title"&gt;&lt;/span&gt;&lt;/h1&gt;
 *      &lt;p data-text="content"&gt;&lt;/p&gt;
 *  &lt;/section&gt;
 * </pre>
 * 
 * Note that this operator belongs to {@link Type#CONTENT} group and only one content operator is allowed per element. See the
 * {@link Type#CONTENT the list} of mutually excluding content operators.
 * 
 * @author Iulian Rotaru
 */
final class OListOperator extends Operator {
	/** Parent serializer instance. */
	private Serializer serializer;

	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct OLIST operator instance.
	 * 
	 * @param serializer parent serializer,
	 * @param content dynamic content.
	 */
	OListOperator(Serializer serializer, Content content) {
		this.serializer = serializer;
		this.content = content;
	}

	/**
	 * Execute OLIST operator. Behaves like {@link ListOperator#doExec(Element, Object, String, Object...)} counterpart but
	 * takes care to create index and increment it before every item processing.
	 * 
	 * @param element context element,
	 * @param scope scope object,
	 * @param propertyPath property path,
	 * @param arguments optional arguments, not used.
	 * @return always returns null to signal full processing.
	 * @throws IOException if underlying writer fails to write.
	 * @throws TemplateException if element has no children or content list is undefined.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String propertyPath, Object... arguments) throws IOException, TemplateException {
		if (!propertyPath.equals(".") && ConverterRegistry.hasType(scope.getClass())) {
			throw new TemplateException("Operand is property path but scope is not an object.");
		}
		Element itemTemplate = element.getFirstChild();
		if (itemTemplate == null) {
			throw new TemplateException("Invalid list element |%s|. Missing item template.", element);
		}
		Stack<Index> indexes = serializer.getIndexes();
		Index index = new Index();
		indexes.push(index);
		for (Object item : content.getIterable(scope, propertyPath)) {
			index.increment();
			serializer.writeItem(itemTemplate, item);
		}
		indexes.pop();
		return null;
	}
}
