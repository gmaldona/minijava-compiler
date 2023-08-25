package minijava.lang.parser;

import java.io.IOException;
import java.io.InputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import antlr4.MiniJavaLexer;
import antlr4.MiniJavaParser;

public class Parser {

   public static MiniJavaParser.ProgramContext parse(InputStream inputStream) throws IOException {
      CharStream        charStream      = CharStreams.fromStream(inputStream);
      MiniJavaLexer     lexer           = new MiniJavaLexer(charStream);
      CommonTokenStream tokenStream     = new CommonTokenStream(lexer);
      MiniJavaParser    parser          = new MiniJavaParser(tokenStream);

      return parser.program();
   }

   public static MiniJavaParser.ProgramContext parse(String input) {
      CharStream        charStream      = CharStreams.fromString(input);
      MiniJavaLexer     lexer           = new MiniJavaLexer(charStream);
      CommonTokenStream tokenStream     = new CommonTokenStream(lexer);
      MiniJavaParser    parser          = new MiniJavaParser(tokenStream);

      return parser.program();
   }

}
