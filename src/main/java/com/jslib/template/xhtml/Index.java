package com.jslib.template.xhtml;

/**
 * Index used by ordered lists and maps to keep items track for numbering. It is created before entering list or map iteration
 * and incremented before every item processing. Note that first item index value is 1, not zero; also every ordered list or map
 * has its own index. All indexes are kept into a stack to allows for nesting.
 * 
 * @author Iulian Rotaru
 */
final class Index
{
  /**
   * Index value. First item is 1.
   */
  int value;

  /**
   * Increment this index value.
   */
  void increment()
  {
    ++this.value;
  }
}