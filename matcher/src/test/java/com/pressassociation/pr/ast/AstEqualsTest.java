package com.pressassociation.pr.ast;

import com.google.common.testing.EqualsTester;

import org.junit.Test;

/**
 * Tests to check the equality of different AST configurations.
 *
 * @author Matt Nathan
 */
public class AstEqualsTest {
  @Test
  public void testEquals() {
    Wildcard wildcard = Wildcard.getSharedInstance();
    new EqualsTester()
        .addEqualityGroup(new Fields(wildcard, wildcard), new Fields(wildcard, wildcard))
        .addEqualityGroup(new Path(wildcard, wildcard), new Path(wildcard, wildcard))
        .addEqualityGroup(new SubSelection(wildcard, wildcard), new SubSelection(wildcard, wildcard))
        .addEqualityGroup(wildcard, wildcard)
        .addEqualityGroup(new Word("name"), new Word("name"))
        .addEqualityGroup(new Word("other"), new Word("other"))
        .testEquals();
  }
}