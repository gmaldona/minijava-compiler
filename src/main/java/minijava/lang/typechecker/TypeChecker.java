package minijava.lang.typechecker;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import minijava.lang.parser.AST;
import minijava.lang.parser.AST.ExprNumber;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.Expression2;
import minijava.lang.parser.AST.Int;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.AST.ExprBoolean;
import minijava.lang.parser.AST.IExpression;
import minijava.lang.parser.AST.Bool;
import minijava.lang.parser.AST.ExprClassMember;
import minijava.lang.parser.AST.ArrayLength;
import minijava.lang.parser.AST.MethodDecl;
import minijava.lang.parser.AST.ExprNot;
import minijava.lang.parser.AST.IntArray;
import minijava.lang.parser.AST.IfStatement;
import minijava.lang.parser.AST.ExprParenthesis;
import minijava.lang.parser.AST.NewIntArrayDecl;
import minijava.lang.parser.AST.ExprThis;
import minijava.lang.parser.AST.WhileLoop;
import minijava.lang.parser.AST.Program;
import minijava.lang.parser.AST.AssignStatement;
import minijava.lang.parser.AST.ExprArray;
import minijava.lang.parser.AST.VarDecl;
import minijava.lang.parser.AST.Operation;
import minijava.lang.parser.AST.MainClass;
import minijava.lang.parser.AST.NewClassDecl;
import minijava.lang.parser.AST.ArrayAssignStatement;
import minijava.lang.parser.AST.ExprId;
import minijava.lang.parser.AST.StatementBlock;
import minijava.lang.parser.AST.PrintStatement;
import minijava.lang.parser.AST.Identifier;
import minijava.lang.parser.SymbolTable;

public class TypeChecker {

   private static Logger LOG = Logger.getLogger(TypeChecker.class.getName());

   protected TypeChecker() {}

   /**
    * Visits a {@link ASTNode} in the {@link AST} and checks if the node is valid {@code minijava.lang.MiniJava} code.
    *
    * @param symbolTable The current {@link minijava.lang.parser.AST.Scope} of the {@link Program}.
    * @param ast {@link ASTNode} within the {@link AST}
    */

   public static void visitAndCheck(SymbolTable<?> symbolTable, ASTNode ast) {
      switch (ast) {
         case Program                  program -> programCheck(symbolTable, program);
         case ClassDecl              classDecl -> classCheck(symbolTable, classDecl);
         case MainClass              mainClass -> mainClassCheck(symbolTable, mainClass);
         case WhileLoop              whileLoop -> whileLoopBoolCheck(symbolTable, whileLoop);
         case MethodDecl            methodDecl -> methodCheck(symbolTable, methodDecl);
         case IfStatement          ifStatement -> ifStatementBoolCheck(symbolTable, ifStatement);
         case PrintStatement    printStatement -> printStatementCheck(symbolTable, printStatement);
         case StatementBlock    statementBlock -> statementBlockCheck(symbolTable, statementBlock);
         case AssignStatement  assignStatement -> assignStatementCheck(symbolTable, assignStatement);
         case ArrayAssignStatement arrayAssign -> arrayAssignStatementCheck(symbolTable, arrayAssign);
         case default -> throw new IllegalStateException("Unknown state for: " + ast);
      }
   }

   protected static Type evalExpression(SymbolTable<?> symbolTable, IExpression expression) {
      return switch (expression) {
         case ExprId                   exprId -> evalExprId(symbolTable, exprId);
         case ExprNot                 exprNot -> evalExprNot(symbolTable, exprNot);
         case ExprThis               exprThis -> evalExprThis(symbolTable, exprThis);
         case ExprNumber           exprNumber -> evalExprNumber(symbolTable, exprNumber);
         case ExprBoolean         exprBoolean -> evalExprBool(symbolTable, exprBoolean);
         case NewClassDecl       newClassDecl -> evalNewClassDecl(symbolTable, newClassDecl);
         case ExprParenthesis exprParenthesis -> evalExprParenthesis(symbolTable, exprParenthesis);
         case NewIntArrayDecl newIntArrayDecl -> evalNewIntArrayDecl(symbolTable, newIntArrayDecl);
         default -> throw new IllegalStateException("Unexpected value: " + expression);
      };
   }

   protected static Type evalExpression2(SymbolTable<?> symbolTable, IExpression expr, Expression2 expression2) {
      return switch (expression2) {
         case ExprArray         exprArray -> evalExprArray(symbolTable, expr, exprArray);
         case Operation         operation -> evalOperation(symbolTable, expr, operation);
         case ArrayLength     arrayLength -> evalArrayLength(symbolTable, expr, arrayLength);
         case ExprClassMember classMember -> evalExprClassMember(symbolTable, expr, classMember);
         default -> throw new IllegalStateException("Unexpected value: " + expression2);
      };
   }

