package js.template.xhtml;

import js.converter.ConverterRegistry;
import js.dom.Element;
import js.template.TemplateException;
import js.template.xhtml.Opcode.Type;

/**
 * Set element descendants to object properties. Being below snippet, templates engine loads person instance from content,
 * update h2 element text content to person name and img source to person picture.
 * 
 * <pre>
 *  &lt;section data-object="person"&gt;
 *      &lt;h1 data-text="name"&gt;&lt;/h1&gt;
 *      &lt;img data-src="picture" /&gt;
 *  &lt;/section&gt;
 *  
 *  class Person {
 *      String name;
 *      Picture picture;
 *  }
 * </pre>
 * 
 * This operator's operand is a property path designating the object which properties are to be injected. One may notice h1 and
 * img elements operators uses property paths relative to person instance. This is because templates engine temporarily changes
 * scope object to person instance while processing element's descendants. Anyway, keep in mind that only descendant's are
 * processed in the person scope; if defining element has conditional or attribute operators they are evaluated in currently
 * existing scope. In below sample, section title is set to parent not child name.
 * 
 * <pre>
 *  &lt;section data-object="child" data-title="name"&gt;
 *      &lt;h1 data-text="name"&gt;&lt;/h1&gt;
 *  &lt;/section&gt;
 * 
 *  class Parent {
 *      String name;
 *      Child child;
 *  }
 *  class Child {
 *      String name;
 *  }
 * </pre>
 * 
 * Note that this operator belongs to {@link Type#CONTENT} group and only one content operator is allowed per element. See the
 * {@link Type#CONTENT the list} of mutually excluding content operators.
 * 
 * @author Iulian Rotaru
 */
final class ObjectOperator extends Operator {
	/** Dynamic content reference. */
	private Content content;

	/**
	 * Construct OBJECT operator instance.
	 * 
	 * @param content dynamic content.
	 */
	ObjectOperator(Content content) {
		this.content = content;
	}

	/**
	 * Execute object operator. This operator just returns the new object scope to be used by templates engine. Throws content
	 * exception if given property path does not designate an existing object. If requested object is null warn the event and
	 * return the null value. Templates engine consider returned null as fully processed branch signal.
	 * 
	 * @param element context element,
	 * @param scope scope object,
	 * @param propertyPath object property path.
	 * @return new scope object or null.
	 * @throws TemplateException if given property path does not designate an existing object.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String propertyPath, Object... arguments) throws TemplateException {
		if (!(propertyPath.equals(".") || isStrictObject(scope))) {
			throw new TemplateException("OBJECT operator on element |%s| requires object scope but got value type |%s|.", element, scope.getClass());
		}
		Object value = content.getObject(scope, propertyPath);
		if (value == null) {
			log.warn("Null scope for property |%s| on element |%s|.", propertyPath, element);
		} else if (!(propertyPath.equals(".") || isStrictObject(value))) {
			throw new TemplateException(propertyPath, "Invalid content type. Expected strict object but got |%s|.", value.getClass());
		}
		return value;
	}

	/**
	 * An object is <em>strict object</em> if is not value type, that is, there is no converter for it.
	 * 
	 * @param object object instance to check.
	 * @return true if <code>object</code> is <em>strict object</em>.
	 */
	private boolean isStrictObject(Object object) {
		return !ConverterRegistry.hasType(object.getClass());
	}
}
