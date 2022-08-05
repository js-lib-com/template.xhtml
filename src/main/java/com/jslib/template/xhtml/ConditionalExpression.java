package com.jslib.template.xhtml;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jslib.api.template.TemplateException;
import com.jslib.converter.Converter;
import com.jslib.converter.ConverterRegistry;
import com.jslib.lang.BugError;
import com.jslib.util.Types;

/**
 * Conditional expression evaluator. A conditional expression has a mandatory property path, an operator opcode and an operand.
 * Property path is used to get content value and opcode to enact specific evaluation logic. Evaluation process usually uses two
 * parameters: content value determined by property path and operand from expression.
 * <p>
 * Usage pattern is straightforward: create instance and test expression value. Constructor need templates content and current scope
 * used to get value to be evaluated and of course expression.
 * 
 * <pre>
 * ConditionalExpression conditionalExpression = new ConditionalExpression(content, scope, expression);
 * if(conditionalExpression.value()) {
 *   // logic executed if conditional expression is true
 * }
 * </pre>
 * 
 * <h4>Conditional Expression Syntax</h4>
 * <pre>
 *  conditional-expression = [ not ] property-path [ opcode operand ]
 *  not = '!' ; ! prefix at expression start negate boolean result
 *  property-path = java-name
 *  opcode = '=' / '<' / '>' ; note that opcode cannot be any valid java-name character
 *  java-name = ( 'a'...'z' / '$' / '_' ) *( 'a'...'z' / 'A'...'Z' / '0'...'9' / '$' / '_' )
 * </pre>
 *
 * Here are couple example of working conditional expressions and parsed components. Although only one negated expression is presented please note
 * that exclamation market prefix can pe used with any operator.
 * <p> 
 * <table border="1" style="border-collapse:collapse;">
 * <tr>
 * <td><b>Expression
 * <td><b>True Condition
 * <td><b>Opcode
 * <td><b>Property Path
 * <td><b>Operand
 * <tr>
 * <td>flag
 * <td><code>flag</code> is a boolean value and is <code>true</code>
 * <td>NOT_EMPTY
 * <td>flag
 * <td>null
 * <tr>
 * <td>!description
 * <td><code>description</code> is a string and is null
 * <td>NOT_EMPTY
 * <td>description
 * <td>null
 * <tr>
 * <td>type=DIRECTORY
 * <td><code>type</code> is an enumeration and its values is <code>DIRECTORY</code>
 * <td>EQUALS
 * <td>type
 * <td>DIRECTORY
 * <tr>
 * <td>loadedPercent<0.9
 * <td><code>loadedPercent</code> is a double value [0, 1) and its values is less than 0.9
 * <td>LESS_THAN
 * <td>loadedPercent
 * <td>0.9
 * <tr>
 * <td>birthDay>1980-01-01T00:00:00Z
 * <td><code>birthDay</code> is a date and its value is after 1980, January 1-st
 * <td>GREATER_THAN
 * <td>birthDay
 * <td>1980-01-01T00:00:00Z
 * </table>
 * <p>
 * See {@link ConditionalExpression.Opcode} for supported operators.
 * @author Iulian Rotaru
 */
final class ConditionalExpression {
	/** Wrapped string source of this conditional expression, mainly for debugging. */
	private String expression;

	/**
	 * Evaluated value negation flag. If true {@link #evaluate(Object)} applies boolean <code>not</code> on returned value. This
	 * flag is true if expression starts with exclamation mark.
	 */
	private boolean not;

	/**
	 * The property path of the content value to evaluate this conditional expression against. See package API for object
	 * property path description. This value is extracted from given expression and is the only mandatory component.
	 */
	private String propertyPath;

	/**
	 * Optional expression operator opcode, default to {@link ConditionalExpression.Opcode#NOT_EMPTY}. This opcode is used to
	 * select the proper expression {@link Processor}.
	 */
	private Opcode opcode = Opcode.NONE;

