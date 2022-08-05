package com.jslib.template.xhtml;

import java.util.Map;
import java.util.TreeMap;

import com.jslib.api.template.TemplateException;

/**
 * Operator factory. There is a single operator factory on templates engine instance. It holds a pool of operator objects that
 * are reused in the context of templates engine instance. Throws templates exception if operator is not implemented.
 * 
 * @author Iulian Rotaru
 */
final class OperatorFactory {
	/**
	 * Operator instances pool. Operator instances are created on factory construction and reused in the scope of templates
	 * engine instance.
	 */
	private Map<Opcode, Operator> operators = new TreeMap<Opcode, Operator>();

	/**
	 * Construct operator factory instance. Initialize operators pool. Depending on every operator needs may pass parent
	 * serializer instance and/or dynamic content reference as operator constructor arguments.
	 * 
	 * @param serializer parent serializer instance,
	 * @param content dynamic content reference.
	 */
	OperatorFactory(Serializer serializer, Content content) {
		this.operators.put(Opcode.IF, new IfOperator(content));
		// this.operators.put(Opcode.IFNOT, new IfNotOperator(content));
		// this.operators.put(Opcode.CASE, new CaseOperator(content));
		this.operators.put(Opcode.EXCLUDE, new ExcludeOperator());
		this.operators.put(Opcode.ATTR, new AttrOperator(content));
		this.operators.put(Opcode.CSS_CLASS, new CssClassOperator(serializer, content));
		this.operators.put(Opcode.ID, new IdOperator(content));
		this.operators.put(Opcode.SRC, new SrcOperator(content));
		this.operators.put(Opcode.HREF, new HrefOperator(content));
		this.operators.put(Opcode.TITLE, new TitleOperator(content));
		this.operators.put(Opcode.VALUE, new ValueOperator(content));
		this.operators.put(Opcode.TEXT, new TextOperator(serializer, content));
		this.operators.put(Opcode.HTML, new HtmlOperator(serializer, content));
		this.operators.put(Opcode.OBJECT, new ObjectOperator(content));
		this.operators.put(Opcode.LIST, new ListOperator(serializer, content));
		this.operators.put(Opcode.OLIST, new OListOperator(serializer, content));
		this.operators.put(Opcode.MAP, new MapOperator(serializer, content));
		this.operators.put(Opcode.OMAP, new OMapOperator(serializer, content));
		this.operators.put(Opcode.FORMAT, new FormatOperator());
		this.operators.put(Opcode.NUMBERING, new NumberingOperator(serializer));
	}

	/**
	 * Get operator instance for requested opcode.
	 * 
	 * @param opcode requested operator opcode.
	 * @return operator instance.
	 * @throws TemplateException if operator is not implemented.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Operator> T geInstance(Opcode opcode) {
		Operator operator = this.operators.get(opcode);
		if (operator == null) {
			throw new TemplateException("Operator |%s| is not implemented.", opcode);
		}
		return (T) operator;
	}
}
