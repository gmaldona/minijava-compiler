package minijava.lang.typechecker;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import minijava.lang.parser.AST;
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

   protected static Type evalExprArray(SymbolTable<?> symbolTable, IExpression expr, ExprArray exprArray) {
      if (exprArray.expr2().isPresent()) {
         areCompatibleTypes(Int.class, evalExpression(symbolTable, exprArray.expr()));
         return evalExpression2(symbolTable, exprArray, exprArray.expr2().get());
      }
      areCompatibleTypes(Int.class, evalExpression(symbolTable, exprArray.expr()));
      return new IntArray();
   }

   protected static Type evalExprClassMember(SymbolTable<?> symbolTable, IExpression expr, ExprClassMember exprClassMember) {
      if (exprClassMember.expr2().isPresent()) {
         return evalExpression2(symbolTable, exprClassMember, exprClassMember.expr2().get());
      }


      return null;
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