	/**
	 * Operator operand, mandatory only if {@link Processor#acceptNullOperand()} requires it. This is the second term of
	 * expression evaluation logic; the first is the content value determined by property path.
	 */
	private String operand;

	/** Expression evaluation value. */
	private boolean value = false;

	/**
	 * Package default constructor.
	 *
	 * @param content dynamic content,
	 * @param scope current object scope,
	 * @param expression conditional expression to parse.
	 * @throws ContentException
	 */
	ConditionalExpression(Content content, Object scope, String expression) {
		this.expression = expression;
		parse();
		this.value = this.evaluate(content.getObject(scope, this.propertyPath));
	}

	/**
	 * Return this conditional expression boolean value.
	 * 
	 * @return this conditional expression value.
	 */
	public boolean value() {
		return this.value;
	}

	/**
	 * Parse conditional expression string and return the property path. This method is in fact a morphological parser, i.e. a
	 * lexer. It just identifies expression components and initialize internal state. Does not check validity; all
	 * <em>insanity</em> tests are performed by {@link #evaluate(Object)} counterpart. Returns <em>property path</em> expression
	 * component, which is in fact the only mandatory part.
	 */
	private void parse() {
		if (this.expression.charAt(0) == '!') {
			this.not = true;
			this.expression = this.expression.substring(1);
		}

		StringBuilder sb = new StringBuilder();
		State state = State.PROPERTY_PATH;

		for (int i = 0; i < this.expression.length(); ++i) {
			char c = this.expression.charAt(i);

			switch (state) {
			case PROPERTY_PATH:
				if (isPropertyPathChar(c)) {
					sb.append(c);
					break;
				}
				this.propertyPath = sb.toString();
				sb.setLength(0);
				this.opcode = Opcode.forChar(c);
				state = State.OPERAND;
				break;

			case OPERAND:
				sb.append(c);
				break;

			default:
				throw new IllegalStateException();
			}
		}

		if (state == State.PROPERTY_PATH) {
			assert this.opcode == Opcode.NONE;
			this.propertyPath = sb.toString();
			this.opcode = Opcode.NOT_EMPTY;
		} else {
			if (sb.length() > 0) {
				// operand string builder may be empty if operand is missing, e.g. 'value='
				this.operand = sb.toString();
			}
		}
	}

	private static boolean isPropertyPathChar(char c) {
		return c == '.' || Character.isJavaIdentifierPart(c);
	}

	/**
	 * Evaluate this conditional expression against given object value. Execute this conditional expression operator on given
	 * <code>object</code> value and {@link #operand} defined by expression. Evaluation is executed after {@link #parse()}
	 * counter part that already initialized this conditional expression internal state. This method takes care to test internal
	 * state consistency and throws templates exception if bad.
	 * 
	 * @param object value to evaluate.
	 * @return true if this conditional expression is positively evaluated.
	 * @throws TemplateException if this conditional expression internal state is not consistent.
	 */
	private boolean evaluate(Object object) {
		if (this.opcode == Opcode.INVALID) {
			throw new TemplateException("Invalid conditional expression |%s|. Not supported opcode.", this.expression);
		}
		Processor processor = getProcessor(opcode);

		if (this.operand == null && !processor.acceptNullOperand()) {
			throw new TemplateException("Invalid conditional expression |%s|. Missing mandatory operand for operator |%s|.", this.expression, this.opcode);
		}
		if (!processor.acceptValue(object)) {
			throw new TemplateException("Invalid conditional expression |%s|. Operator |%s| does not accept value type |%s|.", this.expression, this.opcode, object.getClass());
		}
		if (this.operand != null && !OperandFormatValidator.isValid(object, this.operand)) {
			throw new TemplateException("Invalid conditional expression |%s|. Operand does not match value type |%s|. See |%s| API.", this.expression, object.getClass(), OperandFormatValidator.class);
		}

		boolean value = processor.evaluate(object, this.operand);
		return this.not ? !value : value;
	}

