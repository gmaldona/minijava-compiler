package minijava.lang.parser;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestSymbolTable {

   @Test
   public void flattenTree() {
      SymbolTable<?> root = SymbolTable.empty();
      for (int i = 0; i < 3; i++) {
         SymbolTable<?> child = SymbolTable.empty();
         root.addChildTable(child);
         child.addChildTable(SymbolTable.empty());
      }
      List<SymbolTable<?>> flattenList = root.flattenTree();

      assertEquals(7, flattenList.size());
   }

   /**
    * Depends on {@link TestSymbolTable#flattenTree()} to pass as {@link SymbolTable#findTablesWithScope(Class)} calls {@link SymbolTable#flattenTree()}
    */
   @Test
   public void findTablesWithScope() {
      SymbolTable<?> root = SymbolTable.empty(AST.Program.class);
      SymbolTable<?> classDecl = new SymbolTable<>(AST.ClassDecl.class);
      root.addChildTable(classDecl);
      for (int i = 0; i < 3; i++) {
         classDecl.addChildTable(SymbolTable.empty(AST.MethodDecl.class));
      }

      assertEquals(5, root.flattenTree().size());

      List<SymbolTable<?>> onlyClassDecls = root.findTablesWithScope(AST.ClassDecl.class);
      assertEquals(1, onlyClassDecls.size());
   }

}
