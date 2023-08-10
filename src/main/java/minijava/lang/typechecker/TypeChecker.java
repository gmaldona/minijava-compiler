package minijava.lang.typechecker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import minijava.lang.parser.AST;
import minijava.lang.parser.AST.ExprNumber;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.Expression;
import minijava.lang.parser.AST.Expression2;
import minijava.lang.parser.AST.Int;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.AST.ExprBoolean;
import minijava.lang.parser.AST.IExpression;
import minijava.lang.parser.AST.Bool;
import minijava.lang.parser.AST.MethodParam;
import minijava.lang.parser.AST.ExprClassMember;
import minijava.lang.parser.AST.ArrayLength;
import minijava.lang.parser.AST.MethodDecl;
import minijava.lang.parser.AST.ExprNot;
import minijava.lang.parser.AST.IntArray;
import minijava.lang.parser.AST.IfStatement;
import minijava.lang.parser.AST.ExprParenthesis;
import minijava.lang.parser.AST.NewIntArrayDecl;
import minijava.lang.parser.AST.ClassType;
import minijava.lang.parser.AST.ExprThis;
import minijava.lang.parser.AST.ClassExpression;
import minijava.lang.parser.AST.WhileLoop;
import minijava.lang.parser.AST.Program;
import minijava.lang.parser.AST.AssignStatement;
import minijava.lang.parser.AST.ExprArray;
import minijava.lang.parser.AST.VarDecl;
import minijava.lang.parser.AST.Operation;
import minijava.lang.parser.AST.MainClass;
import minijava.lang.parser.AST.NewClassDecl;
import minijava.lang.parser.AST.LessThan;
import minijava.lang.parser.AST.ArrayAssignStatement;
import minijava.lang.parser.AST.ExprId;
import minijava.lang.parser.AST.Declaration;
import minijava.lang.parser.AST.StatementBlock;
import minijava.lang.parser.AST.PrintStatement;
import minijava.lang.parser.AST.Identifier;
import minijava.lang.parser.SymbolTable;
import minijava.lang.parser.SymbolTable.SymbolTableEntry;

public class TypeChecker {

   private static Logger LOG = Logger.getLogger(TypeChecker.class.getName());