	/**
	 * Parser state machine.
	 * 
	 * @author Iulian Rotaru
	 */
	private static enum State {
		/**
		 * Neutral value.
		 */
		NONE,

		/**
		 * Building property path.
		 */
		PROPERTY_PATH,

		/**
		 * Building operand.
		 */
		OPERAND
	}

	/**
	 * Operator opcodes supported by current conditional expression implementation. Operators always operates on content value
	 * identified by {@link ConditionalExpression#propertyPath} and optional {@link ConditionalExpression#operand}.
	 * 
	 * 
	 * @author Iulian Rotaru
	 */
	private static enum Opcode {
		/**
		 * Neutral value.
		 */
		NONE,

		/**
		 * Invalid character code. Parser uses this opcode when discover a not supported character code for opcode.
		 */
		INVALID,

		/**
		 * Value if not empty. A value is empty if is null, empty string, boolean false, zero value number, collection or array
		 * with zero size. It is implemented by {@link NotEmptyProcessor}.
		 */
		NOT_EMPTY,

		/**
		 * Value and operand are equal. It is implemented by {@link EqualsProcessor}.
		 */
		EQUALS,

		/**
		 * Value is strictly less than operand. It is implemented by {@link LessThanProcessor}.
		 */
		LESS_THAN,

		/**
		 * Value is strictly greater than operand. It is implemented by {@link GreaterThanProcessor}.
		 */
		GREATER_THAN;

		/**
		 * Returns the opcode encoded by given character code. Current implementation encode opcode with a single character. If
		 * given <code>code</code> is not supported returns {@link ConditionalExpression.Opcode#INVALID}.
		 * 
		 * @param code opcode character code.
		 * @return opcode specified by given <code>code</code> or INVALID.
		 */
		public static Opcode forChar(char code) {
			switch (code) {
			case '=':
				return EQUALS;
			case '<':
				return LESS_THAN;
			case '>':
				return GREATER_THAN;
			}
			return INVALID;
		}
	}

	/**
	 * Every conditional expression operator implements this processor interface. A processor implements the actual evaluation
	 * logic, see {@link #evaluate(Object, String)}. Evaluation always occurs on a content value designated by property path and
	 * an optional operand, both described by conditional expression. Value is always first and is important on order based
	 * operators, e.g. on LESS_THAN value should be less than operand.
	 * <p>
	 * Operand can miss in which case evaluation consider only the value, for example, NOT_EMPTY test value emptiness.
	 * <p>
	 * Processor interface provides also predicates to test if implementation supports null operand and if certain value is
	 * acceptable for processing.
	 * 
	 * @author Iulian Rotaru
	 */
	private static interface Processor {
		/**
		 * Apply evaluation specific logic to given value and optional operand.
		 * 
		 * @param value value to evaluate, possible null,
		 * @param operand optional operand to evaluate value against, default to null.
		 * @return evaluation logic result.
		 */
		boolean evaluate(Object value, String operand);

		/**
		 * Test if processor implementation accepts null operand. It is a templates exception if operator processor does not
		 * accept null operand and expression do not include it.
		 * 
		 * @return true if this processor accepts null operand.
		 */
		boolean acceptNullOperand();

		/**
		 * Test performed just before evaluation to determine if given value can be processed. Most common usage is to consider
		 * value type; for example LESS_THAN operator cannot handle boolean values.
		 * 
		 * @param value value to determine if processable.
		 * @return true if given <code>value</code> can be evaluated by this processor.
		 */
		boolean acceptValue(Object value);
	}

	/** NOT_EMPTY operator processor instance. */
	private static final Processor NOT_EPMTY_PROCESSOR = new NotEmptyProcessor();

	/** EQUALS operator processor instance. */
	private static Processor EQUALS_PROCESSOR;

