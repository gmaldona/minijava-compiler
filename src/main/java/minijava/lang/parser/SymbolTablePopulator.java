package minijava.lang.parser;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import minijava.lang.parser.AST.Program;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.AST.ClassType;
import minijava.lang.parser.AST.MethodDecl;
import minijava.lang.parser.SymbolTable.SymbolTableEntry;

public class SymbolTablePopulator {

   public static void visitAndPopulate(SymbolTable<?> symbolTable) {
       switch(symbolTable.scope()) {
         case Program           program  -> populateProgramTable(symbolTable, program);
         case ClassDecl       classDecl  -> populateClassDeclTable(symbolTable, classDecl);
         case MethodDecl      methodDecl ->  populateMethodDeclTable(symbolTable, methodDecl);
         default -> throw new IllegalStateException("Unexpected value: " + symbolTable.scope());
      }
   }

   private static void populateProgramTable(SymbolTable<?> symbolTable, Program program) {
      List<SymbolTableEntry> symbolTableEntries = program.classDecls().stream()
         .map((classDecl) -> new SymbolTableEntry(classDecl.className(), new ClassType(classDecl.className()), classDecl))
         .collect(Collectors.toList());
      symbolTableEntries.add(new SymbolTableEntry(program.mainClass().className(), new ClassType(program.mainClass().className()), program.mainClass()));
      symbolTable.AddTableEntries(symbolTableEntries);

      program.classDecls().stream()
         .map(symbolTable::findChild)
         .filter(Objects::nonNull)
         .forEach(SymbolTablePopulator::visitAndPopulate);
   }

   private static void populateClassDeclTable(SymbolTable<?> symbolTable, ClassDecl classDecl) {
      List<SymbolTableEntry> varTableEntries = classDecl.varDecls().stream()
         .map((varDecl) -> new SymbolTableEntry(varDecl.varName(), varDecl.varType(), varDecl))
         .collect(Collectors.toList());
      List<SymbolTableEntry> methodTableEntries = classDecl.methodDecls().stream()
         .map((methodDecl) -> new SymbolTableEntry(methodDecl.methodName(), methodDecl.methodType(), methodDecl))
         .collect(Collectors.toList());
      symbolTable.AddTableEntries(varTableEntries, methodTableEntries);

      classDecl.methodDecls().stream()
         .map(symbolTable::findChild)
         .filter(Objects::nonNull)
         .forEach(SymbolTablePopulator::visitAndPopulate);
   }

   private static void populateMethodDeclTable(SymbolTable<?> symbolTable, MethodDecl methodDecl) {
      List<SymbolTableEntry> varTableEntries = methodDecl.varDecls().stream()
         .map((varDecl) -> new SymbolTableEntry(varDecl.varName(), varDecl.varType(), varDecl))
         .collect(Collectors.toList());
      List<SymbolTableEntry> methodParamsTableEntries = methodDecl.methodParams().stream()
         .map((methodParam) -> new SymbolTableEntry(methodParam.name(), methodParam.type(), methodParam))
         .collect(Collectors.toList());
      symbolTable.AddTableEntries(varTableEntries, methodParamsTableEntries);
   }
}
