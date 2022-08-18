package com.jslib.template.xhtml;

import java.util.HashMap;
import java.util.Map;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;
import com.jslib.converter.Converter;
import com.jslib.format.Format;
import com.jslib.lang.BugError;
import com.jslib.lang.NoSuchBeingException;
import com.jslib.util.Classes;
import com.jslib.util.Types;

/**
 * Initialize formatter instance. All values injected into document are strings. On the other hand, since content values can be
 * about any type there is the need to transform values into strings; templates engine uses {@link Converter} for this job.
 * Anyway, converter is designed for string (de)serialization and not for user interface. If resulting document is intended for
 * user audience there is the option to attach {@link Format} instances to elements. Element's operators may use this formatter
 * to convert content value before actual injection.
 * 
 * <pre>
 *  &lt;p data-text="birthday" data-format="js.format.FullDateTime"&gt;&lt;/p&gt;
 * </pre>
 * 
 * This operator operand is the formatter qualified class name. Pointing class should implement {@link Format} interface.
 * 
 * @author Iulian Rotaru
 */
final class FormatOperator extends Operator {
	/**
	 * Execute FORMAT operator. Returns requested formatter instance throwing templates exception if not found.
	 * 
	 * @param element context element, unused,
	 * @param scope scope object, unused,
	 * @param formatterName formatter qualified class name,
	 * @param arguments optional arguments, not used.
	 * @return format instance.
	 * @throws TemplateException if formatter class does not exist.
	 */
	@Override
	protected Object doExec(Element element, Object scope, String formatterName, Object... arguments) {
		Format format = getFormat(formatterName);
		if (format == null) {
			throw new TemplateException("Formatting class |%s| not found.", formatterName);
		}
		return format;
	}

	/**
	 * Formatters cache. Because formatters from this packages are not mandatory thread safe we use thread local storage to keep
	 * instances cache.
	 */
	private static final Map<String, ThreadLocal<Format>> classFormatters = new HashMap<String, ThreadLocal<Format>>();

	/**
	 * Return format instance usable to requested class.
	 * 
	 * @param className the name of class to retrieve formatter for.
	 * @return format for requested class.
	 */
	public static Format getFormat(String className) {
		assert className != null;
		if (className.isEmpty()) {
			return null;
		}

		ThreadLocal<Format> tlsFormatter = classFormatters.get(className);
		if (tlsFormatter == null) {
			try {
				// append formatter class name to cache only if exists and is of right type
				Classes.forName(className);
				tlsFormatter = new ThreadLocal<Format>();
				classFormatters.put(className, tlsFormatter);
			} catch (NoSuchBeingException e) {
				log.error("Formatter class |{java_type}| not found.", className);
				return null;
			} catch (ClassCastException e) {
				log.error("Invalid formatter class |{java_type}|. It should inherit from |{java_type}|.", className, Format.class);
				return null;
			}
		}

		Format formatter = tlsFormatter.get();
		if (formatter == null) {
			formatter = createFormatter(className);
			if (formatter == null) {
				// formatter can be invalid only if miss proper constructor and this is for sure a flaw in logic
				throw new BugError("Invalid formatter class |%s|. Proper constructor not found.", className);
			}
			tlsFormatter.set(formatter);
		}
		return formatter;
	}

	private static Format createFormatter(String className) {
		Class<? extends Format> formatterClass = Classes.forName(className);
		if (Types.isKindOf(formatterClass, Format.class)) {
			return Classes.newInstance(formatterClass);
		}
		return null;
	}
}
