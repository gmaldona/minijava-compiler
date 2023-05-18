package minijava.lang.parser;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import minijava.lang.parser.AST.Scope;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.AST.Program;
import minijava.lang.parser.AST.MainClass;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.StatementScope;
import minijava.lang.parser.AST.StatementBlock;
import minijava.lang.parser.AST.MethodDecl;
import minijava.lang.parser.AST.Statement;

public class SymbolTableBuilder {

   public static SymbolTable<?> visitAndBuild(ASTNode AST) {
      return switch(AST) {
         case Program           program    -> programTable(program);
         case MainClass       mainClass    -> mainClassTable(mainClass);
         case ClassDecl       classDecl    -> classDeclTable(classDecl);
         case MethodDecl      methodDecl   -> methodDeclTable(methodDecl);
         case StatementScope  statement    -> statementScopeTable(statement);
         case StatementBlock statements    -> statementBlockTable(statements);
         case Statement nonStatementScopes -> SymbolTable.empty();
         default -> throw new IllegalStateException("Unexpected value: " + AST);
      };
   }

   private static SymbolTable<Program> programTable(Program program) {
      List<SymbolTable<?>> childrenTables = program.classDecls().stream()
         .map(SymbolTableBuilder::visitAndBuild)
         .collect(Collectors.toList());
      childrenTables.add(visitAndBuild(program.mainClass()));
      return SymbolTableBuilder.newTable(program)
         .setChildrenTables(childrenTables)
         .build();
   }

   private static SymbolTable<MainClass> mainClassTable(MainClass mainClass) {
      return SymbolTableBuilder.newTable(mainClass)
         .setChildrenTables(
            Collections.singletonList(visitAndBuild(mainClass.statement()))
         )
         .build();
   }

   private static SymbolTable<ClassDecl> classDeclTable(ClassDecl classDecl) {
      List<SymbolTable<?>> childrenTables = classDecl.methodDecls().stream()
         .map(SymbolTableBuilder::visitAndBuild)
         .collect(Collectors.toList());
      return SymbolTableBuilder.newTable(classDecl)
         .setChildrenTables(childrenTables)
         .build();
   }

   private static SymbolTable<MethodDecl> methodDeclTable(MethodDecl methodDecl) {
      List<SymbolTable<?>> childrenTables = methodDecl.statements().stream()
         .map(SymbolTableBuilder::visitAndBuild)
         .collect(Collectors.toList());
      return SymbolTableBuilder.newTable(methodDecl)
         .setChildrenTables(childrenTables)
         .build();
   }

   private static SymbolTable<StatementScope> statementScopeTable(StatementScope statement) {
      return SymbolTableBuilder.newTable(statement)
         .setChildrenTables(
            Collections.singletonList(visitAndBuild(statement.statement()))
         )
         .build();
   }

   private static SymbolTable<StatementBlock> statementBlockTable(StatementBlock statementBlock) {
      List<SymbolTable<?>> childrenTables = statementBlock.statements().stream()
         .map(SymbolTableBuilder::visitAndBuild)
         .collect(Collectors.toList());
      return SymbolTableBuilder.newTable(statementBlock)
         .setChildrenTables(childrenTables)
         .build();
   }

   public SymbolTableBuilder() {}

   public static <T extends Scope> NewTable<T> newTable(T scope) {
      return new NewTable<T>(scope);
   }

   private static class NewTable<T extends Scope> {

      private final SymbolTable<T> symbolTable;

      protected NewTable(T scope) {
         symbolTable = new SymbolTable<>(scope);
      }

      protected NewTable<T> setParentTable(SymbolTable<?> parentTable) {
         symbolTable.setParentTable(parentTable);
         return this;
      }

      protected NewTable<T> setChildrenTables(List<SymbolTable<?>> childrenTables) {
         symbolTable.setChildrenTables(childrenTables);
         return this;
      }

      protected SymbolTable<T> build() {
         return symbolTable;
      }

   }


}
