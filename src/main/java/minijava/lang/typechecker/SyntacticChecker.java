package minijava.lang.typechecker;

import minijava.lang.parser.AST;
import minijava.lang.parser.AST.Scope;
import minijava.lang.parser.AST.ClassDecl;
import minijava.lang.parser.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyntacticChecker {

    public static final Logger LOG = Logger.getLogger(SyntacticChecker.class.getName());

    public static List<String> recursiveSuperClass(List<String> classesChain, SymbolTable<?> symbolTable, Scope scope) {
        if (! (scope instanceof ClassDecl)) {
            LOG.warning(() -> "RecursiveSuperClass function was passed non-ClassDecl type: " + scope.getClass());
        }

        ClassDecl classDecl = (ClassDecl) scope;

        classesChain.add(classDecl.className().id());

        if (classesChain.stream().distinct().toList().size() < classesChain.size()) {
            return classesChain;
        }

        if (classDecl.superClass().isEmpty()) {
            return classesChain;
        }

        String superClass = classDecl.superClass().get().id();

        Optional<SymbolTable<?>> superClassSymbolTable = symbolTable.findChildrenTable(superClass)
                .stream().findFirst();

        if (superClassSymbolTable.isEmpty()) {
            throw new IllegalStateException(superClass + "was not found. Reference before declaration.");
        }

        return recursiveSuperClass(classesChain, symbolTable, superClassSymbolTable.get().scope());
    }

    public static void circularDependencyChecker(SymbolTable<?> symbolTable) {
        List<Scope> classDecls = symbolTable.getRoot()
                .childTableStream(ClassDecl.class)
                .map(SymbolTable::scope)
                .filter(scope -> scope instanceof ClassDecl)
                .toList();
        List<List<String>> classesChain = classDecls.parallelStream()
                .map(classDecl -> recursiveSuperClass(new ArrayList<>(), symbolTable.getRoot(), classDecl))
                .toList();
        classesChain.forEach((chain) -> {
            if (chain.stream().distinct().toList().size() < chain.size()) {
                throw new IllegalStateException("Circular dependency for class: " + chain.get(0));
            }
        });
    }


}
