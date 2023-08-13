package minijava.lang.typechecker;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import minijava.lang.parser.AST.IntArray;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.Identifier;
import minijava.lang.parser.AST.Int;
import minijava.lang.parser.AST.MethodParam;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestExpression2TypeChecker {

   @Test
   public void areMatchingMethodHeaders() {
      List<MethodParam> params = Arrays.asList(
         new MethodParam(new Int(), new Identifier("p1")),
         new MethodParam(new Int(), new Identifier("p2"))
      );

      List<Type> types = Arrays.asList(
        new Int(),
        new Int()
      );

      assertTrue(Expression2TypeChecker.areMatchingMethodHeaders(params, types));

      types = Arrays.asList(
         new Int(),
         new IntArray()
      );

      assertFalse(Expression2TypeChecker.areMatchingMethodHeaders(params, types));
   }

}