   /**
    * Visits the {@link Program} node.
    *
    * (1) Checks if there are duplicated class names
    *
    * @param symbolTable The current {@link minijava.lang.parser.AST.Scope} of the {@link Program}.
    * @param program {@link Program} within the {@link AST}
    */
   private static void programCheck(SymbolTable<?> symbolTable, Program program) {
      List<String> uniqueClassDecl = program.classDecls().stream()
         .map(ClassDecl::className)
         .map(Identifier::id)
         .distinct()
         .toList();
      if (uniqueClassDecl.size() != program.classDecls().size()) {
         throw new IllegalStateException("Duplicate classes.");
      }
      Optional<String> hasDuplicatedMainClass = uniqueClassDecl.stream()
         .filter((className) -> className.equals(program.mainClass().className().id()))
         .findAny();
      if (hasDuplicatedMainClass.isPresent()) {
         throw new IllegalStateException("Duplicate classes.");
      }
      program.classDecls()
         .forEach((classDecl) -> {
            SymbolTable<?> classDeclTable = symbolTable.findChild(classDecl);
            visitAndCheck(classDeclTable, classDecl);
         });
      SymbolTable<?> mainClassTable = symbolTable.findChild(program.mainClass());
      visitAndCheck(mainClassTable, program.mainClass());
   }

   /**
    * Visits the {@link MainClass} node.
    */
   private static void mainClassCheck(SymbolTable<?> symbolTable, MainClass mainClass) {
      visitAndCheck(symbolTable, mainClass.statement());
   }

   private static void classCheck(SymbolTable<?> symbolTable, ClassDecl classDecl) {
      List<String> uniqueMethodDecl = classDecl.methodDecls().stream()
         .map(MethodDecl::methodName)
         .map(Identifier::id)
         .distinct()
         .toList();
      if (uniqueMethodDecl.size() != classDecl.methodDecls().size()) {
         throw new IllegalStateException("Duplicate methods.");
      }
      List<String> uniqueVarDecl = classDecl.varDecls().stream()
         .map(VarDecl::varName)
         .map(Identifier::id)
         .distinct()
         .toList();
      if (uniqueVarDecl.size() != classDecl.varDecls().size()) {
         throw new IllegalStateException("Duplicate variables.");
      }
      classDecl.methodDecls()
         .forEach((methodDecl) -> {
            Optional<SymbolTable<?>> methodDeclTable = symbolTable.childTableStream()
               .filter((childTable) -> childTable.name().equals(methodDecl.methodName().id()))
               .findFirst();
            visitAndCheck(methodDeclTable.get(), methodDecl);
         });
   }

   private static void methodCheck(SymbolTable<?> symbolTable, MethodDecl methodDecl) {
      List<String> uniqueVarDecl = methodDecl.varDecls().stream()
         .map(VarDecl::varName)
         .map(Identifier::id)
         .distinct()
         .toList();
      if (uniqueVarDecl.size() != methodDecl.varDecls().size()) {
         throw new IllegalStateException("Duplicate variables.");
      }
      areCompatibleTypes(methodDecl.methodType(), evalExpression(symbolTable, methodDecl.returnExpr()));
      List<String> parameters = methodDecl.methodParams().stream()
         .map(AST.MethodParam::name)
         .map(Identifier::id)
         .toList();
      if (! Collections.disjoint(uniqueVarDecl, parameters)) {
         throw new IllegalStateException("Duplicate variable.");
      }
      methodDecl.statements()
         .forEach((statement) -> visitAndCheck(symbolTable, statement));
   }

   private static void statementBlockCheck(SymbolTable<?> symbolTable, StatementBlock statementBlock) {
      statementBlock.statements()
         .forEach((statement) -> visitAndCheck(symbolTable, statement));
   }

   private static void arrayAssignStatementCheck(SymbolTable<?> symbolTable, ArrayAssignStatement arrayAssign) {
      if (! hasCompatibleTypes(Int.class, evalExpression(symbolTable, arrayAssign.indexExpr()))) {
         throw new IllegalStateException("Index is not type " + new Int());
      }
      if (! hasCompatibleTypes(Int.class, evalExpression(symbolTable, arrayAssign.expr()))) {
         LOG.warning(() -> "Found type: " + evalExpression(symbolTable, arrayAssign.expr()));
         throw new IllegalStateException("Array assignment is not type " + new Int());
      }
   }

   private static void printStatementCheck(SymbolTable<?> symbolTable, PrintStatement printStatement) {
      evalExpression(symbolTable, printStatement.expr());
   }

