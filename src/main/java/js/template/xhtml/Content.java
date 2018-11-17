package js.template.xhtml;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import js.converter.Converter;
import js.converter.ConverterException;
import js.converter.ConverterRegistry;
import js.format.Format;
import js.lang.BugError;
import js.log.Log;
import js.log.LogFactory;
import js.template.TemplateException;
import js.util.Classes;
import js.util.Params;
import js.util.Strings;
import js.util.Types;

/**
 * Model object adapter. Templates engine operates upon application model used as source for dynamic content. But model
 * object is, and must be, business oriented and is quite possible to exist documents or views with needs not covered
 * directly by model. This class is used exactly for that: it is an adapter for model used to synthesize properties not
 * directly supplied by it. So, strictly speaking, templates engine does not directly acts upon model object but on this
 * content adapter.
 * <p>
 * This class supplies getters for miscellaneous types: string, object, list, array, map but all getters uses
 * {@link #getValue(Object, String)} which on its turn retrieves fields from model. Now is the interesting part: if
 * model field is missing value getter uses this class instance with a getter derived from searched property name. And
 * since this class does not offer per se any special getter it relies on subclasses.
 * 
 * <pre>
 *  class Person {
 *      int id;
 *  }
 * 
 *  class PersonContent extends Content {
 *      String getLink() {
 *          return &quot;person-view.xsp?id&quot; + model.getId();
 *      }
 *  }
 * 
 *  &lt;a data-href="link" /&gt;
 * </pre>
 * 
 * In above snippet we have a link with <em>href</em> operator having <em>link</em> as operand, in this case a property
 * path. Operator requests from content instance a property with name <em>link</em>. Content adapter searches person
 * instance, the model in this case, and on no such field create a getter with the name <em>getLink</em> and use it
 * against content instance, this time with success.
 * <p>
 * Content adapter does use <em>property path</em> abstraction to denote a specific content value. This name hides two
 * concepts: property and path.
 * <ul>
 * <li>Property is used to designates a value inside an object. Is is more generic than object field since it covers
 * content synthetic getters too, as shown above. Also a property name can be a numeric index, if object instance is an
 * array or list. This way both object and array like instances are accessible with the same property abstraction:
 * 
 * <pre>
 * content.getValue(object, &quot;id&quot;); // here object is an Object instance with a field named &quot;id&quot;
 * content.getValue(object, &quot;2&quot;); // object is an array or list and &quot;2&quot; is the index
 * </pre>
 * 
 * <li>In this class acception an object is a graph of values and property path is simply a list of path components
 * separated by dots. For example, in below snippet the property path <em>car.wheels.1.manufacturer</em> designates the
 * manufacturer of the second wheel from car while <em>car.model</em> designates, you guess, car model.
 * 
 * <pre>
 *  class Car {
 *      String model;
 *      Wheel[4] wheels;
 *  }
 *  class Wheel {
 *      String manufacturer;
 *  }
 * </pre>
 * 
 * Property path can be absolute, when begin with dot, or relative. An absolute path refers to content adapter root,
 * that is, the wrapped model. A relative one uses a scope object; this scope object is always present where property
 * path is present too and there are operators that change this scope object.
 * </ul>
 * 
 * @author Iulian Rotaru
 */
public class Content
{
  /** Class logger. */
  private static final Log log = LogFactory.getLog(Content.class);

  /** j(s)-script specific date format. */
  private static final ThreadLocal<DateFormat> scriptDateFormat = new ThreadLocal<DateFormat>();

  /** j(s)-script specific number format. */
  private static final ThreadLocal<NumberFormat> scriptNumberFormat = new ThreadLocal<NumberFormat>();

  /**
   * Model object. This value holds application specific dynamic content and is the root of content adapter property
   * paths.
   */
  protected Object model;

  /**
   * Construct content instance.
   * 
   * @param model model object.
   * @throws IllegalArgumentException if <code>model</code> parameter is null.
   */
  public Content(Object model) throws IllegalArgumentException
  {
    Params.notNull(model, "Model");
    this.model = model;
  }

  /**
   * Get application model object.
   * 
   * @return application model object.
   */
  Object getModel()
  {
    assert this.model != null;
    return this.model;
  }

  /**
   * Retrieve content object. Delegates {@link #getValue(Object, String)} to obtain the requested value. If value is
   * null warn the event; in any case return value.
   * 
   * @param scope scope object,
   * @param propertyPath object property path.
   * @return content object or null.
   * @throws TemplateException if requested value is undefined.
   */
  Object getObject(Object scope, String propertyPath) throws TemplateException
  {
    Object object = getValue(scope, propertyPath);
    if(object == null) {
      warn(scope.getClass(), propertyPath);
    }
    return object;
  }

