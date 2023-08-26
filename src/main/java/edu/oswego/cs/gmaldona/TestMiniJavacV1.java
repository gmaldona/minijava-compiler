package edu.oswego.cs.gmaldona;

import antlr4.MiniJavaParser;
import antlr4.MiniJavaVisitor;
import minijava.lang.parser.*;
import minijava.lang.typechecker.TypeChecker;

/**
 * Test Functions for {@link MiniJavac}
 */
public class TestMiniJavacV1 {

    public static SymbolTable<?> extractSymbolTable(String input) {
        MiniJavaParser.ProgramContext parseTree = Parser.parse(input);
        MiniJavaVisitor<AST.ASTNode> visitor = new MiniJavaVisitorImpl();
        AST.ASTNode ast = visitor.visit(parseTree);

        return new SymbolTableFactory(ast)
           .newTable()
           .populate()
           .build();
    }

}
