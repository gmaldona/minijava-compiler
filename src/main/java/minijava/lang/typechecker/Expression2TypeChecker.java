package minijava.lang.typechecker;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import minijava.lang.parser.AST.Expression2;
import minijava.lang.parser.AST.ClassExpression;
import minijava.lang.parser.AST.ClassType;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.Operation;
import minijava.lang.parser.AST.ArrayLength;
import minijava.lang.parser.AST.ExprArray;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.IntArray;
import minijava.lang.parser.AST.Int;
import minijava.lang.parser.AST.MethodParam;
import minijava.lang.parser.AST.ExprClassMember;
import minijava.lang.parser.AST.IExpression;
import minijava.lang.parser.SymbolTable;

public class Expression2TypeChecker extends TypeChecker {

   private static final Logger LOG = Logger.getLogger(Expression2TypeChecker.class.getName());

   private Expression2TypeChecker() {}

   /**
    * Evaluates a class function returns.
    *    e.g. Klass.getFunction(params);
    * Assumption that the param expr will always evaluate to {@link ClassDecl}
    * Makes a deep copy of {@link ClassDecl} but removes the reference to Expr2 to avoid an infinite recursive loop
    * @return Class member (function) type
    */
   protected static Type evalExprClassMember(SymbolTable<?> symbolTable, IExpression expr, ExprClassMember exprClassMember) {
      @FunctionalInterface
      interface IntermediateFunction {
         Type run();
      }

      class IntermediateHelper {
         /**
          * Problem that {@link IntermediateHelper} fixes:
          *    - The ClassType for the previous IExpression is needed to determine if exprClassMember exists
          *    - This is a recursive step so if Some(IExpression) has an Some(IExpression2) and within evalExpression2 you try to
          *          eval the parent Some(IExpression) you run into an infinite recursive step because in order for evalExpression2 to run
          *          that means there is always a Some(Expression2) attached to Some(Expression)
          * Solution that {@link IntermediateHelper} implements:
          *    - To avoid this infinite recursive step, we want n - 1. In order for n to exist we always know n - 1 exists.
          *       - 1. Store pointer to n from n - 1
          *       - 2. Remove reference from n - 1 to n
          *       - 3. Run some {@link IntermediateFunction}
          *       - 4. Update reference from n -1 to n to the previous value
          *  This will get you the ClassType of n - 1 without running to a recursive loop of eval(n) from n - 1 and then within n - 1,
          *  eval( (n - 1) + 1) which is where you started causing a stackoverflow
          * {@link IntermediateHelper}
          *
          * @return the evaluated type for {@link IExpression} expr
          */
         Type deepCopy(ClassExpression expr, IntermediateFunction function) {
            Optional<Expression2> expr2 = expr.expr2();
            Expression2 expr2Value = null;
            if (expr2.isPresent()) {
               expr2Value = expr2.get();
            }
            expr.expr2(Optional.empty());
            Type type = function.run();
            if (Objects.nonNull(expr2Value)) {
               expr.expr2(Optional.of(expr2Value));
            }
            return type;
         }
      }

      IntermediateHelper helper = new IntermediateHelper();
      Type type = helper.deepCopy((ClassExpression) expr, () -> evalExpression(symbolTable, expr));
      // get root table and check all ClassDecl Tables if classMember exists
      SymbolTable<?> rootTable       = symbolTable.getRoot();
      Stream<SymbolTable<?>> classDeclTables = rootTable.childTableStream(ClassDecl.class);

      return type;
   }

   protected static Int evalArrayLength(SymbolTable<?> symbolTable, IExpression expr, ArrayLength arrayLength) {
      return (arrayLength.expr2().isPresent()) ?
         (Int) areCompatibleTypes(Int.class, evalExpression2(symbolTable, arrayLength, arrayLength.expr2().get())) :
         new Int();
   }

   protected static Type evalExprArray(SymbolTable<?> symbolTable, IExpression expr, ExprArray exprArray) {
      if (exprArray.expr2().isPresent()) {
         areCompatibleTypes(Int.class, evalExpression(symbolTable, exprArray.expr()));
         return evalExpression2(symbolTable, exprArray, exprArray.expr2().get());
      }
      areCompatibleTypes(Int.class, evalExpression(symbolTable, exprArray.expr()));
      return new IntArray();
   }

   protected static Type evalExprOperation(SymbolTable<?> symbolTable, IExpression expr, Operation operation) {
      return (operation.expr2().isPresent()) ?
         areCompatibleTypes(evalExpression(symbolTable, operation.expr()), evalExpression2(symbolTable, operation, operation.expr2().get())) :
         evalExpression(symbolTable, operation.expr());
   }

   protected static boolean areMatchingMethodHeaders(List<MethodParam> methodParams, List<Type> otherMethodParams) {
      List<Class<? extends Type>> methodParamsTypes = methodParams.stream()
         .map(param -> param.type().getClass())
         .collect(Collectors.toList());
      List<Class<? extends Type>> otherMethodParamsType = otherMethodParams.stream()
         .map(Type::getClass)
         .collect(Collectors.toList());
      return methodParamsTypes.equals(otherMethodParamsType);
   }

}
