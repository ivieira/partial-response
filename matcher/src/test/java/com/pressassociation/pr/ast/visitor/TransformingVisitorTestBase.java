/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Press Association Limited
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.pressassociation.pr.ast.visitor;

import com.pressassociation.pr.ast.AstNode;
import com.pressassociation.pr.parser.Parser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Base class to make testing transforming visitors easier.
 *
 * @param <V> The type of visitor
 * @param <T> The response type
 * @author Matt Nathan
 */
public abstract class TransformingVisitorTestBase<V extends TransformingVisitor<T>, T> {

  protected static Object[] args(String source, String expected) {
    return genArgs(source, expected);
  }

  protected static Object[] genArgs(Object... args) {
    return args;
  }

  private final String source;
  private final String expected;
  private Parser parser;

  protected TransformingVisitorTestBase(String source, String expected) {
    this.source = source;
    this.expected = expected;
  }

  @Before
  public void setUp() {
    parser = new Parser();
  }

  @Test
  public void testTransformation() {
    AstNode sourceNode = parser.parse(source);
    T resultNode = createVisitor().applyTo(sourceNode);
    checkResult(sourceNode, resultNode);
  }

  protected void checkResult(AstNode sourceNode, T resultNode) {
    assertEquals("For source " + source, expected, toString(resultNode));
  }

  protected String toString(T resultNode) {
    return resultNode.toString();
  }

  protected abstract V createVisitor();
}
