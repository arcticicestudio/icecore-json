/*
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
title      JSON Parser Test                                +
project    icecore-json                                    +
version    0.8.0-frost.1                                   +
repository https://github.com/arcticicestudio/icecore-json +
author     Arctic Ice Studio                               +
email      development@arcticicestudio.com                 +
copyright  Copyright (C) 2016                              +
++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
*/
package com.arcticicestudio.icecore.json;

import org.junit.Before;
import org.junit.Test;

import static com.arcticicestudio.icecore.json.Json.parse;
import static com.arcticicestudio.icecore.json.TestUtil.assertException;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import com.arcticicestudio.icecore.json.Json.DefaultHandler;
import com.arcticicestudio.icecore.json.TestUtil.RunnableEx;

/**
 * Tests the JSON parser class {@link JsonParser}.
 *
 * @author Arctic Ice Studio &lt;development@arcticicestudio.com&gt;
 * @since 0.7.0
 */
public class JsonParserTest {

  private TestHandler handler;
  private JsonParser parser;

  /**
   * @since 0.8.0
   */
  @Before
  public void setUp() {
    handler = new TestHandler();
    parser = new JsonParser(handler);
  }

  /**
   * @since 0.8.0
   */
  @Test(expected = NullPointerException.class)
  public void constructorRejectsNullHandler() {
    new JsonParser(null);
  }

  /**
   * @since 0.8.0
   */
  @Test(expected = NullPointerException.class)
  public void parseStringRejectsNull() {
    parser.parse((String)null);
  }

  /**
   * @since 0.8.0
   */
  @Test(expected = NullPointerException.class)
  public void parseReaderRejectsNull() throws IOException {
    parser.parse((Reader)null);
  }

  /**
   * @since 0.8.0
   */
  @Test(expected = IllegalArgumentException.class)
  public void parseReaderRejectsNegativeBufferSize() throws IOException {
    parser.parse(new StringReader("[]"), -1);
  }

  @Test
  public void parseStringRejectsEmpty() {
    assertParseException(0, "Unexpected end of input", "");
  }