   private static void ifStatementBoolCheck(SymbolTable<?> symbolTable, IfStatement ifStatement) {
      LOG.warning(() -> evalExpression(symbolTable, ifStatement.expr()).toString());
      areCompatibleTypes(Bool.class, evalExpression(symbolTable, ifStatement.expr()));
      visitAndCheck(symbolTable, ifStatement.statement());
      visitAndCheck(symbolTable, ifStatement.elseStatement());
   }

   private static void whileLoopBoolCheck(SymbolTable<?> symbolTable, WhileLoop whileLoop) {
      areCompatibleTypes(Bool.class, evalExpression(symbolTable, whileLoop.expr()));
      visitAndCheck(symbolTable, whileLoop.statement());
   }

   private static void assignStatementCheck(SymbolTable<?> symbolTable, AssignStatement assignStatement) {

   }

   /**
    * Check if two given {@link Type}s are compatible
    * @return {@link Type} if they are equal
    * @throws {@link IllegalStateException} if the types are not equal
    */
   protected static Type areCompatibleTypes(Type type, Type otherType) {
       if (! hasCompatibleTypes(type, otherType)) {
          throw new IllegalStateException("Types are not compatible: " + type + ", " + otherType);
       }
       return type;
   }

   protected static Type areCompatibleTypes(Class<? extends Type> type, Type otherType) {
      if (! hasCompatibleTypes(type, otherType)) {
         throw new IllegalStateException("Types are not compatible: " + type.getName() + ", " + otherType);
      }
      return otherType;
   }

   private static boolean hasCompatibleTypes(Type type, Type otherType) {
      return type.getClass().equals(otherType.getClass());
   }

   private static boolean hasCompatibleTypes(Class<? extends Type> type, Type otherType) {
      return type.equals(otherType.getClass());
   }

   protected static Type areCompatibleTypes(Class<? extends Type> type, Type ... otherTypes) {
      for (Type otherType : otherTypes) {
         if (! type.equals(otherType.getClass())) {
            throw new IllegalStateException("Types are not compatible: " + type + ", " + otherType);
         }
      }
      return otherTypes[0];
   }

   private static Int evalExprNumber(SymbolTable<?> symbolTable, ExprNumber exprNumber) {
      return ExpressionTypeChecker.evalExprNumber(symbolTable, exprNumber);
   }

   private static Bool evalExprBool(SymbolTable<?> symbolTable, ExprBoolean exprBoolean) {
      return ExpressionTypeChecker.evalExprBool(symbolTable, exprBoolean);
   }

   private static Type evalExprId(SymbolTable<?> symbolTable, ExprId exprId) {
      return ExpressionTypeChecker.evalExprId(symbolTable, exprId);
   }

   private static Type evalExprThis(SymbolTable<?> symbolTable, ExprThis exprThis) {
      return ExpressionTypeChecker.evalExprThis(symbolTable, exprThis);
   }

   private static IntArray evalNewIntArrayDecl(SymbolTable<?> symbolTable, NewIntArrayDecl newIntArrayDecl) {
      return ExpressionTypeChecker.evalNewIntArrayDecl(symbolTable, newIntArrayDecl);
   }

   private static Type evalNewClassDecl(SymbolTable<?> symbolTable, NewClassDecl newClassDecl) {
      return ExpressionTypeChecker.evalNewClassDecl(symbolTable, newClassDecl);
   }

   private static Bool evalExprNot(SymbolTable<?> symbolTable, ExprNot exprNot) {
      return ExpressionTypeChecker.evalExprNot(symbolTable, exprNot);
   }

   private static Type evalExprParenthesis(SymbolTable<?> symbolTable, ExprParenthesis exprParenthesis) {
      return ExpressionTypeChecker.evalExprParenthesis(symbolTable, exprParenthesis);
   }

   private static Type evalExprClassMember(SymbolTable<?> symbolTable, IExpression expr, ExprClassMember exprClassMember) {
      return Expression2TypeChecker.evalExprClassMember(symbolTable, expr, exprClassMember);
   }

   private static Int evalArrayLength(SymbolTable<?> symbolTable, IExpression expr, ArrayLength arrayLength) {
      return Expression2TypeChecker.evalArrayLength(symbolTable, expr, arrayLength);
   }

   private static Type evalExprArray(SymbolTable<?> symbolTable, IExpression expr, ExprArray exprArray) {
       return Expression2TypeChecker.evalExprArray(symbolTable, expr, exprArray);
   }

   private static Type evalOperation(SymbolTable<?> symbolTable, IExpression expr, Operation operation) {
      return Expression2TypeChecker.evalExprOperation(symbolTable, expr, operation);
   }
}