  /**
   * Retrieve content array like instance as iterable. Delegates {@link #getValue(Object, String)} to obtain the
   * requested value. If value is null warn the event and return empty list. I value is array like return it as
   * iterable, otherwise throws content exception.
   * 
   * @param scope scope object,
   * @param propertyPath object property path.
   * @return array like instance, possible empty, converted to iterable.
   * @throws TemplateException if requested value is undefined or is not an {@link Types#isArrayLike(Object) an array
   *           like}.
   */
  Iterable<?> getIterable(Object scope, String propertyPath) throws TemplateException
  {
    Object value = getValue(scope, propertyPath);
    if(value == null) {
      warn(scope.getClass(), propertyPath);
      return Collections.EMPTY_LIST;
    }
    if(!Types.isArrayLike(value)) {
      throw new TemplateException("Invalid type. Expected list but got |%s|.", value.getClass());
    }
    return Types.asIterable(value);
  }

  /**
   * Retrieve content map instance. Delegates {@link #getValue(Object, String)} to obtain the requested value. If value
   * is null warn the event an return empty map. If value is map return it, otherwise throws context exception.
   * 
   * @param scope scope object,
   * @param propertyPath object property path.
   * @return map instance, possible empty.
   * @throws TemplateException if requested value is undefined or not a {@link Types#isMap(Object) map}.
   */
  Map<?, ?> getMap(Object scope, String propertyPath) throws TemplateException
  {
    Object map = getValue(scope, propertyPath);
    if(map == null) {
      warn(scope.getClass(), propertyPath);
      return Collections.EMPTY_MAP;
    }
    if(!Types.isMap(map)) {
      throw new TemplateException("Invalid type. Expected map but got |%s|.", map.getClass());
    }
    return (Map<?, ?>)map;
  }

  /**
   * Test if value is empty. Delegates {@link #getValue(Object, String)} and returns true if found value fulfill one of
   * the next conditions:
   * <ul>
   * <li>null
   * <li>boolean false
   * <li>number equals with 0
   * <li>empty string
   * <li>array of length 0
   * <li>collection of size 0
   * <li>map of size 0
   * <li>undefined character
   * </ul>
   * Note that undefined value, that is, field or method getter not found, is not considered empty value.
   * 
   * @param scope scope object,
   * @param propertyPath property path.
   * @return true if value is empty.
   * @throws TemplateException if requested value is undefined.
   */
  boolean isEmpty(Object scope, String propertyPath) throws TemplateException
  {
    return Types.asBoolean(getValue(scope, propertyPath)) == false;
  }

  /**
   * Convenient method to call {@link #getString(Object, String, Format)} with null formatter.
   * 
   * @param scope object scope,
   * @param propertyPath object property path.
   * @return content value as string, possible null.
   * @throws TemplateException if requested value is undefined.
   */
  String getString(Object scope, String propertyPath) throws TemplateException
  {
    return getString(scope, propertyPath, null);
  }

  /**
   * Get value converted to string. Delegates {@link #getValue(Object, String)} to obtain the requested value; if is
   * null log warning and returns null value. Now, value can be of any type and need to be converted to string, as
   * follow:
   * <ul>
   * <li>if format is not null returns its invocation result,
   * <li>at this point value should be a {@link Types#isPrimitiveLike(Object) sudo-primitive} or have converter,
   * otherwise fatal fail
   * <li>if value is a string just return it,
   * <li>if this template is HTML and value is boolean, number or date returns j(s)-script specific format,
   * <li>try to use {@link Converter}, throwing templates exception if fail.
   * </ul>
   * 
   * @param scope scope object,
   * @param propertyPath object property path,
   * @param format formatter instance, possible null.
   * @return requested value as string or null.
   * @throws TemplateException if value not found or found but cannot convert it to string.
   * @throws ConverterException if value serialization fails.
   */
  String getString(Object scope, String propertyPath, Format format) throws TemplateException, ConverterException
  {
    Object value = getValue(scope, propertyPath);
    if(value == null) {
      warn(scope.getClass(), propertyPath);
      return null;
    }

    if(format != null) {
      return format.format(value);
    }
    if(!Types.isPrimitiveLike(value) && !ConverterRegistry.hasType(value.getClass())) {
      throw new TemplateException("Value |%s#%s| should be a primitive like but is |%s|.", scope.getClass(), propertyPath, value.getClass());
    }
    if(value instanceof String) return (String)value;

    // booleans, numbers and date should yield the same result as script engine counterpart
    if(Types.isBoolean(value)) {
      return value.toString().toLowerCase();
    }
    if(Types.isNumber(value)) {
      NumberFormat nf = scriptNumberFormat.get();
      if(nf == null) {
        nf = NumberFormat.getNumberInstance(Locale.getDefault());
        nf.setGroupingUsed(false);
        scriptNumberFormat.set(nf);
      }
      return nf.format(value);
    }
    if(Types.isDate(value)) {
      DateFormat df = scriptDateFormat.get();
      if(df == null) {
        df = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss z", Locale.getDefault());
        df.setTimeZone(TimeZone.getDefault());
        scriptDateFormat.set(df);
      }
      return df.format(value);
    }

    return ConverterRegistry.getConverter().asString(value);
  }

