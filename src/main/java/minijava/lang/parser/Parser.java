package minijava.lang.parser;

import java.io.IOException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import antlr4.MiniJavaLexer;
import antlr4.MiniJavaParser;

public class Parser {

   public static MiniJavaParser.ProgramContext parse(String filename) throws IOException {
      CharStream        charStream      = CharStreams.fromFileName(filename);
      MiniJavaLexer     lexer           = new MiniJavaLexer(charStream);
      CommonTokenStream tokenStream     = new CommonTokenStream(lexer);
      MiniJavaParser    parser          = new MiniJavaParser(tokenStream);

      return parser.program();
   }

}
