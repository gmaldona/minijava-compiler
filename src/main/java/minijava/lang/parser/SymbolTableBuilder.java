package minijava.lang.parser;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
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

   private static final Logger LOG = Logger.getLogger(SymbolTableBuilder.class.getName());

   /**
    * Builds the Symbol Table by starting at the given {@link ASTNode} and recursively visiting all child nodes.
    * The builder creates the Tree structure of the {@link AST} by giving pointers to each table.
    * @param AST {@link ASTNode}
    * @return The SymbolTable built for the given {@link ASTNode}
    */
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

   public static <S extends Scope> NewTable<S> newTable(S scope) {
      return new NewTable<>(scope);
   }

   private static class NewTable<S extends Scope> {

      private final SymbolTable<S> symbolTable;

      protected NewTable(S scope) {
         symbolTable = new SymbolTable<>(scope, scope.getClass());
      }

      protected NewTable<S> setParentTable(SymbolTable<?> parentTable) {
         symbolTable.setParentTable(parentTable);
         return this;
      }

      protected NewTable<S> setChildrenTables(List<SymbolTable<?>> childrenTables) {
         symbolTable.setChildrenTables(childrenTables);
         return this;
      }

      protected SymbolTable<S> build() {
         return symbolTable;
      }

   }


}