  /**
   * Get content value. This method consider object as a graph of values and property path as a list of path components
   * separated by dots and uses next logic to retrieve requested value:
   * <ul>
   * <li>if property path is anonymous, i.e. is exactly ".", returns given object itself,
   * <li>if property path is absolute, starts with ".", uses content root object and transform the path as relative,
   * <li>split property path and traverse all path components returning last found object.
   * </ul>
   * Value can be about anything: primitives or aggregates. Anyway, there is distinction between not found value and a
   * null one. First condition is known as undefined value and throws content exception; null value denotes an existing
   * one but not initialized. Finally, this method uses {@link #getObjectProperty(Object, String)} to actually process
   * path components in sequence.
   * 
   * @param object instance to use if property path is relative,
   * @param propertyPath object property path.
   * @return requested content value or null.
   * @throws TemplateException if requested value is undefined.
   */
  private Object getValue(Object object, String propertyPath) throws TemplateException
  {
    if(this.model == null) {
      return null;
    }

    // anonymous property path has only a dot
    if(propertyPath.equals(".")) {
      return object;
    }

    Object o = object;
    if(propertyPath.charAt(0) == '.') {
      o = this.model;
      propertyPath = propertyPath.substring(1);
    }
    for(String property : propertyPath.split("\\.")) {
      o = getObjectProperty(o, property);
      if(o == null) {
        return null;
      }
    }
    return o;
  }

  /**
   * Get object property. This helper method is the work horse of all content getters. An object property is not limited
   * to object field; it also includes array and list items, content instance getters and super-classes, as follow:
   * <ul>
   * <li>if object is instance of array or list property name should be a numeric value used as index,
   * <li>try to get given object instance field with requested property name and return its value,
   * <li>if no such field consider this content instance and try a getter with the property name,
   * <li>if no such method delegates {@link #getContentObject(Object, String)},
   * <li>if field is still null throws content exception.
   * </ul>
   * 
   * @param object instance to retrieve property from,
   * @param property property name.
   * @return requested object property or null.
   * @throws IllegalArgumentException if any of object or property name arguments is null.
   * @throws TemplateException if property not found.
   */
  private Object getObjectProperty(Object object, String property) throws IllegalArgumentException, TemplateException
  {
    Params.notNull(object, "Object");
    Params.notNull(property, "Property");

    if(object.getClass().isArray()) {
      try {
        int index = Integer.parseInt(property);
        return Array.get(object, index);
      }
      catch(NumberFormatException unused) {
        throw new TemplateException("Invalid property on |%s|. Expect numeric used as index but got |%s|.", object.getClass(), property);
      }
    }

    if(object instanceof List<?>) {
      try {
        List<?> list = (List<?>)object;
        int index = Integer.parseInt(property);
        return list.get(index);
      }
      catch(NumberFormatException unused) {
        throw new TemplateException("Invalid property on |%s|. Expect numeric used as index but got |%s|.", object.getClass(), property);
      }
    }

    // try to load field value from object hierarchy and if not found make a second attempt using content getter
    // if both fails throw exception
    // next logic uses exception for normal flow control but i do not see reasonable alternative
    try {
      return Classes.getFieldEx(object.getClass(), Strings.toMemberName(property)).get(object);
    }
    catch(NoSuchFieldException expectedMissingField) {
      return getContentObject(object, property);
    }
    catch(Exception unexpected) {
      throw new BugError(unexpected);
    }
  }

  /**
   * Return object property value using content getter. By convention accessor name is <code>get</code> concatenated
   * with title case property name. If content has not such method throws content exception.
   * 
   * @param object content scope object,
   * @param property the name of property to retrieve.
   * @return property value.
   * @throws TemplateException if property getter not found.
   */
  private Object getContentObject(Object object, String property) throws TemplateException
  {
    String getterName = Strings.getMethodAccessor("get", property);
    try {
      Method getter = this.getClass().getDeclaredMethod(getterName, object.getClass());
      getter.setAccessible(true);
      return getter.invoke(this, object);
    }
    catch(NoSuchMethodException expectedMissingGetter) {
      throw new TemplateException("Missing property |%s| from object |%s|.", property, object.getClass());
    }
    catch(Exception unexpected) {
      throw new BugError(unexpected);
    }
  }

  /**
   * Record warning message to class logger.
   * 
   * @param scope scope object,
   * @param propertyPath object property path.
   */
  private static void warn(Object scope, String propertyPath)
  {
    if(scope == null) {
      log.warn("Null object scope while searching for property |%s|.", propertyPath);
    }
    else {
      log.warn("Null value for |%s#%s|.", scope.getClass(), propertyPath);
    }
  }
}
