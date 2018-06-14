package js.template.xhtml;

import java.util.ArrayList;
import java.util.List;

import js.dom.Attr;
import js.dom.Element;
import js.lang.BugError;
import js.template.TemplateException;

/**
 * Templates element operators list. Scan templates element attributes, looking for operators syntax and store {@link Meta
 * meta-data} about operators found. There are semantic restrictions on element operators list, see below; this class takes care
 * to enforce them and throws templates exception if semantic restriction is broken. Operators are grouped into types, see
 * {@link Opcode.Type} and this class stores meta-data using the same structure.
 * 
 * <h4>Operators list constrains</h4>
 * <ul>
 * <li>at most one conditional operator, see {@link Opcode.Type#CONDITIONAL} for a list of mutually excluding conditional
 * operators,
 * <li>at most one content operator, see {@link Opcode.Type#CONTENT} for a list of mutually excluding content operators,
 * <li>at most one formatter instance declared; this constrain is actually enforced by XML syntax that does not allow for
 * multiple attributes with the same name,
 * <li>format instance mandates {@link TextOperator} or {@link ValueOperator} presence.
 * </ul>
 * 
 * @author Iulian Rotaru
 */
final class OperatorsList {
	/** Conditional operator meta-data. */
	private Meta conditionalOperator;

	/** Formatting instance meta-data. */
	private Meta formattingOperator;

	/** Content operator meta-data. */
	private Meta contentOperator;

	/** Attribute operators meta-data list. */
	private List<Meta> attributeOperators = new ArrayList<Meta>();

	/**
	 * Construct operators list instance. Scan element attributes looking for operators syntax and initialize internal
	 * meta-data.
	 * 
	 * @param element element to scan for operators.
	 * @throws TemplateException if found operator with empty operand or if semantic restriction is broken.
	 */
	OperatorsList(Element element) {
		for (Attr attr : element.getAttrs()) {
			Opcode opcode = Opcode.fromAttrName(attr.getName());
			if (opcode == Opcode.NONE) {
				continue;
			}

			final String attrValue = attr.getValue();
			if (attrValue.isEmpty()) {
				throw new TemplateException("Empty operand on element |%s|.", element);
			}

			Meta meta = new Meta(opcode, attrValue);
			switch (opcode.type()) {
			case CONDITIONAL:
				insanityCheck(element, this.conditionalOperator, opcode.type());
				this.conditionalOperator = meta;
				break;
			case FORMATTING:
				insanityCheck(element, this.formattingOperator, opcode.type());
				this.formattingOperator = meta;
				break;
			case CONTENT:
				insanityCheck(element, this.contentOperator, opcode.type());
				this.contentOperator = meta;
				break;
			case ATTRIBUTE:
				this.attributeOperators.add(meta);
				break;
			default:
				throw new BugError("Invalid opcode type |%s|.", opcode.type());
			}
		}
	}

	/**
	 * Specialized constructor for elements that are items in a list or map.
	 * 
	 * @param element element to scan for operators,
	 * @param isItem mark parameter for specialized constructor.
	 * @throws TemplateException if found operator with empty operand or if semantic restriction is broken.
	 */
	OperatorsList(Element element, boolean isItem) {
		this(element);
		if (contentOperator == null) {
			Opcode opcode = element.hasChildren() ? js.template.xhtml.Opcode.OBJECT : js.template.xhtml.Opcode.TEXT;
			contentOperator = new Meta(opcode, ".");
		}
	}

	/**
	 * Throws template exception if operator of given type is already declared.
	 * 
	 * @param element context element,
	 * @param meta operator meta-data,
	 * @param type operator type.
	 * @throws TemplateException if operator of given type is already declared.
	 */
	private void insanityCheck(Element element, Meta meta, Opcode.Type type) {
		if (meta != null) {
			throw new TemplateException("Invalid operators list on element |%s|. Only one %s operator is allowed.", element, type);
		}
	}

	/**
	 * Return true if this operators list contains a conditional operator.
	 * 
	 * @return true if conditional operator is present.
	 */
	boolean hasConditionalOperator() {
		return conditionalOperator != null;
	}

	/**
	 * Get conditional operator meta-data.
	 * 
	 * @return conditional operator meta-data.
	 */
	Meta getConditionalOperatorMeta() {
		return conditionalOperator;
	}

	/**
	 * Return true if this operators list contains a formatting operator.
	 * 
	 * @return true if formatting operator is present.
	 */
	boolean hasFormattingOperator() {
		return formattingOperator != null;
	}

	/**
	 * Get formatting operator meta-data.
	 * 
	 * @return formatting operator meta-data.
	 */
	Meta getFormattingOperatorMeta() {
		return formattingOperator;
	}

	/**
	 * Return true if this operators list contains a content operator.
	 * 
	 * @return true if content operators is present.
	 */
	boolean hasContentOperator() {
		return contentOperator != null;
	}

	/**
	 * Get content operator meta-data.
	 * 
	 * @return content operator meta-data.
	 */
	Meta getContentOperatorMeta() {
		return contentOperator;
	}

	/**
	 * Get attribute operators meta-data list, possible empty.
	 * 
	 * @return attribute operators meta-data.
	 */
	List<Meta> getAttributeOperatorsMeta() {
		return attributeOperators;
	}

	/**
	 * Operator meta-data extracted from template's element.
	 * 
	 * @author Iulian Rotaru
	 */
	static final class Meta {
		/** Operator opcode. */
		Opcode opcode;

		/** Operator operand. */
		String operand;

		/**
		 * Construct operator meta instance.
		 * 
		 * @param opcode operator opcode,
		 * @param operand operator operand.
		 */
		private Meta(Opcode opcode, String operand) {
			this.opcode = opcode;
			this.operand = operand;
		}
	}
}