	/** LESS_THAN operator processor instance. */
	private static Processor LESS_THAN_PROCESSOR;

	/** GREATER_THAN operator processor instance. */
	private static Processor GREATER_THAN_PROCESSOR;

	/**
	 * Operator processor factory. Returned processor instance is a singleton, that is, reused on running virtual machine.
	 * 
	 * @param opcode return processor suitable for requested operator.
	 * @return operator processor instance.
	 */
	private static Processor getProcessor(Opcode opcode) {
		switch (opcode) {
		case NOT_EMPTY:
			return NOT_EPMTY_PROCESSOR;

		case EQUALS:
			if (EQUALS_PROCESSOR == null) {
				EQUALS_PROCESSOR = new EqualsProcessor();
			}
			return EQUALS_PROCESSOR;

		case LESS_THAN:
			if (LESS_THAN_PROCESSOR == null) {
				LESS_THAN_PROCESSOR = new LessThanProcessor();
			}
			return LESS_THAN_PROCESSOR;

		case GREATER_THAN:
			if (GREATER_THAN_PROCESSOR == null) {
				GREATER_THAN_PROCESSOR = new GreaterThanProcessor();
			}
			return GREATER_THAN_PROCESSOR;

		default:
			throw new BugError("Unsupported opcode |%s|.", opcode);
		}
	}

	/**
	 * Operator processor for not empty value test.
	 * 
	 * @author Iulian Rotaru
	 */
	private static final class NotEmptyProcessor implements Processor {
		@Override
		public boolean evaluate(Object value, String operand) {
			return Types.asBoolean(value) == true;
		}

		@Override
		public boolean acceptNullOperand() {
			return true;
		}

		@Override
		public boolean acceptValue(Object value) {
			return true;
		}
	}

	/**
	 * Equality operator processor.
	 * 
	 * @author Iulian Rotaru
	 */
	private static final class EqualsProcessor implements Processor {
		/**
		 * Implements equality test logic. This method converts value to string using {@link Converter} then compare it with
		 * operand. As a consequence operand format must be compatible with value type. For example if value type is a
		 * {@link Date} operand syntax should be ISO8601; please see {@link Converter} documentation for supported formats.
		 */
		@Override
		public boolean evaluate(Object value, String operand) {
			if (value == null) {
				return operand.equals("null");
			}
			if (value instanceof Date) {
				return evaluateDates((Date) value, operand);
			}
			return ConverterRegistry.getConverter().asString(value).equals(operand);
		}

