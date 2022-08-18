package com.jslib.template.xhtml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import com.jslib.api.dom.Attr;
import com.jslib.api.dom.ChildNode;
import com.jslib.api.dom.Element;
import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;
import com.jslib.api.template.TemplateException;
import com.jslib.format.Format;
import com.jslib.template.xhtml.OperatorsList.Meta;
import com.jslib.util.Strings;

/**
 * Templates serializer. This class serialize template document and execute operators on the fly. Its classic use case is HTML
 * pages serialization on HTTP response writer, but for no means limited to this. This class has specialized writing methods for
 * elements start, end tag, attributes and text content. Serialization process is started externally by a call to
 * {@link Serializer#write(Element, Object)} method with a given element and traverse recursively the element all its
 * descendants, using depth-first algorithm.
 * 
 * @author Iulian Rotaru
 */
final class Serializer {
	/** Class logger. */
	private static final Log log = LogFactory.getLog(Serializer.class);

	/** This serializer writer. */
	private Writer writer;

	/** Operator factory. */
	private OperatorFactory factory;

	/**
	 * Indexes stack for ordered lists. Every ordered list has its own index instance and in order to cope with nested lists we
	 * used this stack. For the same reason, although indexes are used only by {@link ListOperator}, indexes stack is global per
	 * serializer instance.
	 */
	private Stack<Index> indexes = new Stack<Index>();

	/**
	 * By default operators are not serialized on resulting document. Forcing this flag to true will determine serializer to
	 * include operators, useful for data extraction.
	 */
	private boolean enableOperatorsSerialization;

	/**
	 * Set this serializer writer.
	 * 
	 * @param writer resulting document goes on this writer.
	 */
	void setWriter(Writer writer) {
		this.writer = writer instanceof BufferedWriter ? writer : new BufferedWriter(writer);
	}

	/**
	 * Set this serializer content. Content instance is not used by this class but is passed to operators factory, which is
	 * created by this method.
	 * 
	 * @param content dynamic content to inject.
	 */
	void setContent(Content content) {
		factory = new OperatorFactory(this, content);
	}

	/**
	 * Disable operators serialization. By default, operators are included into resulting document. If document is validated
	 * operators syntax may collide with document grammar and render document invalid; or perhaps author just prefer to hide
	 * implementation details. In all cases uses this method to disable operators serialization.
	 */
	void enableOperatorsSerialization() {
		enableOperatorsSerialization = true;
	}

	/**
	 * Get indexes stack.
	 * 
	 * @return indexes stack.
	 */
	Stack<Index> getIndexes() {
		return indexes;
	}

	/**
	 * Write recursively the element and its children. It is both serialization entry point and recursive loop; prepares
	 * element's operators list and delegates {@link #write(Element, Object, OperatorsList)} for real work.
	 * 
	 * @param element element to serialize,
	 * @param scope object scope.
	 * @throws TemplateException if underlying writer fails to write.
	 * @throws IOException if underlying writer fails to write.
	 */
	void write(Element element, Object scope) throws IOException {
		OperatorsList operators = new OperatorsList(element);
		write(element, scope, operators);
	}

	/**
	 * Write list or map item. List and map operators invoke this method to process every item; it prepare element's operators
	 * list and delegates {@link #write(Element, Object, OperatorsList)} for real work.
	 * 
	 * @param element element to serialize,
	 * @param scope object scope.
	 * @throws TemplateException if underlying writer fails to write.
	 * @throws IOException if underlying writer fails to write.
	 */
	void writeItem(Element element, Object scope) throws IOException {
		OperatorsList operators = new OperatorsList(element, true);
		write(element, scope, operators);
	}

	/**
	 * Escape XML reserved chars and write as element text content. Used by text and numbering operators to actually write the
	 * element text content. At the moment this method is invoked templates engine was already wrote start tag, including
	 * closing tag mark, e.g. <em>&lt;div attrs... &gt;</em>.
	 * 
	 * @param text element text content.
	 * @throws IOException if underlying writer fails to write.
	 */
	void writeTextContent(String text) throws IOException {
		writer.write((String) Strings.escapeXML(text));
	}

	/**
	 * Write HTML content as it is, that is, no XML escape performed. Used by HTML operator to actually write the element inner
	 * HTML.
	 * 
	 * @param html HTML content.
	 * @throws IOException if underlying writer fails to write.
	 */
	void writeHtmlContent(String html) throws IOException {
		writer.write(html);
	}