   /**
    * Visits a {@link ASTNode} in the {@link AST} and checks if the node is valid {@code MiniJava} code.
    *
    * @param symbolTable The current {@link minijava.lang.parser.AST.Scope} of the {@link Program}.
    * @param ast {@link ASTNode} within the {@link AST}
    */
   public static void visitAndCheck(SymbolTable<?> symbolTable, ASTNode ast) {
      switch (ast) {
         case Program program                  -> programCheck(symbolTable, program);
         case ClassDecl classDecl              -> classCheck(symbolTable, classDecl);
         case MainClass mainClass              -> mainClassCheck(symbolTable, mainClass);
         case MethodDecl methodDecl            -> methodCheck(symbolTable, methodDecl);
         case IfStatement ifStatement          -> ifStatementBoolCheck(symbolTable, ifStatement);
         case PrintStatement printStatement    -> printStatementCheck(symbolTable, printStatement);
         case WhileLoop whileLoop              -> whileLoopBoolCheck(symbolTable, whileLoop);
         case AssignStatement assignStatement  -> assignStatementCheck(symbolTable, assignStatement);
         case StatementBlock statementBlock    -> statementBlockCheck(symbolTable, statementBlock);
         case ArrayAssignStatement arrayAssign -> arrayAssignStatementCheck(symbolTable, arrayAssign);
         case default -> throw new IllegalStateException("Unknown state for: " + ast);
      }
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
   private static Type areCompatibleTypes(Type type, Type otherType) {
       if (! hasCompatibleTypes(type, otherType)) {
          throw new IllegalStateException("Types are not compatible: " + type + ", " + otherType);
       }
       return type;
   }

   private static Type areCompatibleTypes(Class<? extends Type> type, Type otherType) {
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

   private static boolean areMatchingMethodHeaders(List<MethodParam> methodParams, List<Type> otherMethodParams) {
      List<Class<? extends Type>> methodParamsTypes = methodParams.stream()
         .map(param -> param.type().getClass())
         .collect(Collectors.toList());
      List<Class<? extends Type>> otherMethodParamsType = otherMethodParams.stream()
         .map(Type::getClass)
         .collect(Collectors.toList());
      return methodParamsTypes.equals(otherMethodParamsType);
   }

   private static Type areCompatibleTypes(Class<? extends Type> type, Type ... otherTypes) {
      for (Type otherType : otherTypes) {
         if (! type.equals(otherType.getClass())) {
            throw new IllegalStateException("Types are not compatible: " + type + ", " + otherType);
         }
      }
      return otherTypes[0];
   }

   private static Type evalExpression(SymbolTable<?> symbolTable, IExpression expression) {
      return switch (expression) {
         case ExprNumber           exprNumber -> evalExprNumber(symbolTable, exprNumber);
         case NewClassDecl       newClassDecl -> evalNewClassDecl(symbolTable, newClassDecl);
         case ExprId                   exprId -> evalExprId(symbolTable, exprId);
         case ExprThis               exprThis -> evalExprThis(symbolTable, exprThis);
         case ExprNot                 exprNot -> evalExprNot(symbolTable, exprNot);
         case ExprParenthesis exprParenthesis -> evalExprParenthesis(symbolTable, exprParenthesis);
         case ExprBoolean         exprBoolean -> evalExprBoolean(symbolTable, exprBoolean);
         default -> throw new IllegalStateException("Unexpected value: " + expression);
      };
   }

   private static Type evalExpression2(SymbolTable<?> symbolTable, IExpression expr, Expression2 expression2) {
      return switch (expression2) {
         case ArrayLength     arrayLength -> evalArrayLength(symbolTable, expr, arrayLength);
         case ExprClassMember classMember -> evalExprClassMember(symbolTable, expr, classMember);
         case LessThan           lessThan -> evalLessThan(symbolTable, expr, lessThan);
         case ExprArray         exprArray -> evalExprArray(symbolTable, expr, exprArray);
         case Operation         operation -> evalOperation(symbolTable, expr, operation);
         default -> throw new IllegalStateException("Unexpected value: " + expression2);
      };
   }

   private static Type evalNewClassDecl(SymbolTable<?> symbolTable, NewClassDecl newClassDecl) {
      return (newClassDecl.expr2().isPresent()) ?
         evalExpression2(symbolTable, newClassDecl, newClassDecl.expr2().get()) :
         new ClassType(newClassDecl.className());
   }

   private static Int evalExprNumber(SymbolTable<?> symbolTable, ExprNumber exprNumber) {
      return (exprNumber.expr2().isPresent()) ?
         (Int) areCompatibleTypes(Int.class, evalExpression2(symbolTable, exprNumber, exprNumber.expr2().get())) :
         new Int();
   }

   private static Bool evalExprBool(SymbolTable<?> symbolTable, ExprBoolean exprBoolean) {
      return (exprBoolean.expr2().isPresent()) ?
         (Bool) areCompatibleTypes(Bool.class, evalExpression2(symbolTable, exprBoolean, exprBoolean.expr2().get())) :
         new Bool();
   }

   private static Type evalOperation(SymbolTable<?> symbolTable, IExpression expr, Operation operation) {
      return (operation.expr2().isPresent()) ?
         areCompatibleTypes(evalExpression(symbolTable, operation.expr()), evalExpression2(symbolTable, operation, operation.expr2().get())) :
         evalExpression(symbolTable, operation.expr());
   }

   private static Bool evalLessThan(SymbolTable<?> symbolTable, IExpression expr, LessThan lessThan) {
      return (lessThan.expr2().isPresent()) ?
         (Bool) areCompatibleTypes(evalExpression(symbolTable, lessThan.expr()), evalExpression2(symbolTable, lessThan, lessThan.expr2().get())) :
         new Bool();
   }

   private static Bool evalExprBoolean(SymbolTable<?> symbolTable, ExprBoolean exprBoolean) {
      return (exprBoolean.expr2().isPresent()) ?
         (Bool) areCompatibleTypes(Bool.class, evalExpression2(symbolTable, exprBoolean, exprBoolean.expr2().get())) :
         new Bool();
   }

   private static Bool evalExprNot(SymbolTable<?> symbolTable, ExprNot exprNot) {
      return (exprNot.expr2().isPresent()) ?
         (Bool) areCompatibleTypes(evalExpression(symbolTable, exprNot.expr()), evalExpression2(symbolTable, exprNot, exprNot.expr2().get())) :
         (Bool) areCompatibleTypes(Bool.class, evalExpression(symbolTable, exprNot.expr()));
   }

   private static Type evalExprId(SymbolTable<?> symbolTable, ExprId exprId) {
      SymbolTable<?> ancestorTable = symbolTable.findFirstTableWithEntry(exprId.className(), Declaration.class);
      Optional<SymbolTableEntry> tableEntry = ancestorTable.tableEntryStream()
         .filter((entry) -> entry.identifier().equals(exprId.className()))
         .findFirst();
      return (exprId.expr2().isPresent()) ?
         evalExpression2(symbolTable, exprId, exprId.expr2().get()) :
         tableEntry.get().type();
   }

   private static Type evalExprThis(SymbolTable<?> symbolTable, ExprThis exprThis) {
      ClassType thisType = new ClassType(null);
      Iterator<SymbolTable<?>> ancestorSymbolTableIterator = symbolTable.ancestorSymbolTableIterator();
      while (ancestorSymbolTableIterator.hasNext()) {
         SymbolTable<?> ancestorSymbolTable = ancestorSymbolTableIterator.next();
         if (ancestorSymbolTable.scope() instanceof ClassDecl) {
            thisType = new ClassType(((ClassDecl) ancestorSymbolTable.scope()).className());
            break;
         }
      }
      return (exprThis.expr2().isPresent()) ?
         evalExpression2(symbolTable, exprThis, exprThis.expr2().get()) :
         thisType;
   }

   private static Int evalArrayLength(SymbolTable<?> symbolTable, IExpression expr, ArrayLength arrayLength) {
      return (arrayLength.expr2().isPresent()) ?
         (Int) areCompatibleTypes(Int.class, evalExpression2(symbolTable, arrayLength, arrayLength.expr2().get())) :
         new Int();
   }

   private static Type evalExprParenthesis(SymbolTable<?> symbolTable, ExprParenthesis exprParenthesis) {
      return (exprParenthesis.expr2().isPresent()) ?
         areCompatibleTypes(evalExpression(symbolTable, exprParenthesis.expr()), evalExpression2(symbolTable, exprParenthesis, exprParenthesis.expr2().get())) :
         evalExpression(symbolTable, exprParenthesis.expr());
   }

   private static Type evalExprClassMember(SymbolTable<?> symbolTable, IExpression expr, ExprClassMember exprClassMember) {
      Type type = areCompatibleTypes(ClassType.class, evalExpression(symbolTable, expr));
      if (! (type instanceof ClassType exprId)) {
         throw new IllegalStateException(type.getClass() + "was not type " + ClassType.class);
      }
//      SymbolTable<?> classTable = symbolTable.findFirstTableWithEntry()

















      SymbolTable<?>         classTable = symbolTable.findFirstTableWithEntry(exprId.identifier(), VarDecl.class).parentTable();
      List<SymbolTable<?>> memberTables = classTable.findChildrenTable(exprClassMember.id().id());
      if (memberTables.size() == 0) {
         throw new IllegalStateException("Class member " + exprClassMember.id() + ", does not exist for class " + exprId.identifier());
      }
      List<Type> exprClassMemberParams = exprClassMember.memberParams().stream()
         .map((param) -> evalExpression(symbolTable, param))
         .collect(Collectors.toList());
      Optional<SymbolTable<?>> classMemberSymbolTable = memberTables.stream()
         .filter((table) -> areMatchingMethodHeaders(((MethodDecl) table.scope()).methodParams(), exprClassMemberParams))
         .findFirst();
      if (classMemberSymbolTable.isEmpty()) {
         throw new IllegalStateException("Class Member with parameters " + exprClassMemberParams + " does not exist.");
      }
      return (exprClassMember.expr2().isPresent()) ?
         evalExpression2(symbolTable, exprClassMember, exprClassMember.expr2().get()) :
         ((MethodDecl) classMemberSymbolTable.get().scope()).methodType();
   }

   private static IntArray evalNewIntArrayDecl(SymbolTable<?> symbolTable, NewIntArrayDecl newIntArrayDecl) {
      return (newIntArrayDecl.expr2().isPresent()) ?
         (IntArray) areCompatibleTypes(IntArray.class, evalExpression2(symbolTable, newIntArrayDecl, newIntArrayDecl.expr2().get())) :
         new IntArray();
   }

   private static Type evalExprArray(SymbolTable<?> symbolTable, IExpression expr, ExprArray exprArray) {
      if (exprArray.expr2().isPresent()) {
         areCompatibleTypes(Int.class, evalExpression(symbolTable, exprArray.expr()));
         return evalExpression2(symbolTable, exprArray, exprArray.expr2().get());
      } else {
         areCompatibleTypes(Int.class, evalExpression(symbolTable, exprArray.expr()));
         return new IntArray();
      }
   }

}
