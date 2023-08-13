package minijava.lang.typechecker;

import java.util.Iterator;
import java.util.Optional;
import minijava.lang.parser.AST;
import minijava.lang.parser.AST.Bool;
import minijava.lang.parser.AST.ExprId;
import minijava.lang.parser.AST.ExprBoolean;
import minijava.lang.parser.AST.ExprNumber;
import minijava.lang.parser.AST.Int;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.ExprThis;
import minijava.lang.parser.SymbolTable;
import minijava.lang.parser.AST.ExprParenthesis;
import minijava.lang.parser.AST.IntArray;
import minijava.lang.parser.AST.NewIntArrayDecl;

public class ExpressionTypeChecker extends TypeChecker {

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
      SymbolTable<?> ancestorTable = symbolTable.findFirstTableWithEntry(exprId.className(), AST.Declaration.class);
      Optional<SymbolTable.SymbolTableEntry> tableEntry = ancestorTable.tableEntryStream()
         .filter((entry) -> entry.identifier().equals(exprId.className()))
         .findFirst();
      return (exprId.expr2().isPresent()) ?
         evalExpression2(symbolTable, exprId, exprId.expr2().get()) :
         tableEntry.get().type();
   }

   protected static Type evalExprThis(SymbolTable<?> symbolTable, ExprThis exprThis) {
      AST.ClassType thisType = new AST.ClassType(null);
      Iterator<SymbolTable<?>> ancestorSymbolTableIterator = symbolTable.ancestorSymbolTableIterator();
      while (ancestorSymbolTableIterator.hasNext()) {
         SymbolTable<?> ancestorSymbolTable = ancestorSymbolTableIterator.next();
         if (ancestorSymbolTable.scope() instanceof AST.ClassDecl) {
            thisType = new AST.ClassType(((AST.ClassDecl) ancestorSymbolTable.scope()).className());
            break;
         }
      }
      return (exprThis.expr2().isPresent()) ?
         evalExpression2(symbolTable, exprThis, exprThis.expr2().get()) :
         thisType;
   }

   protected static IntArray evalNewIntArrayDecl(SymbolTable<?> symbolTable, NewIntArrayDecl newIntArrayDecl) {
      return (newIntArrayDecl.expr2().isPresent()) ?
         (IntArray) areCompatibleTypes(IntArray.class, evalExpression2(symbolTable, newIntArrayDecl, newIntArrayDecl.expr2().get())) :
         new IntArray();
   }

   protected static Type evalExprParenthesis(SymbolTable<?> symbolTable, ExprParenthesis exprParenthesis) {
      return (exprParenthesis.expr2().isPresent()) ?
         areCompatibleTypes(
            evalExpression(symbolTable, exprParenthesis.expr()), evalExpression2(symbolTable, exprParenthesis, exprParenthesis.expr2().get())
         ) :
         evalExpression(symbolTable, exprParenthesis.expr());
   }



}