package com.pressassociation.fire.partialresponse.fields.ast.visitor;

import com.google.common.collect.Lists;
import com.pressassociation.fire.partialresponse.fields.ast.*;

import java.util.Deque;

import static com.google.common.base.Preconditions.checkState;

/**
 * A visitor that will take an ast and expand out sub selection groups to their own paths.
 * <p/>
 * For example:<br/>
 * items(id,name) -> items/id,items/name<br/>
 * items(id,homeTown/id)/auth -> items/id/auth,items/homeTown/id/auth
 *
 * @author Matt Nathan
 */
public class ExpandingVisitor extends TransformingVisitor<AstNode> {

  private final Deque<AstNode> stack = Lists.newLinkedList();

  @Override
  public void visitFields(Fields fields) {
    super.visitFields(fields);
    // the last two items on the stack should be the items for this field
    AstNode top = stack.removeLast();
    AstNode bottom = stack.removeLast();
    stack.addLast(new Fields((Field) bottom, top));
  }

  @Override
  public void visitPath(Path path) {
    super.visitPath(path);
    // the last two things on the stack should be the parts of the path
    // they could be anything, for example items(id,type)/name(other,again) would resolve to
    // items/id,items/type and name/other,name/again
    AstNode top = stack.removeLast();
    AstNode bottom = stack.removeLast();
    if (bottom instanceof Fields) {
      if (top instanceof Fields) {
        // we need to cross join all the fields from the left with all the fields from the right
        // get all the fields from the top and bottom
        AstNode result = crossJoinFields((Fields) top, (Fields) bottom);
        stack.add(result);
      } else {
        AstNode left = new PathPostfixingVisitor((Field) top).applyTo(bottom);
        stack.addLast(left);
      }
    } else {
      assert bottom instanceof Node : "Bottom should be an instance of node, found: " + bottom;
      stack.addLast(new PathPrefixingVisitor((Node) bottom).applyTo(top));
    }
  }

  @Override
  public void visitSubSelection(SubSelection subSelection) {
    // first expand the sub selection
    AstNode expandedFields = new ExpandingVisitor().applyTo(subSelection.getFields());
    // then prefix the expanded fields with the sub selection name
    AstNode expandedSelection = new PathPrefixingVisitor(subSelection.getName()).applyTo(expandedFields);
    stack.addLast(expandedSelection);
  }

  @Override
  public void visitWildcard(Wildcard wildcard) {
    stack.addLast(wildcard);
  }

  @Override
  public void visitWord(Word word) {
    stack.addLast(word);
  }

  /**
   * Get the result from the visitor traversing the ast. This is optional because if the visitor is never called then
   * it is possible that there is no ast to return.
   *
   * @return The result.
   */
  @Override
  public AstNode getResult() {
    checkState(!stack.isEmpty(), "Cannot get the result when the visitor hasn't been used");
    return stack.peek();
  }

  /**
   * Takes two Fields and combines all combinations of their root lists.
   * <p/>
   * For example:
   * item,id and type,kind becomes item/type,item/kind,id/type,id/kind
   *
   * @param first  The first Fields
   * @param second The second fields
   * @return The node representing the cross join of both fields
   */
  private AstNode crossJoinFields(Fields first, Fields second) {
    // first we compile the list of all top level fields (plus the end non field) for each Fields
    Iterable<Field> firstFields = new FindFieldsVisitor().applyTo(first);
    Iterable<Field> secondFields = new FindFieldsVisitor().applyTo(second);

    AstNode result = null;
    // then we join all of first with the all of second.
    for (Field secondField : secondFields) {
      for (Field firctField : firstFields) {
        if (result == null) {
          result = new PathPostfixingVisitor(firctField).applyTo(secondField);
        } else {
          Field left = (Field) new PathPostfixingVisitor(firctField).applyTo(secondField);
          result = new Fields(left, result);
        }
      }
    }
    return result;
  }
}
