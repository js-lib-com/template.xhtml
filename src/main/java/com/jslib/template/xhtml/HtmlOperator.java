package com.jslib.template.xhtml;

import java.io.IOException;

import com.jslib.api.dom.Element;
import com.jslib.api.template.TemplateException;

/**
 * Set element inner HTML, useful for text formatted with HTML tags.
 * 
 * @author Iulian Rotaru
 */
public class HtmlOperator extends Operator
{
  /**
   * Parent serializer instance.
   */
  private Serializer serializer;

  /**
   * Dynamic content reference.
   */
  private Content content;

  /**
   * Construct TEXT operator instance.
   * 
   * @param serializer parent serializer instance,
   * @param content dynamic content reference.
   */
  HtmlOperator(Serializer serializer, Content content)
  {
    this.serializer = serializer;
    this.content = content;
  }

  @Override
  protected Object doExec(Element element, Object scope, String propertyPath, Object... arguments) throws IOException
  {
    if(element.hasChildren()) {
      throw new TemplateException("Illegal HTML operator on element with children.");
    }
    String html = content.getString(scope, propertyPath);
    if(html != null) {
      serializer.writeHtmlContent(html);
    }
    return null;
  }
}
