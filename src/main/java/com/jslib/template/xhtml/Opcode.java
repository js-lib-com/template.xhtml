package com.jslib.template.xhtml;

/**
 * Templates engine operator codes enumeration. Operators are defined into template elements using standard attribute syntax.
 * This enumeration provides {@link #fromAttrName(String)} factory method just for that: extract opcode from attribute name.
 * Also every operator's code belongs to a {@link Type}; it can be retrieved using {@link Opcode#type()} getter.
 * 
 * @author Iulian Rotaru
 */
enum Opcode
{
  /** Neutral value. */
  NONE(Type.NONE),

  /** Set one or more element's attributes values. */
  ATTR(Type.ATTRIBUTE),

  /** Set element <em>id</em> attribute value. */
  ID(Type.ATTRIBUTE),

  /** Add / remove element <em>class</em>. */
  CSS_CLASS(Type.ATTRIBUTE),

  /** Set element <em>src</em> attribute value. */
  SRC(Type.ATTRIBUTE),

  /** Set element <em>href</em> attribute value. */
  HREF(Type.ATTRIBUTE),

  /** Set element <em>title</em> attribute value. */
  TITLE(Type.ATTRIBUTE),

  /** Set element <em>value</em> attribute value. */
  VALUE(Type.ATTRIBUTE),

  /** Exclude branch from resulting document based on boolean literal. */
  EXCLUDE(Type.CONDITIONAL),

  /** Include branch if content value is not empty. */
  IF(Type.CONDITIONAL),

  /** Set element text content. */
  TEXT(Type.CONTENT),

  /** Set element inner HTML. */
  HTML(Type.CONTENT),

  /** Initialize formatter instance. */
  FORMAT(Type.FORMATTING),

  /** Set element text content accordingly numbering format and item index. */
  NUMBERING(Type.CONTENT),

  /** Set element descendants to content object properties. */
  OBJECT(Type.CONTENT),

  /** Populate element using first child as item template. */
  LIST(Type.CONTENT),

  /** Ordered variant of {@link #LIST}. */
  OLIST(Type.CONTENT),

  /** Populate element using first two children as key/value templates. */
  MAP(Type.CONTENT),

  /** Ordered variant of {@link #MAP}. */
  OMAP(Type.CONTENT);

  private static final String PREFIX_DATA_ATTR = "data-";
  private static final int PREFIX_DATA_ATTR_LENGHT = PREFIX_DATA_ATTR.length();

  /**
   * Parse attribute name and return the operator's code. If attribute is not a valid operator returns {@link #NONE}.
   * 
   * @param attrName attribute name.
   * @return opcode extracted from attribute name or NONE.
   */
  public static Opcode fromAttrName(String attrName)
  {
    if(!attrName.startsWith(PREFIX_DATA_ATTR)) return NONE;
    try {
      return valueOf(attrName.substring(PREFIX_DATA_ATTR_LENGHT).toUpperCase().replace('-', '_'));
    }
    catch(IllegalArgumentException e) {
      // is legal to have data-xxx attributes that are not template opcodes
      // using exception for normal flow control is clearly anti-pattern but i do not see a better solution
      return NONE;
    }
  }

  /** Operator's type. */
  private Type type;

  /**
   * Construct opcode instance.
   * 
   * @param type operator's type.
   */
  private Opcode(Type type)
  {
    this.type = type;
  }

  /**
   * Retrieve operator type.
   * 
   * @return operator type.
   */
  public Type type()
  {
    return this.type;
  }

  /**
   * An operator belongs to a given category, defined by this type. Templates engine algorithm does not use opcodes directly. In
   * order to keep it generic the algorithm uses groups of opcodes defined by this enumeration. This way, adding new operators
   * does not inflect on engine algorithm.
   * 
   * @author Iulian Rotaru
   */
  public static enum Type
  {
    /**
     * Neutral value.
     */
    NONE,

    /**
     * This operators include or exclude DOM branches based on some condition. Conditional operators return branches enabled
     * flag: if returned value is false current element and all descendants are not included into generated document.
     * <p>
     * Note that at most one conditional operator is allowed per element and next operators are mutually excluding:
     * <ul>
     * <li>{@link Opcode#IF}
     * <li>{@link Opcode#EXCLUDE}
     * </ul>
     * Templates engine will rise invalid operators list exception if trying to combine any of them.
     */
    CONDITIONAL,

    /**
     * Set {@link Format} instance used to prepare content value before actual insertion.
     */
    FORMATTING,

    /**
     * Operates upon element content be it text content or generated children elements. Operator implementation should return
     * the scope object or null if processing is complete. In last case templates engine does not attempt to scan context
     * element children, i.e. consider the branch fully processed.
     * <p>
     * Note that at most one content operator is allowed per element and next operators are mutually excluding:
     * <ul>
     * <li>{@link Opcode#OBJECT}
     * <li>{@link Opcode#TEXT}
     * <li>{@link Opcode#HTML}
     * <li>{@link Opcode#NUMBERING}
     * <li>{@link Opcode#LIST}
     * <li>{@link Opcode#OLIST}
     * <li>{@link Opcode#MAP}
     * <li>{@link Opcode#OMAP}
     * </ul>
     * Templates engine will rise invalid operators list if trying to combine any of them.
     */
    CONTENT,

    /**
     * Set specified attribute to value extracted from content.
     */
    ATTRIBUTE
  }
}