		private static boolean evaluateDates(Date date, String dateFormat) {
			int[] dateItems = Dates.dateItems(date);
			Matcher matcher = Dates.dateMatcher(dateFormat);
			for (int i = 0; i < dateItems.length; ++i) {
				String value = matcher.group(i + 1);
				if (value == null) {
					break;
				}
				if (dateItems[i] != Integer.parseInt(value)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean acceptNullOperand() {
			return false;
		}

		@Override
		public boolean acceptValue(Object vlaue) {
			return true;
		}
	}

	/**
	 * Base class for inequality comparisons.
	 * 
	 * @author Iulian Rotaru
	 */
	private static abstract class ComparisonProcessor implements Processor {
		@Override
		public boolean evaluate(Object value, String operand) {
			if (Types.isNumber(value)) {
				Converter converter = ConverterRegistry.getConverter();
				Double doubleValue = ((Number) value).doubleValue();
				Double doubleOperand = 0.0;
				if (value instanceof Float) {
					// converting float to double may change last decimal digits
					// we need to ensure both value and operand undergo the same treatment, i.e. use Float#doubleValue() method
					// for both
					// for example float number 1.23F is converted to double to 1.2300000190734863
					// if we convert string "1.23" to double we have 1.23 != 1.2300000190734863
					Float floatOperand = converter.asObject(operand, Float.class);
					doubleOperand = floatOperand.doubleValue();
				} else {
					doubleOperand = converter.asObject(operand, Double.class);
				}
				return compare(doubleValue, doubleOperand);
			}
			if (Types.isDate(value)) {
				Date dateValue = (Date) value;
				Date dateOperand = Dates.parse(operand);
				return compare(dateValue, dateOperand);
			}
			return false;
		}

		/**
		 * Comparator for numbers.
		 * 
		 * @param value numeric value,
		 * @param operand numeric operand.
		 * @return true if value and operand fulfill comparator criterion.
		 */
		protected abstract boolean compare(Double value, Double operand);

		/**
		 * Comparator for calendar dates.
		 * 
		 * @param value date value,
		 * @param operand date operand.
		 * @return true if value and operand fulfill comparator criterion.
		 */
		protected abstract boolean compare(Date value, Date operand);

		/**
		 * Comparison processors always require not null operand.
		 */
		@Override
		public boolean acceptNullOperand() {
			return false;
		}

		/**
		 * Current implementation of comparison processor accept numbers and dates.
		 */
		@Override
		public boolean acceptValue(Object value) {
			if (Types.isNumber(value)) {
				return true;
			}
			if (Types.isDate(value)) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Comparison processor implementation for <code>LESS_THAN</code> operator.
	 * 
	 * @author Iulian Rotaru
	 */
	private static class LessThanProcessor extends ComparisonProcessor {
		@Override
		protected boolean compare(Double value, Double operand) {
			return value < operand;
		}

		@Override
		protected boolean compare(Date value, Date operand) {
			return value.compareTo(operand) < 0;
		}
	}

	/**
	 * Comparison processor implementation for <code>GREATER_THAN</code> operator.
	 * 
	 * @author Iulian Rotaru
	 */
	private static final class GreaterThanProcessor extends ComparisonProcessor {
		@Override
		protected boolean compare(Double value, Double operand) {
			return value > operand;
		}

		@Override
		protected boolean compare(Date value, Date operand) {
			return value.compareTo(operand) > 0;
		}
	}

	/**
	 * Utility class for operand format validation. Operand is a string and has a specific format that should be compatible with
	 * value type. For example if value is a date operand should be ISO8601 date format. A null operand is not compatible with
	 * any value.
	 * <p>
	 * Current validator implementation recognizes boolean, number and date types. All other value types are not on scope of
	 * this validator and always return positive. For supported types see this class regular expression patterns.
	 * 
	 * @author Iulian Rotaru
	 */
	private static final class OperandFormatValidator {
		/**
		 * Date format should be ISO8601 with UTC time zone, <code>dddd-dd-ddTdd:dd:ddZ</code>.
		 */
		private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}(?:-\\d{2}(?:-\\d{2}(?:T\\d{2}(?::\\d{2}(?::\\d{2}(?:Z)?)?)?)?)?)?");

		/**
		 * Signed numeric decimal number but not scientific notation.
		 */
		private static final Pattern NUMBER_PATTERN = Pattern.compile("[+-]?\\d+(?:\\.\\d+)?");

		/**
		 * Boolean operand should be <code>true</code> or <code>false</code>, lower case.
		 */
		private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false");

		/**
		 * Private constructor.
		 */
		private OperandFormatValidator() {
		}

		/**
		 * Check operand format compatibility against value counterpart.
		 * 
		 * @param value content value to validate operand against,
		 * @param operand formatted string operand.
		 * @return true if <code>operand</code> format is compatible with requested <code>value</code>.
		 */
		public static boolean isValid(Object value, String operand) {
			if (operand == null) {
				// validation is enacted only for operator processors that do not accept null operand
				// so always return false
				return false;
			}
			if (Types.isBoolean(value)) {
				return BOOLEAN_PATTERN.matcher(operand).matches();
			}
			if (Types.isNumber(value)) {
				return NUMBER_PATTERN.matcher(operand).matches();
			}
			if (Types.isDate(value)) {
				return DATE_PATTERN.matcher(operand).matches();
			}
			return true;
		}
	}

	/**
	 * Utility class for dates related processing.
	 * 
	 * @author Iulian Rotaru
	 */
	private static final class Dates {
		/**
		 * ISO8601 date format pattern but with optional fields. Only year is mandatory. Optional fields should be in sequence;
		 * if an optional field is missing all others after it should also be missing.
		 */
		private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})(?:-(\\d{2})(?:-(\\d{2})(?:T(\\d{2})(?::(\\d{2})(?::(\\d{2})(?:Z)?)?)?)?)?)?");

		/**
		 * Private constructor.
		 */
		private Dates() {
		}

		/**
		 * Get a regular expression matcher for ISO8601 date format but with optional fields.
		 * 
		 * @param dateFormat date format to parse.
		 * @return matcher initialized with values from given <code>dateFormat</code>.
		 */
		public static Matcher dateMatcher(String dateFormat) {
			Matcher matcher = DATE_PATTERN.matcher(dateFormat);
			if (!matcher.matches()) {
				throw new IllegalStateException();
			}
			return matcher;
		}

		/**
		 * Parse ISO8601 formatted date but accept optional fields. For missing fields uses sensible default values, that is,
		 * minimum value specific to field. For example, default day of the month value is 1.
		 * 
		 * @param dateFormat date format to parse.
		 * @return date instance initialized from given <code>dateFormat</code>.
		 */
		public static Date parse(String dateFormat) {
			Matcher matcher = dateMatcher(dateFormat);

			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calendar.set(Calendar.YEAR, group(matcher, 1));
			calendar.set(Calendar.MONTH, group(matcher, 2));
			calendar.set(Calendar.DAY_OF_MONTH, group(matcher, 3));
			calendar.set(Calendar.HOUR_OF_DAY, group(matcher, 4));
			calendar.set(Calendar.MINUTE, group(matcher, 5));
			calendar.set(Calendar.SECOND, group(matcher, 6));

			return calendar.getTime();
		}

		/**
		 * Return normalized integer value for specified matcher group. If requested group value is null uses sensible default
		 * values. Takes care to return 0 for January and 1 for default day of the month; all other fields defaults to 0.
		 * 
		 * @param matcher regular expression matcher,
		 * @param group group number.
		 * @return integer value extracted from matcher or specific default value.
		 */
		private static int group(Matcher matcher, int group) {
			String value = matcher.group(group);
			if (group == 2) {
				// the second group is hard coded to month and should be normalized, January should be 0
				return parseInt(value, 1) - 1;
			}
			if (group == 3) {
				// the third group is hard coded to day of month and should default to 1
				return parseInt(value, 1);
			}
			// all other groups defaults to 0
			return parseInt(value, 0);
		}

		/**
		 * Return integer value from given numeric string or given default if value is null.
		 * 
		 * @param value numeric value, possible null,
		 * @param defaultValue default value used when value is null.
		 * @return integer value, parsed or default.
		 */
		private static int parseInt(String value, int defaultValue) {
			return value != null ? Integer.parseInt(value) : defaultValue;
		}

		/**
		 * Decompose date into its constituent items. Takes care to human normalize month value, that is, January is 1 not 0.
		 * 
		 * @param date date value.
		 * @return given <code>date</code> items.
		 */
		public static int[] dateItems(Date date) {
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			calendar.setTime(date);
			int[] items = new int[6];

			items[0] = calendar.get(Calendar.YEAR);
			items[1] = calendar.get(Calendar.MONTH) + 1;
			items[2] = calendar.get(Calendar.DAY_OF_MONTH);
			items[3] = calendar.get(Calendar.HOUR_OF_DAY);
			items[4] = calendar.get(Calendar.MINUTE);
			items[5] = calendar.get(Calendar.SECOND);

			return items;
		}

	}
}
