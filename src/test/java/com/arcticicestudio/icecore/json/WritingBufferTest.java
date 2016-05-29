/*
+++++++++++++++++++++++++++++++++++++++++++
title     JSON Writing Buffer Test        +
project   icecore-json                    +
file      WritingBufferTest.java          +
version                                   +
author    Arctic Ice Studio               +
email     development@arcticicestudio.com +
website   http://arcticicestudio.com      +
copyright Copyright (C) 2016              +
created   2016-05-29 20:52 UTC+0200       +
modified  2016-05-29 20:53 UTC+0200       +
+++++++++++++++++++++++++++++++++++++++++++

[Description]
Tests the JSON writer writing buffer class "WritingBuffer".

[Copyright]
Copyright (C) 2016 Arctic Ice Studio <development@arcticicestudio.com>

[References]
JSON
  (http://json.org)
ECMA-404 1st Edition (October 2013)
  (http://www.ecma-international.org/publications/files/ECMA-ST/ECMA-404.pdf)
Java 8 API Documentation
  (https://docs.oracle.com/javase/8/docs/api/)
JUnit
  (http://junit.org)
Mockito
  (http://mockito.org)
Arctic Versioning Specification (ArcVer)
  (http://specs.arcticicestudio.com/arcver)
*/
package com.arcticicestudio.icecore.json;

import org.junit.Before;

import java.io.StringWriter;
import java.util.Arrays;

/**
 * Tests the JSON writer writing buffer class {@link WritingBuffer}.
 *
 * @author Arctic Ice Studio &lt;development@arcticicestudio.com&gt;
 * @since 0.7.0
 */
public class WritingBufferTest {

  private static final int BUFFER_SIZE = 16;
  private StringWriter wrapped;
  private WritingBuffer writer;

  @Before
  public void setUp() {
    wrapped = new StringWriter();
    writer = new WritingBuffer(wrapped, BUFFER_SIZE);
  }

  private static String createString(int length) {
    return new String(createChars(length));
  }

  private static char[] createChars(int length) {
    char[] array = new char[length];
    Arrays.fill(array, 'x');
    return array;
  }
}
