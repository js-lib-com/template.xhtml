package com.jslib.template.xhtml;

import java.io.IOException;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.converter.ConverterRegistry;
import com.jslib.template.xhtml.Opcode.Type;

/**
 * Populate element using first child as item template. Extract content list designated by defined property path then uses first
 * element as item template and repeat it for every item from list. Missing child is fatal error; if more children, they are
 * simple ignored. When processing items this operator does a temporary scope object switch. Every child element is processed
 * into list item object scope. List item can be primitives, arbitrary complex object or nested lists or maps. There is no
 * restriction on nesting level.
 * 
 * <pre>
 *  &lt;ul data-list="persons"&gt;
 *      &lt;li data-text="name"&gt;&lt;/li&gt;
 *  &lt;/ul&gt;
 *  
 *  List&lt;Person&gt; persons;
 *  class Person {
 *      String name;
 *  }
 * </pre>
 * 
 * This operator operand is the property path designating the list.
 * <p>
 * Item template operators are processed recursively by engine logic. Anyway, child element operator can miss, in which case
 * default is applied as follows: if template element has children is assumed to have {@link Opcode#OBJECT} operator, otherwise
 * {@link Opcode#TEXT}.
 * 
 * <pre>
 *  &lt;ul data-list="."&gt;
 *      &lt;li&gt; . . . &lt;/li&gt;
 *  &lt;/ul&gt;
 * </pre>
 * 
 * Note that this operator belongs to {@link Type#CONTENT} group and only one content operator is allowed per element. See the
 * {@link Type#CONTENT the list} of mutually excluding content operators.
 * 
 * @author Iulian Rotaru
 */
final class ListOperator extends Operator {
	/** Parent serializer instance. */
	private Serializer serializer;

	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct LIST operator instance.
	 *
	 * @param serializer parent serializer,
	 * @param content dynamic content.
	 */
	ListOperator(Serializer serializer, Content content) {
		this.serializer = serializer;
		this.content = content;
	}

	/**
	 * Execute LIST operator. Extract content list then repeat context element first child for every list item.
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
		for (Object item : content.getIterable(scope, propertyPath)) {
			serializer.writeItem(itemTemplate, item);
		}
		return null;
	}
}
