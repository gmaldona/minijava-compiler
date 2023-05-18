package minijava.lang.parser;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import minijava.lang.parser.AST.Scope;
import minijava.lang.parser.AST.Identifier;
import minijava.lang.parser.AST.Type;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.MethodDecl;

public class SymbolTable<S extends Scope> {

   private Scope scope;

   private SymbolTable<?> parentTable;

   private final List<SymbolTableEntry> tableEntries = new ArrayList<>();

   private final List<SymbolTable<?>> childrenTables = new ArrayList<>();

   public record SymbolTableEntry(Identifier identifier, Type type, ASTNode node) {
   }

   protected SymbolTable() {}

   protected SymbolTable(S scope) {
      this.scope = scope;
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
      if (childTable.scope != null) {
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

   public Stream<SymbolTable<?>> childStream() {
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

   public List<SymbolTable<?>> flattenTree() {
      return flattenTree(
         new ArrayList<>(Collections.singletonList(this)),
         this
      );
   }

   public SymbolTable<?> findFirstTableWithEntry(Identifier identifier, Class<? extends ASTNode> astNode) {
      Optional<SymbolTableEntry> tableEntry = tableEntryStream()
         .filter((entry) -> entry.identifier().equals(identifier))
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

   private List<SymbolTable<?>> flattenTree(List<SymbolTable<?>> symbolTables, SymbolTable<?> currentTable) {
      currentTable.childStream()
         .forEach(symbolTables::add);
      currentTable.childStream()
         .forEach((childTable) -> flattenTree(symbolTables, childTable));
      return symbolTables;
   }

   public static SymbolTable<?> empty() {
      return new SymbolTable<>();
   }

   public String getStringRepresentation() {
      return getStringRepresentation(this, new StringBuilder());
   }

   public SymbolTable<?> findChild(ASTNode node) {
      for (SymbolTable<?> child : children()) {
         if (child.scope().equals(node)) {
            return child;
         }
      }
      return null;
   }

   private String getStringRepresentation(SymbolTable<?> symbolTable, StringBuilder stringBuilder) {
      stringBuilder
         .append(String.format("{#table -> %s}: ", symbolTable.name()));
      if (symbolTable.childrenTables.size() == 0) {
         return stringBuilder.toString();
      }
      symbolTable.childrenTables.forEach((table) ->
         stringBuilder.append(String.format("{#table -> %s} ", table.name()))
      );
      stringBuilder.append(System.lineSeparator());
      for (SymbolTable<?> childSymbolTable : symbolTable.childrenTables) {
         getStringRepresentation(childSymbolTable, stringBuilder);
      }
      return stringBuilder.toString();
   }

}
