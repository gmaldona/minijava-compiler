package minijava.lang.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import minijava.lang.parser.AST.Scope;
import minijava.lang.parser.AST.Identifier;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.MethodDecl;

public class SymbolTable<S extends Scope> {

   private final S scope;

   private final Class<? extends Scope> scopeClass;

   private SymbolTable<?> parentTable;

   private final List<SymbolTableEntry> tableEntries = new ArrayList<>();

   private final List<SymbolTable<?>> childrenTables = new ArrayList<>();

   public record SymbolTableEntry(Identifier identifier, Type type, ASTNode node) {
      public String toString() {
         return node.toString();
      }
   }

   protected SymbolTable() {
      this(null, null);
   }

   protected SymbolTable(S scope) {
      this(scope, null);
   }

   protected SymbolTable(Class<? extends Scope> scopeClass) {
      this(null, scopeClass);
   }

   protected SymbolTable(S scope, Class<? extends Scope> scopeClass) {
      this.scope = scope;
      this.scopeClass = scopeClass;
   }

   public SymbolTable<?> AddTableEntry(SymbolTableEntry tableEntry) {
      tableEntries.add(tableEntry);
      return this;
   }

   public SymbolTable<?> AddTableEntries(Iterable<SymbolTableEntry> tableEntryIterable) {
      Iterator<SymbolTableEntry> iterator = tableEntryIterable.iterator();
      while (iterator.hasNext()) {
         tableEntries.add(iterator.next());
      }
      return this;
   }

   @SafeVarargs
   public final SymbolTable<?> AddTableEntries(Iterable<SymbolTableEntry>... entriesList) {
      for (Iterable<SymbolTableEntry> entries : entriesList) {
         AddTableEntries(entries);
      }
      return this;
   }

   protected void addChildTable(final SymbolTable<?> childTable) {
      if (childTable.scopeClass != null) {
         childrenTables.add(childTable);
      }
   }

   protected void setParentTable(final SymbolTable<?> parentTable) {
      this.parentTable = parentTable;
   }

   public Iterator<SymbolTable<?>> ancestorSymbolTableIterator() {
      return recursiveAncestorSymbolTable(new ArrayList<>(), this).iterator();
   }

   private List<SymbolTable<?>> recursiveAncestorSymbolTable(List<SymbolTable<?>> ancestorSymbolTables, SymbolTable<?> symbolTable) {
      if (symbolTable.parentTable == null) {
         return ancestorSymbolTables;
      }
      ancestorSymbolTables.add(symbolTable.parentTable);
      return recursiveAncestorSymbolTable(ancestorSymbolTables, symbolTable.parentTable);
   }

   protected void setChildrenTables(final List<SymbolTable<?>> childrenTables) {
      childrenTables.forEach((table) -> {
         table.setParentTable(this);
         addChildTable(table);
      });
   }

   public List<SymbolTable<?>> children() {
      return childrenTables;
   }

   public Stream<SymbolTable<?>> childTableStream() {
      return childrenTables.stream();
   }

   public Stream<SymbolTableEntry> tableEntryStream() {
      return tableEntries.stream();
   }

   public Scope scope() {
      return scope;
   }

   public String name() {
      return switch (scope) {
         case ClassDecl classDecl   -> classDecl.className().toString();
         case MethodDecl methodDecl -> methodDecl.methodName().toString();
         case default               -> scope.getClass().getSimpleName();
      };
   }

   public SymbolTable<?> parentTable() {
      return parentTable;
   }

   public SymbolTable<?> getRoot() {
      if (parentTable == null) {
         return this;
      }
      return parentTable.getRoot();
   }

   public SymbolTable<?> findFirstTableWithEntry(Identifier identifier, Class<? extends ASTNode> astNode) {
      Optional<SymbolTableEntry> tableEntry = tableEntryStream()
         .filter((entry) -> entry.identifier().id().equals(identifier.id()))
         .filter((entry) -> astNode.isInstance(entry.node))
         .findFirst();
      if (tableEntry.isPresent()) {
         return this;
      }
      if (parentTable == null) {
         throw new IllegalStateException("Could not find identifier: " + identifier);
      }
      return parentTable.findFirstTableWithEntry(identifier, astNode);
   }

   /**
    * Finds all tables from Root with given {@link Scope}
    * @param scope Filtered {@link Scope}
    * @return {@link List} containing {@link SymbolTable} with given {@link Scope}
    */
   public List<SymbolTable<?>> findTablesWithScope(Class <? extends Scope> scope) {
      return getRoot().flattenTree()
         .stream()
         .filter(table -> table.scopeClass.equals(scope))
         .collect(Collectors.toList());
   }

   /**
    * Takes the Tree like structure of the SymbolTables and flattens the tree into a List
    * @return A List containing all descendant SymbolTables from Root
    */
   public List<SymbolTable<?>> flattenTree() {
      return flattenTree(
         new ArrayList<>(List.of(this)),
         this
      );
   }

   /**
    *
    * @param symbolTables List of child symbol tables
    * @param currentTable Recursive step for current symbol table
    * @return A List representation of SymbolTable Tree
    */
   private List<SymbolTable<?>> flattenTree(List<SymbolTable<?>> symbolTables, SymbolTable<?> currentTable) {
      currentTable.childTableStream()
         .forEach(symbolTables::add);
      currentTable.childTableStream()
         .forEach((childTable) -> flattenTree(symbolTables, childTable));
      return symbolTables;
   }

   /**
    * @return An Empty SymbolTable
    */
   public static SymbolTable<?> empty() {
      return new SymbolTable<>();
   }

   /**
    * @return An Empty SymbolTable with a {@link Scope}
    */
   public static SymbolTable<?> empty(Class<? extends Scope> scope) {
      return new SymbolTable<>(scope);
   }

   public SymbolTable<?> findChild(ASTNode node) {
      for (SymbolTable<?> child : children()) {
         if (child.scope().equals(node)) {
            return child;
         }
      }
      return null;
   }

   public List<SymbolTable<?>> findChildrenTable(String tableName) {
      return childTableStream()
         .filter((table) -> table.name().equals(tableName))
         .collect(Collectors.toList());
   }
}