	/**
	 * Format and send attribute name and value to underlying writer. Used by attribute operators to actually write formatted
	 * attribute to underlying writer. When attribute operators are executed templates engine was already sent to writer the
	 * element opening tag mark and tag name, e.g. <em>&lt;div</em>. Formatted attribute is prefixed with space followed by
	 * name, equals sign and value between double quotes, see below BNF.
	 * 
	 * <pre>
	 *    attribute := ' ' name '=' '"' value '"'
	 * </pre>
	 * 
	 * @param name attribute name,
	 * @param value attribute value.
	 * @throws IOException if underlying writer fails to write.
	 */
	void writeAttribute(String name, String value) throws IOException {
		writer.write(' ');
		writer.write(name);
		writer.write('=');
		writer.write('"');
		writer.write(Strings.escapeXML(value));
		writer.write('"');
	}

	/**
	 * Write string to underlying writer.
	 * 
	 * @param string string to send to underlying writer.
	 * @throws IOException if underlying writer fails to write.
	 */
	void write(String string) throws IOException {
		writer.write(string);
	}

	/**
	 * Flush underlying writer.
	 * 
	 * @throws IOException if underlying writer fails to flush.
	 */
	void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Helper method for element serialization. This method is serializer workhorse; it implements templates generic algorithm
	 * as shown below. Takes care to write given element with all its components: start, end tag, attributes and perhaps text
	 * content; if element has children, process them recursively.
	 * <ul>
	 * <li>if conditional operator exists, return from this method if operator execution returns false,
	 * <li>if inline operator, execute it and return from this method,
	 * <li>write element start tag and attributes,
	 * <li>extract format instance, if one is declared,
	 * <li>executes all attribute operators,
	 * <li>if element is empty close element tag and return from this method,
	 * <li>execute content operator, if any and return from this method if content processed completely,
	 * <li>traverse all children recursively.
	 * </ul>
	 * Note that described algorithm is dubbed <em>generic</em> because it operates on operator types, not on concrete one. This
	 * way, one can add new operators without changing this algorithm.
	 * 
	 * @param element element to serialize,
	 * @param scope scope object,
	 * @param operators prepared operators list.
	 * @throws TemplateException if scope is null or this algorithm is not able to end properly.
	 * @throws IOException if underlying writer fails to write.
	 */
	@SuppressWarnings("unchecked")
	private void write(Element element, Object scope, OperatorsList operators) throws IOException {
		// do not process conditional operators on null scope
		if (scope != null && operators.hasConditionalOperator()) {
			Object returnedValue = execOperator(element, scope, operators.getConditionalOperatorMeta());
			if (returnedValue == null) {
				// conditional operators always returns boolean
				// anyway, if exception occurs operator super class returns null
				// this include content exception that can be generated when value is missing
				// in any case we consider condition not fulfilled and break this document branch processing
				return;
			}
			assert returnedValue instanceof Boolean;
			boolean branchEnabled = (Boolean) returnedValue;
			if (!branchEnabled) {
				log.debug("Element |{dom_element}| rejected by conditional operator.", element);
				return;
			}
		}

		// do not process formatter on null scope
		Format format = null;
		if (scope != null && operators.hasFormattingOperator()) {
			format = execOperator(element, scope, operators.getFormattingOperatorMeta());
		}

		String tag = element.getCaseSensitiveTag();
		writeOpenTag(tag);

		// if scope is null set includeCssClass parameter to true
		Set<Attr> attributes = collectAttributes(element, scope == null);
		// on null scope just write element attributes as they are
		if (scope != null) {
			for (Meta meta : operators.getAttributeOperatorsMeta()) {
				Object value = execOperator(element, scope, meta, format);
				// value returned by attribute operator execution can be null, instance of js.dom.Attr or Java Set

				if (value == null) {
					// null means executed attribute operator has nothing to serialize
					continue;
				}
				if (value instanceof Attr) {
					addAttribute(element, attributes, value);
					continue;
				}
				assert value instanceof Set;
				// Set.addAll return true if set was changed even some items to add are already in set
				// true means at least one item was added, so we need to do it hard way ;-)
				for (Attr attr : (Set<? extends Attr>) value) {
					addAttribute(element, attributes, attr);
				}
			}
		}
		writeAttributes(element, attributes);

		boolean emptyTag = HTML.EMPTY_TAGS.contains(tag);
		writeClosingMark(emptyTag);
		if (emptyTag) {
			return;
		}

		// do not process element dynamic content on null scope
		if (operators.hasContentOperator()) {
			if (scope != null) {
				scope = execOperator(element, scope, operators.getContentOperatorMeta(), format);
			}
			if (scope == null && operators.getContentOperatorMeta().opcode != Opcode.OBJECT) {
				// content operator returns null if fully processed, that is, branch is ended
				// so just close the tag and return
				writeEndTag(tag);
				return;
			}
		}

		// an element can have both child elements and text nodes if is formatted text
		// takes care to deal with formatted text
		
		for (ChildNode node : element.getChildNodes()) {
			if (node.isElement()) {
				write(node.asElement(), scope);
			} else {
				writeTextContent(node.asText());
			}
		}

		writeEndTag(tag);
	}

