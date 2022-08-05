package com.jslib.template.xhtml;

import java.util.ArrayList;
import java.util.List;

import com.jslib.api.dom.Element;
import com.jslib.util.Strings;

/**
 * Element CSS class wrapper.
 * 
 * @author Iulian Rotaru
 */
final class CssClass extends AttrImpl
{
  private static final String ATTR_CLASS = "class";

  private List<String> classNames;

  protected CssClass(Element element)
  {
    super(ATTR_CLASS);
    classNames = Strings.split(element.getAttr(ATTR_CLASS));
    if(this.classNames == null) {
      this.classNames = new ArrayList<String>();
    }
  }

  protected void add(String className)
  {
    if(!this.classNames.contains(className)) {
      this.classNames.add(className);
    }
  }

  protected void remove(String className)
  {
    this.classNames.remove(className);
  }

  @Override
  public String getValue()
  {
    return Strings.join(classNames);
  }
}