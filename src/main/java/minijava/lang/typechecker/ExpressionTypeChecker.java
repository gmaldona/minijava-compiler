package minijava.lang.typechecker;

import java.util.Iterator;
import java.util.Optional;
import java.util.logging.Logger;
import minijava.lang.parser.AST;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.ClassType;
import minijava.lang.parser.AST.ExprNot;
import minijava.lang.parser.AST.Bool;
import minijava.lang.parser.AST.ExprId;
import minijava.lang.parser.AST.ExprBoolean;
import minijava.lang.parser.AST.ExprNumber;
import minijava.lang.parser.AST.Int;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.ExprThis;
import minijava.lang.parser.SymbolTable;
import minijava.lang.parser.AST.NewClassDecl;
import minijava.lang.parser.AST.ExprParenthesis;
import minijava.lang.parser.AST.IntArray;
import minijava.lang.parser.AST.NewIntArrayDecl;

public class ExpressionTypeChecker extends TypeChecker {

   private static final Logger LOG = Logger.getLogger(ExpressionTypeChecker.class.getName());

   private ExpressionTypeChecker() {}

   protected static Int evalExprNumber(SymbolTable<?> symbolTable, ExprNumber exprNumber) {
      return (exprNumber.expr2().isPresent()) ?
         (Int) areCompatibleTypes(Int.class, evalExpression2(symbolTable, exprNumber, exprNumber.expr2().get())) :
         new Int();
   }

   protected static Bool evalExprBool(SymbolTable<?> symbolTable, ExprBoolean exprBoolean) {
      return (exprBoolean.expr2().isPresent()) ?
         (Bool) areCompatibleTypes(Bool.class, evalExpression2(symbolTable, exprBoolean, exprBoolean.expr2().get())) :
         new Bool();
   }

   protected static Type evalExprId(SymbolTable<?> symbolTable, ExprId exprId) {
      SymbolTable<?> ancestorTable = symbolTable.findFirstTableWithEntry(exprId.identifier(), AST.Declaration.class);
      Optional<SymbolTable.SymbolTableEntry> tableEntry = ancestorTable.tableEntryStream()
         .filter((entry) -> entry.identifier().equals(exprId.identifier()))
         .findFirst();
      return (exprId.expr2().isPresent()) ?
         evalExpression2(symbolTable, exprId, exprId.expr2().get()) :
         tableEntry.get().type();
   }

   protected static Type evalExprThis(SymbolTable<?> symbolTable, ExprThis exprThis) {
      ClassType thisType = new ClassType(null);
      Iterator<SymbolTable<?>> ancestorSymbolTableIterator = symbolTable.ancestorSymbolTableIterator();
      while (ancestorSymbolTableIterator.hasNext()) {
         SymbolTable<?> ancestorSymbolTable = ancestorSymbolTableIterator.next();
         if (ancestorSymbolTable.scope() instanceof ClassDecl) {
            thisType = new ClassType(((ClassDecl) ancestorSymbolTable.scope()).className());
            break;
         }
      }
      var temp = (exprThis.expr2().isPresent()) ?
         evalExpression2(symbolTable, exprThis, exprThis.expr2().get()) :
         thisType;

      LOG.warning(() -> "EvalExprThis: " + temp);

      return temp;
   }

   protected static IntArray evalNewIntArrayDecl(SymbolTable<?> symbolTable, NewIntArrayDecl newIntArrayDecl) {
      return (newIntArrayDecl.expr2().isPresent()) ?
         (IntArray) areCompatibleTypes(IntArray.class, evalExpression2(symbolTable, newIntArrayDecl, newIntArrayDecl.expr2().get())) :
         new IntArray();
   }

   protected static Type evalNewClassDecl(SymbolTable<?> symbolTable, NewClassDecl newClassDecl) {
      return (newClassDecl.expr2().isPresent()) ?
         evalExpression2(symbolTable, newClassDecl, newClassDecl.expr2().get()) :
         new AST.ClassType(newClassDecl.identifier());
   }

   protected static Bool evalExprNot(SymbolTable<?> symbolTable, ExprNot exprNot) {
      return (exprNot.expr2().isPresent()) ?
         (Bool) areCompatibleTypes(evalExpression(symbolTable, exprNot.expr()), evalExpression2(symbolTable, exprNot, exprNot.expr2().get())) :
         (Bool) areCompatibleTypes(Bool.class, evalExpression(symbolTable, exprNot.expr()));
   }

   protected static Type evalExprParenthesis(SymbolTable<?> symbolTable, ExprParenthesis exprParenthesis) {
      return (exprParenthesis.expr2().isPresent()) ?
         areCompatibleTypes(
            evalExpression(symbolTable, exprParenthesis.expr()), evalExpression2(symbolTable, exprParenthesis, exprParenthesis.expr2().get())
         ) :
         evalExpression(symbolTable, exprParenthesis.expr());
   }
}