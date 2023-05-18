package minijava.lang.parser;

import minijava.lang.parser.AST.ASTNode;

public class SymbolTableFactory {

   private ASTNode ast;

   public SymbolTableFactory(ASTNode ast) {
      this.ast = ast;
   }

   public static class PopulatorStage {

      private final SymbolTable<?> symbolTable;

      public PopulatorStage(SymbolTable<?> symbolTable) {
         this.symbolTable = symbolTable;
      }

      public PopulatorStage populate() {
            SymbolTablePopulator.visitAndPopulate(symbolTable);
            return this;
         }

         public SymbolTable<?> build() {
            return symbolTable;
         }
      }

   public PopulatorStage newTable() {
      return new PopulatorStage(SymbolTableBuilder.visitAndBuild(ast));
   }

}