	/**
	 * Helper method for operator execution.
	 * 
	 * @param element
	 * @param scope
	 * @param meta
	 * @return operator specific value, cast to any type or null.
	 * @throws IOException if underlying writer fails to write.
	 */
	@SuppressWarnings("unchecked")
	private <T> T execOperator(Element element, Object scope, Meta meta, Format... format) throws IOException {
		Operator operator = factory.geInstance(meta.opcode);
		return (T) operator.exec(element, scope, meta.operand, format.length == 1 ? format[0] : null);
	}

	/**
	 * Write opening tag mark and tag name but not closing mark. Start a new element start tag, e.g. <em>&lt;div</em>.
	 * 
	 * @param tag tag name
	 * @throws IOException if underlying writer fails to write.
	 */
	private void writeOpenTag(String tag) throws IOException {
		writer.write('<');
		writer.write(tag);
	}

	/**
	 * Write start tag closing mark. Write tag closing mark; if element is empty close the element also, e.g. <em>/&gt;</em>
	 * 
	 * @param empty true if element is empty.
	 * @throws IOException if underlying writer fails to write.
	 */
	private void writeClosingMark(boolean empty) throws IOException {
		if (empty) {
			writer.write(' ');
			writer.write('/');
		}
		writer.write('>');
	}

	/**
	 * Write end tag for non-empty elements. At this point element start tag and its content is already wrote to underlying
	 * writer and just need to close it with, e.g. <em>&lt/div&gt;</em>.
	 * 
	 * @param tag tag name
	 * @throws IOException if underlying writer fails to write.
	 */
	private void writeEndTag(String tag) throws IOException {
		writer.write('<');
		writer.write('/');
		writer.write(tag);
		writer.write('>');
	}

	/**
	 * Traverse all element's attributes and delegates {@link #writeAttribute(String, String)}. Skip operators if
	 * {@link #enableOperatorsSerialization} is false. This method is called after element start tag was opened.
	 * 
	 * @param element element whose attributes to write.
	 * @throws IOException if underlying writer fails to write.
	 */
	private void writeAttributes(Element element, Iterable<Attr> attributes) throws IOException {
		for (Attr attr : attributes) {
			final String attrName = attr.getName();
			if (!enableOperatorsSerialization && Opcode.fromAttrName(attrName) != Opcode.NONE) {
				// skip operator attributes if operators serialization is disabled
				continue;
			}
			final String attrValue = attr.getValue();
			if (attrValue.isEmpty()) {
				// do not write the attribute if its value is empty
				continue;
			}
			if (attrValue.equals(HTML.DEFAULT_ATTRS.get(attrName))) {
				// do not write the attribute if it is a default one with a default value
				continue;
			}
			writeAttribute(attrName, attrValue);
		}
	}

	/**
	 * Collect all element attributes less CSS class. This method collect all attributes from given <code>element</code> but
	 * skip the <code>class</code> attribute to allows for operator handling. Anyway, if CSS_CLASS operator is missing static
	 * CSS class is still included in order to preserve element class.
	 * 
	 * @param element element to scan for attributes.
	 * @return non CSS class element attributes.
	 */
	private static Set<Attr> collectAttributes(Element element, boolean includeCssClass) {
		Set<Attr> attributes = new HashSet<Attr>();
		Attr cssClass = null;
		boolean foundDataCssClass = false;

		for (Attr attr : element.getAttrs()) {
			final AttrImpl attrImpl = new AttrImpl(attr);
			if (includeCssClass) {
				attributes.add(attrImpl);
				continue;
			}

			// logic for variant with CSS class exclusion
			if (!"class".equalsIgnoreCase(attr.getName())) {
				attributes.add(attrImpl);
			} else {
				cssClass = attrImpl;
			}
			if ("data-css-class".equalsIgnoreCase(attr.getName())) {
				foundDataCssClass = true;
			}
		}

		if (!foundDataCssClass && cssClass != null) {
			// takes care to preserve static CSS class if CSS_CLASS operator is not present
			attributes.add(cssClass);
		}
		return attributes;
	}

	/**
	 * Helper method to add an attribute to a set.
	 * 
	 * @param element attribute to add owning element, for debugging,
	 * @param attributes set of attributes,
	 * @param value attribute value to add.
	 */
	private static void addAttribute(Element element, Set<Attr> attributes, Object value) {
		Attr attr = (Attr) value;
		if (!attributes.add(attr)) {
			throw new TemplateException("Invalid element |%s|. It has both static attribute |%s| and attribute operator.", element, attr.getName());
		}
	}
}
