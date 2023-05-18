import java.io.IOException;
import java.util.List;
import antlr4.MiniJavaParser.ProgramContext;
import antlr4.MiniJavaVisitor;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.MiniJavaVisitorImpl;
import minijava.lang.parser.Parser;
import minijava.lang.parser.SymbolTable;
import minijava.lang.parser.SymbolTableFactory;
import minijava.lang.parser.SymbolTablePopulator;
import minijava.lang.typechecker.TypeChecker;

public class Compiler {

   public static void main(String[] args) throws IOException {
      String filename = "src/main/minijava/testing.minijava";
      ProgramContext parseTree         = Parser.parse(filename);
      MiniJavaVisitor<ASTNode> visitor = new MiniJavaVisitorImpl();
      ASTNode ast                      = visitor.visit(parseTree);

      SymbolTable<?> symbolTable = new SymbolTableFactory(ast)
         .newTable()
         .populate()
         .build();
      TypeChecker.visitAndCheck(symbolTable, ast);
   }

}
