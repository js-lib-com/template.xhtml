package com.jslib.template.xhtml;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import com.jslib.api.dom.Document;
import com.jslib.api.log.Log;
import com.jslib.api.log.LogFactory;
import com.jslib.api.template.Template;
import com.jslib.util.Params;

/**
 * Implementation for template interface. Basically this implementation has a reference to a template document -
 * injected at template instance creation and delegates {@link Serializer#write(js.dom.Element, Object)} for actual
 * template serialization.
 * <p>
 * Templates engine creates a new instance of this template class for every serialization but wrapped document instance
 * is reused. This implementation takes care to not alter template document.
 * 
 * @author Iulian Rotaru
 * @version final
 */
public final class XhtmlTemplate implements Template
{
  /** Class logger. */
  private static final Log log = LogFactory.getLog(XhtmlTemplate.class);

  /** Template name, unique per current JVM. This name is provided by external logic. */
  private final String name;

  /** Template document. */
  private final Document document;

  /**
   * Include XML prolog into serialization process, flag default to true. If document is (X)HTML, prolog is replaced by
   * DOCTYPE. This flag can be forced to false with property setter, e.g.
   * <code>setProperty("js.template.serialize.prolog", false)</code>.
   */
  private boolean serializeProlog;

  /**
   * By default operators are not included into serialized template. In order to enable operators serialization uses
   * property setter, e.g. <code>setProperty("js.template.serialize.operator", true)</code>.
   */
  private boolean serializeOperators;

  /**
   * Construct template engine instance.
   * 
   * @param name unique template name.
   * @param document reference to template document.
   */
  public XhtmlTemplate(String name, Document document)
  {
    this.name = name;
    this.document = document;
    this.serializeProlog = true;
    this.serializeOperators = false;
  }

  @Override
  public String getName()
  {
    return name;
  }

  /**
   * Set template instance properties overriding the template engine ones. Current implementation deals with
   * <code>js.template.serialize.prolog</code> and <code>js.template.serialize.operator</code> flags.
   * <p>
   * First is used to disable prolog serialization. By default prolog serialization is enabled, see
   * {@link #serializeProlog}. Use this property to disable it, that to not include XML prolog, respective X(H)MTL
   * DOCTYPE, into serialization output.
   * <p>
   * The second property is used to control operators serialization. By default, templates operators are not included
   * into resulting document. If one may want to include operators, perhaps in order to enable data extraction, uses
   * this method. But be warned that if document is validated operators syntax may collide with document grammar and
   * render document invalid.
   * 
   * @param name property name,
   * @param value property value.
   */
  @Override
  public void setProperty(String name, Object value)
  {
    switch(name) {
    case "js.template.serialize.prolog":
      serializeProlog = (Boolean)value;
      break;

    case "js.template.serialize.operator":
      serializeOperators = (Boolean)value;
      break;
    }
  }

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

  /**
   * Serialize template with given domain model to a writer. Walk through template document from its root and serialize
   * every node; if node contains operators execute them. Operators extract values from given domain model and process
   * them, result going to the same writer.
   * <p>
   * Writer parameter does not need to be {@link BufferedWriter}, but is acceptable. This interface implementation takes
   * care to initialize the writer accordingly. Given domain model can be an instance of {@link Content} or any POJO
   * containing values needed by template operators.
   * <p>
   * If any argument is null or writer is already closed this method behavior is not defined.
   * <p>
   * This method creates and instance of {@link Serializer} then delegates
   * {@link Serializer#write(js.dom.Element, Object)} for actual work.
   * 
   * @param model domain model object to inject into template,
   * @param writer writer to serialize template to.
   */
  private void _serialize(Writer writer, Object model) throws IOException
  {
    if(model == null) {
      document.serialize(writer);
      return;
    }

    Serializer serializer = new Serializer();
    if(serializeOperators) {
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
