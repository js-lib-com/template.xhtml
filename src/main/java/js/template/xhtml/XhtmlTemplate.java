package js.template.xhtml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import js.converter.ConverterRegistry;
import js.dom.Document;
import js.log.Log;
import js.log.LogFactory;
import js.template.Template;
import js.util.Params;

/**
 * Templates serializer. This is the facade of the templates engine package. Using it is straightforward: get instance
 * using {@link #getInstance(ConverterRegistry, Document)} and call {@link #serialize(Writer, Object)}. Template
 * instance life cycle is controlled by this class. User code should always use factory method to get templates engine
 * instance and avoid caching and reusing it.
 * <p>
 * Templates engine depends on converter package for value object serialization. A value object is and object that can
 * be represented as a single value like file, URL or date. In order to avoid hard dependencies converter instance is
 * injected as parameter to factory method.
 * <p>
 * Templates engine serializes template document and executes embedded operator. Source template document is not changed
 * so it can be reused. This has important consequence: template files can be pre-loaded and parsed and kept in cache as
 * DOM document.
 * 
 * <pre>
 * ConverterRegistry converterManager = Factory.getInstance(ConverterRegistry.class);
 * . . .
 * Document document = Builder.parseHTML(...);
 * Template template = Template.getInstance(converterManager, document);
 * template.serialize(writer, dao.getModel());
 * </pre>
 * 
 * Note that serialization process behavior is not defined if writer is already closed or model is null.
 * 
 * @author Iulian Rotaru
 */
public final class XhtmlTemplate implements Template
{
  /** Class logger. */
  private static final Log log = LogFactory.getLog(XhtmlTemplate.class);

  /** Template document. */
  private final Document document;

  /**
   * Include XML prolog into serialization process, flag default to true. If document is (X)HTML, prolog is replaced by
   * DOCTYPE. This flag can be forced to false by {@link #suppressProlog()}.
   */
  private boolean serializeProlog;

  private boolean enableOperatorsSerialization;

  /**
   * Construct template engine instance.
   * 
   * @param document template document.
   */
  public XhtmlTemplate(Document document)
  {
    this.serializeProlog = true;
    this.document = document;
  }

  /**
   * Enable operators serialization. By default, templates operators are not included into resulting document. If one
   * may want to, perhaps in order to enable data extraction, uses this method. Be aware that if document is validated
   * operators syntax may collide with document grammar and render document invalid.
   */
  @Override
  public void setProperty(String name, Object value)
  {
    // TODO: test implementation; refine it
    if(name.equals("js.template.serialize.operator")) {
      if((Boolean)value) {
        enableOperatorsSerialization = true;
      }
    }
  }

  /**
   * Do not include XML prolog, respective X(H)MTL DOCTYPE, into serialization output.
   * 
   * @see #serializeProlog
   */
  public void suppressProlog()
  {
    serializeProlog = false;
  }

  /**
   * Serialize template with given dynamic content to a writer. Walk through template from its root and serialize every
   * node; if node contains operators execute them. Operators extract values from given dynamic content and process
   * them, result going to the same writer.
   * <p>
   * Writer parameter does not need to be {@link BufferedWriter}, but is acceptable. This interface implementation takes
   * care to initialize the writer accordingly. Given dynamic content can be an instance of {@link Content} or any POJO
   * containing values needed by template operators.
   * <p>
   * If any argument is null or writer is already closed this method behavior is not defined.
   * 
   * @param model domain model object to inject into template,
   * @param writer writer to serialize template to.
   */
  @Override
  public void serialize(Object model, Writer writer) throws IOException
  {
    Params.notNull(writer, "Writer");
    _serialize(writer, model);
  }

  @Override
  public String serialize(Object model)
  {
    Writer writer = new StringWriter();
    try {
      _serialize(writer, model);
    }
    catch(IOException e) {
      log.error(e);
    }
    // do not need to close string writer; excerpt from api-doc:
    // Closing a StringWriter has no effect.
    return writer.toString();
  }

  private void _serialize(Writer writer, Object model) throws IOException
  {
    if(model == null) {
      document.serialize(writer);
      return;
    }
    
    Serializer serializer = new Serializer();
    if(enableOperatorsSerialization) {
      serializer.enableOperatorsSerialization();
    }

    Content content = model instanceof Content ? (Content)model : new Content(model);
    serializer.setContent(content);
    serializer.setWriter(writer);

    if(serializeProlog) {
      if(document.isXML()) {
        serializer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
      }
      else {
        // if is not XML document should be HTML5
        serializer.write("<!DOCTYPE HTML>\r\n");
      }
    }
    if(document.getRoot() != null) {
      serializer.write(document.getRoot(), content.getModel());
    }
    serializer.flush();
  }
}