  @Test
  public void parseReaderRejectsEmpty() {
    ParseException exception = assertException(ParseException.class, new RunnableEx() {
      public void run() throws IOException {
        parser.parse(new StringReader(""));
      }
    });
    assertEquals(0, exception.getLocation().offset);
    assertThat(exception.getMessage(), startsWith("Unexpected end of input at"));
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseNull() {
    parser.parse("null");
    assertEquals(join("startNull 0", "endNull 4"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseTrue() {
    parser.parse("true");
    assertEquals(join("startBoolean 0", "endBoolean true 4"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseFalse() {
    parser.parse("false");
    assertEquals(join("startBoolean 0", "endBoolean false 5"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseString() {
    parser.parse("\"yogurt\"");
    assertEquals(join("startString 0", "endString yogurt 8"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseStringEmpty() {
    parser.parse("\"\"");
    assertEquals(join("startString 0", "endString  2"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseNumber() {
    parser.parse("23");
    assertEquals(join("startNumber 0", "endNumber 23 2"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseNumberNegative() {
    parser.parse("-23");
    assertEquals(join("startNumber 0", "endNumber -23 3"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseNumberNegativeExponent() {
    parser.parse("-2.3e-12");
    assertEquals(join("startNumber 0", "endNumber -2.3e-12 8"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseArray() {
    parser.parse("[23]");
    assertEquals(join(
      "startArray 0",
      "startArrayValue a1 1",
      "startNumber 1",
      "endNumber 23 3",
      "endArrayValue a1 3",
      "endArray a1 4"),
      handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseArrayEmpty() {
    parser.parse("[]");
    assertEquals(join("startArray 0", "endArray a1 2"), handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseObject() {
    parser.parse("{\"foo\": 23}");
    assertEquals(join(
      "startObject 0",
      "startObjectName o1 1",
      "endObjectName o1 foo 6",
      "startObjectValue o1 foo 8",
      "startNumber 8",
      "endNumber 23 10",
      "endObjectValue o1 foo 10",
      "endObject o1 11"),
      handler.getLog());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseObjectEmpty() {
    parser.parse("{}");
    assertEquals(join("startObject 0", "endObject o1 2"), handler.getLog());
  }

  @Test
  public void parseStripsPadding() {
    assertEquals(new JsonArray(), parse(" [ ] "));
  }

  @Test
  public void parseIgnoresAllWhiteSpace() {
    assertEquals(new JsonArray(), parse("\t\r\n [\t\r\n ]\t\r\n "));
  }

  @Test
  public void parseFailsWithUnterminatedString() {
    assertParseException(5, "Unexpected end of input", "[\"foo");
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseLineAndColumnOnFirstLine() {
    parser.parse("[]");
    assertEquals("1:3", handler.lastLocation.toString());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseLineAndColumnAfterLF() {
    parser.parse("[\n]");
    assertEquals("2:2", handler.lastLocation.toString());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseLineAndColumnAfterCRLF() {
    parser.parse("[\r\n]");
    assertEquals("2:2", handler.lastLocation.toString());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseLineAndColumnAfterCR() {
    parser.parse("[\r]");
    assertEquals("1:4", handler.lastLocation.toString());
  }

  @Test
  public void parseHandlesInputsThatExceedBufferSize() throws IOException {
    DefaultHandler defHandler = new DefaultHandler();
    parser = new JsonParser(defHandler);
    String input = "[ 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47 ]";
    parser.parse(new StringReader(input), 3);
    assertEquals("[2,3,5,7,11,13,17,19,23,29,31,37,41,43,47]", defHandler.getValue().toString());
  }

  @Test
  public void parseHandlesStringsThatExceedBufferSize() throws IOException {
    DefaultHandler defHandler = new DefaultHandler();
    parser = new JsonParser(defHandler);
    String input = "[ \"lorem ipsum dolor sit amet\" ]";
    parser.parse(new StringReader(input), 3);
    assertEquals("[\"lorem ipsum dolor sit amet\"]", defHandler.getValue().toString());
  }

  @Test
  public void parseHandlesNumbersThatExceedBufferSize() throws IOException {
    DefaultHandler defHandler = new DefaultHandler();
    parser = new JsonParser(defHandler);
    String input = "[ 3.141592653589 ]";
    parser.parse(new StringReader(input), 3);
    assertEquals("[3.141592653589]", defHandler.getValue().toString());
  }

  @Test
  public void parseHandlesPositionsCorrectlyWhenInputExceedsBufferSize() {
    final String input = "{\n  \"a\": 23,\n  \"b\": 42,\n}";
    ParseException exception = assertException(ParseException.class, new RunnableEx() {
      public void run() throws IOException {
        parser.parse(new StringReader(input), 3);
      }
    });
    assertEquals(new Location(24, 4, 1), exception.getLocation());
  }

  @Test
  public void parseFailsOnTooDeeplyNestedArray() {
    JsonArray array = new JsonArray();
    for (int i = 0; i < 1001; i++) {
      array = new JsonArray().add(array);
    }
    final String input = array.toString();
    ParseException exception = assertException(ParseException.class, new RunnableEx() {
      public void run() throws IOException {
        parser.parse(input);
      }
    });
    assertEquals("Nesting too deep at 1:1002", exception.getMessage());
  }

  @Test
  public void parseFailsOnTooDeeplyNestedObject() {
    JsonObject object = new JsonObject();
    for (int i = 0; i < 1001; i++) {
      object = new JsonObject().add("foo", object);
    }
    final String input = object.toString();
    ParseException exception = assertException(ParseException.class, new RunnableEx() {
      public void run() throws IOException {
        parser.parse(input);
      }
    });
    assertEquals("Nesting too deep at 1:7002", exception.getMessage());
  }

  @Test
  public void parseFailsOnTooDeeplyNestedMixedObject() {
    JsonValue value = new JsonObject();
    for (int i = 0; i < 1001; i++) {
      value = i % 2 == 0 ? new JsonArray().add(value) : new JsonObject().add("foo", value);
    }
    final String input = value.toString();
    ParseException exception = assertException(ParseException.class, new RunnableEx() {
      public void run() throws IOException {
        parser.parse(input);
      }
    });
    assertEquals("Nesting too deep at 1:4002", exception.getMessage());
  }

  @Test
  public void parseDoesNotFailWithManyArrays() {
    JsonArray array = new JsonArray();
    for (int i = 0; i < 1001; i++) {
      array.add(new JsonArray().add(7));
    }
    final String input = array.toString();
    JsonValue result = parse(input);
    assertTrue(result.isArray());
  }

  @Test
  public void parseDoesNotFailWithManyEmptyArrays() {
    JsonArray array = new JsonArray();
    for (int i = 0; i < 1001; i++) {
      array.add(new JsonArray());
    }
    final String input = array.toString();
    JsonValue result = parse(input);
    assertTrue(result.isArray());
  }

  @Test
  public void parseDoesNotFailWithManyObjects() {
    JsonArray array = new JsonArray();
    for (int i = 0; i < 1001; i++) {
      array.add(new JsonObject().add("a", 7));
    }
    final String input = array.toString();
    JsonValue result = parse(input);
    assertTrue(result.isArray());
  }

  @Test
  public void parseDoesNotFailWithManyEmptyObjects() {
    JsonArray array = new JsonArray();
    for (int i = 0; i < 1001; i++) {
      array.add(new JsonObject());
    }
    final String input = array.toString();
    JsonValue result = parse(input);
    assertTrue(result.isArray());
  }

  /**
   * @since 0.8.0
   */
  @Test
  public void parseCanBeCalledTwice() {
    parser.parse("[23]");
    parser.parse("[42]");
    assertEquals(join(
      "startArray 0",
      "startArrayValue a1 1",
      "startNumber 1",
      "endNumber 23 3",
      "endArrayValue a1 3",
      "endArray a1 4",
      "startArray 0",
      "startArrayValue a2 1",
      "startNumber 1",
      "endNumber 42 3",
      "endArrayValue a2 3",
      "endArray a2 4"),
      handler.getLog());
  }

  @Test
  public void arraysEmpty() {
    assertEquals("[]", parse("[]").toString());
  }

  @Test
  public void arraysSingleValue() {
    assertEquals("[92]", parse("[92]").toString());
  }

  @Test
  public void arraysMultipleValues() {
    assertEquals("[92,42]", parse("[92,42]").toString());
  }

  @Test
  public void arraysWithWhitespaces() {
    assertEquals("[92,42]", parse("[ 92 , 42 ]").toString());
  }

  @Test
  public void arraysNested() {
    assertEquals("[[92]]", parse("[[92]]").toString());
    assertEquals("[[[]]]", parse("[[[]]]").toString());
    assertEquals("[[92],42]", parse("[[92],42]").toString());
    assertEquals("[[92],[42]]", parse("[[92],[42]]").toString());
    assertEquals("[[92],[42]]", parse("[[92],[42]]").toString());
    assertEquals("[{\"yogurt\":[92]},{\"coconut\":[42]}]", parse("[{\"yogurt\":[92]},{\"coconut\":[42]}]").toString());
  }

  @Test
  public void arraysIllegalSyntax() {
    assertParseException(1, "Expected value", "[,]");
    assertParseException(4, "Expected ',' or ']'", "[92 42]");
    assertParseException(4, "Expected value", "[92,]");
  }

  @Test
  public void arraysIncomplete() {
    assertParseException(1, "Unexpected end of input", "[");
    assertParseException(2, "Unexpected end of input", "[ ");
    assertParseException(3, "Unexpected end of input", "[92");
    assertParseException(4, "Unexpected end of input", "[92 ");
    assertParseException(4, "Unexpected end of input", "[92,");
    assertParseException(5, "Unexpected end of input", "[92, ");
  }

  @Test
  public void objectsEmpty() {
    assertEquals("{}", parse("{}").toString());
  }

  @Test
  public void objectsSingleValue() {
    assertEquals("{\"yogurt\":92}", parse("{\"yogurt\":92}").toString());
  }

  @Test
  public void objectsMultipleValues() {
    assertEquals("{\"yogurt\":92,\"coconut\":42}", parse("{\"yogurt\":92,\"coconut\":42}").toString());
  }

  @Test
  public void objectsWhitespace() {
    assertEquals("{\"yogurt\":92,\"coconut\":42}", parse("{ \"yogurt\" : 92, \"coconut\" : 42 }").toString());
  }

  @Test
  public void objectsNested() {
    assertEquals("{\"yogurt\":{}}", parse("{\"yogurt\":{}}").toString());
    assertEquals("{\"yogurt\":{\"coconut\":42}}", parse("{\"yogurt\":{\"coconut\": 42}}").toString());
    assertEquals("{\"yogurt\":{\"coconut\":{\"chocolate\":42}}}",
      parse("{\"yogurt\":{\"coconut\": {\"chocolate\": 42}}}").toString());
    assertEquals("{\"yogurt\":[{\"coconut\":{\"chocolate\":[[42]]}}]}",
      parse("{\"yogurt\":[{\"coconut\": {\"chocolate\": [[42]]}}]}").toString());
  }

  @Test
  public void objectsIllegalSyntax() {
    assertParseException(1, "Expected name", "{,}");
    assertParseException(1, "Expected name", "{:}");
    assertParseException(1, "Expected name", "{92}");
    assertParseException(4, "Expected ':'", "{\"a\"}");
    assertParseException(5, "Expected ':'", "{\"a\" \"b\"}");
    assertParseException(5, "Expected value", "{\"a\":}");
    assertParseException(8, "Expected name", "{\"a\":92,}");
    assertParseException(8, "Expected name", "{\"a\":92,42");
  }

  @Test
  public void objectsIncomplete() {
    assertParseException(1, "Unexpected end of input", "{");
    assertParseException(2, "Unexpected end of input", "{ ");
    assertParseException(2, "Unexpected end of input", "{\"");
    assertParseException(4, "Unexpected end of input", "{\"a\"");
    assertParseException(5, "Unexpected end of input", "{\"a\" ");
    assertParseException(5, "Unexpected end of input", "{\"a\":");
    assertParseException(6, "Unexpected end of input", "{\"a\": ");
    assertParseException(7, "Unexpected end of input", "{\"a\":92");
    assertParseException(8, "Unexpected end of input", "{\"a\":92 ");
    assertParseException(8, "Unexpected end of input", "{\"a\":92,");
    assertParseException(9, "Unexpected end of input", "{\"a\":92, ");
  }

  @Test
  public void stringsEmptyStringIsAccepted() {
    assertEquals("", parse("\"\"").asString());
  }

  @Test
  public void stringsAsciiCharactersAreAccepted() {
    assertEquals(" ", parse("\" \"").asString());
    assertEquals("a", parse("\"a\"").asString());
    assertEquals("yogurt", parse("\"yogurt\"").asString());
    assertEquals("A2-D2", parse("\"A2-D2\"").asString());
    assertEquals("\u007f", parse("\"\u007f\"").asString());
  }

  @Test
  public void stringsNonAsciiCharactersAreAccepted() {
    assertEquals("Русский", parse("\"Русский\"").asString());
    assertEquals("العربية", parse("\"العربية\"").asString());
    assertEquals("日本語", parse("\"日本語\"").asString());
  }

  @Test
  public void stringsControlCharactersAreRejected() {
    /* JSON strings MUST NOT contain characters < 0x20 */
    assertParseException(3, "Expected valid string character", "\"--\n--\"");
    assertParseException(3, "Expected valid string character", "\"--\r\n--\"");
    assertParseException(3, "Expected valid string character", "\"--\t--\"");
    assertParseException(3, "Expected valid string character", "\"--\u0000--\"");
    assertParseException(3, "Expected valid string character", "\"--\u001f--\"");
  }

  @Test
  public void stringsValidEscapesAreAccepted() {
    /* Valid escapes are \" \\ \/ \b \f \n \r \t and unicode escapes */
    assertEquals(" \" ", parse("\" \\\" \"").asString());
    assertEquals(" \\ ", parse("\" \\\\ \"").asString());
    assertEquals(" / ", parse("\" \\/ \"").asString());
    assertEquals(" \u0008 ", parse("\" \\b \"").asString());
    assertEquals(" \u000c ", parse("\" \\f \"").asString());
    assertEquals(" \r ", parse("\" \\r \"").asString());
    assertEquals(" \n ", parse("\" \\n \"").asString());
    assertEquals(" \t ", parse("\" \\t \"").asString());
  }

  @Test
  public void stringsEscapeAtStart() {
    assertEquals("\\x", parse("\"\\\\x\"").asString());
  }

  @Test
  public void stringsEscapeAtEnd() {
    assertEquals("x\\", parse("\"x\\\\\"").asString());
  }

  @Test
  public void stringsIllegalEscapesAreRejected() {
    assertParseException(2, "Expected valid escape sequence", "\"\\a\"");
    assertParseException(2, "Expected valid escape sequence", "\"\\x\"");
    assertParseException(2, "Expected valid escape sequence", "\"\\000\"");
  }

  @Test
  public void stringsValidUnicodeEscapesAreAccepted() {
    assertEquals("\u0021", parse("\"\\u0021\"").asString());
    assertEquals("\u4711", parse("\"\\u4711\"").asString());
    assertEquals("\uffff", parse("\"\\uffff\"").asString());
    assertEquals("\uabcdx", parse("\"\\uabcdx\"").asString());
  }

  @Test
  public void stringsIllegalUnicodeEscapesAreRejected() {
    assertParseException(3, "Expected hexadecimal digit", "\"\\u \"");
    assertParseException(3, "Expected hexadecimal digit", "\"\\ux\"");
    assertParseException(5, "Expected hexadecimal digit", "\"\\u20 \"");
    assertParseException(6, "Expected hexadecimal digit", "\"\\u000x\"");
  }

  @Test
  public void stringsIncompleteStringsAreRejected() {
    assertParseException(1, "Unexpected end of input", "\"");
    assertParseException(7, "Unexpected end of input", "\"yogurt");
    assertParseException(8, "Unexpected end of input", "\"yogurt\\");
    assertParseException(9, "Unexpected end of input", "\"yogurt\\n");
    assertParseException(9, "Unexpected end of input", "\"yogurt\\u");
    assertParseException(10, "Unexpected end of input", "\"yogurt\\u0");
    assertParseException(12, "Unexpected end of input", "\"yogurt\\u000");
    assertParseException(13, "Unexpected end of input", "\"yogurt\\u0000");
  }

  @Test
  public void numbersInteger() {
    assertEquals(new JsonNumber("0"), parse("0"));
    assertEquals(new JsonNumber("-0"), parse("-0"));
    assertEquals(new JsonNumber("1"), parse("1"));
    assertEquals(new JsonNumber("-1"), parse("-1"));
    assertEquals(new JsonNumber("23"), parse("23"));
    assertEquals(new JsonNumber("-23"), parse("-23"));
    assertEquals(new JsonNumber("1234567890"), parse("1234567890"));
    assertEquals(new JsonNumber("123456789012345678901234567890"), parse("123456789012345678901234567890"));
  }

  @Test
  public void numbersMinusZero() {
    /* Allowed for JSON and Java */
    JsonValue value = parse("-0");
    assertEquals(0, value.asInt());
    assertEquals(0l, value.asLong());
    assertEquals(0f, value.asFloat(), 0);
    assertEquals(0d, value.asDouble(), 0);
  }

  @Test
  public void numbersDecimal() {
    assertEquals(new JsonNumber("0.23"), parse("0.23"));
    assertEquals(new JsonNumber("-0.23"), parse("-0.23"));
    assertEquals(new JsonNumber("1234567890.12345678901234567890"), parse("1234567890.12345678901234567890"));
  }

  @Test
  public void numbersWithExponent() {
    assertEquals(new JsonNumber("0.1e9"), parse("0.1e9"));
    assertEquals(new JsonNumber("0.1e9"), parse("0.1e9"));
    assertEquals(new JsonNumber("0.1E9"), parse("0.1E9"));
    assertEquals(new JsonNumber("-0.23e9"), parse("-0.23e9"));
    assertEquals(new JsonNumber("0.23e9"), parse("0.23e9"));
    assertEquals(new JsonNumber("0.23e+9"), parse("0.23e+9"));
    assertEquals(new JsonNumber("0.23e-9"), parse("0.23e-9"));
  }

  @Test
  public void numbersWithInvalidFormat() {
    assertParseException(0, "Expected value", "+1");
    assertParseException(0, "Expected value", ".1");
    assertParseException(1, "Unexpected character", "02");
    assertParseException(2, "Unexpected character", "-02");
    assertParseException(1, "Expected digit", "-x");
    assertParseException(2, "Expected digit", "1.x");
    assertParseException(2, "Expected digit", "1ex");
    assertParseException(3, "Unexpected character", "1e1x");
  }

  @Test
  public void numbersIncomplete() {
    assertParseException(1, "Unexpected end of input", "-");
    assertParseException(2, "Unexpected end of input", "1.");
    assertParseException(4, "Unexpected end of input", "1.0e");
    assertParseException(5, "Unexpected end of input", "1.0e-");
  }

  @Test
  public void nullComplete() {
    assertEquals(Json.NULL, parse("null"));
  }

  @Test
  public void nullIncomplete() {
    assertParseException(1, "Unexpected end of input", "n");
    assertParseException(2, "Unexpected end of input", "nu");
    assertParseException(3, "Unexpected end of input", "nul");
  }

  @Test
  public void nullWithIllegalCharacter() {
    assertParseException(1, "Expected 'u'", "nx");
    assertParseException(2, "Expected 'l'", "nux");
    assertParseException(3, "Expected 'l'", "nulx");
    assertParseException(4, "Unexpected character", "nullx");
  }

  @Test
  public void trueComplete() {
    assertSame(Json.TRUE, parse("true"));
  }

  @Test
  public void trueIncomplete() {
    assertParseException(1, "Unexpected end of input", "t");
    assertParseException(2, "Unexpected end of input", "tr");
    assertParseException(3, "Unexpected end of input", "tru");
  }

  @Test
  public void trueWithIllegalCharacter() {
    assertParseException(1, "Expected 'r'", "tx");
    assertParseException(2, "Expected 'u'", "trx");
    assertParseException(3, "Expected 'e'", "trux");
    assertParseException(4, "Unexpected character", "truex");
  }

  @Test
  public void falseComplete() {
    assertSame(Json.FALSE, parse("false"));
  }

  @Test
  public void falseIncomplete() {
    assertParseException(1, "Unexpected end of input", "f");
    assertParseException(2, "Unexpected end of input", "fa");
    assertParseException(3, "Unexpected end of input", "fal");
    assertParseException(4, "Unexpected end of input", "fals");
  }

  @Test
  public void falseWithIllegalCharacter() {
    assertParseException(1, "Expected 'a'", "fx");
    assertParseException(2, "Expected 'l'", "fax");
    assertParseException(3, "Expected 's'", "falx");
    assertParseException(4, "Expected 'e'", "falsx");
    assertParseException(5, "Unexpected character", "falsex");
  }

  private void assertParseException(int offset, String message, final String json) {
    ParseException exception = assertException(ParseException.class, new Runnable() {
      public void run() {
        parser.parse(json);
      }
    });
    assertEquals(offset, exception.getLocation().offset);
    assertThat(exception.getMessage(), startsWith(message + " at"));
  }

  /**
   * @param strings The strings to join
   * @return the joined string
   * @since 0.8.0
   */
  private static String join(String... strings) {
    StringBuilder builder = new StringBuilder();
    for (String string : strings) {
      builder.append(string).append('\n');
    }
    return builder.toString();
  }

  /**
   * @since 0.8.0
   */
  static class TestHandler extends JsonHandler<Object, Object> {

    Location lastLocation;
    StringBuilder log = new StringBuilder();
    int sequence = 0;

    @Override
    public void startNull() {
      record("startNull");
    }

    @Override
    public void endNull() {
      record("endNull");
    }

    @Override
    public void startBoolean() {
      record("startBoolean");
    }

    @Override
    public void endBoolean(boolean value) {
      record("endBoolean", Boolean.valueOf(value));
    }

    @Override
    public void startString() {
      record("startString");
    }

    @Override
    public void endString(String string) {
      record("endString", string);
    }

    @Override
    public void startNumber() {
      record("startNumber");
    }

    @Override
    public void endNumber(String string) {
      record("endNumber", string);
    }

    @Override
    public Object startArray() {
      record("startArray");
      return "a" + ++sequence;
    }

    @Override
    public void endArray(Object array) {
      record("endArray", array);
    }

    @Override
    public void startArrayValue(Object array) {
      record("startArrayValue", array);
    }

    @Override
    public void endArrayValue(Object array) {
      record("endArrayValue", array);
    }

    @Override
    public Object startObject() {
      record("startObject");
      return "o" + ++sequence;
    }

    @Override
    public void endObject(Object object) {
      record("endObject", object);
    }

    @Override
    public void startObjectName(Object object) {
      record("startObjectName", object);
    }

    @Override
    public void endObjectName(Object object, String name) {
      record("endObjectName", object, name);
    }

    @Override
    public void startObjectValue(Object object, String name) {
      record("startObjectValue", object, name);
    }

    @Override
    public void endObjectValue(Object object, String name) {
      record("endObjectValue", object, name);
    }

    private void record(String event, Object... args) {
      lastLocation = getLocation();
      log.append(event);
      for (Object arg : args) {
        log.append(' ').append(arg);
      }
      log.append(' ').append(lastLocation.offset).append('\n');
    }

    String getLog() {
      return log.toString();
    }
  }
}
