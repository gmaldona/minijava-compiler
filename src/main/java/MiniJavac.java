import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import antlr4.MiniJavaParser.ProgramContext;
import antlr4.MiniJavaVisitor;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.MiniJavaVisitorImpl;
import minijava.lang.parser.Parser;
import minijava.lang.parser.SymbolTable;
import minijava.lang.parser.SymbolTableFactory;
import minijava.lang.typechecker.TypeChecker;

public class MiniJavac implements MiniJava {

   enum Flags {
      PARALLELIZED
   }

   private static final Logger LOG = Logger.getLogger(MiniJavac.class.getName());

   private MiniJavac() {}

   protected static void compile(InputStream inputStream) {
      ProgramContext parseTree;
      try {
         parseTree = Parser.parse(inputStream);

         MiniJavaVisitor<ASTNode> visitor = new MiniJavaVisitorImpl();
         ASTNode ast                      = visitor.visit(parseTree);

         SymbolTable<?> symbolTable = new SymbolTableFactory(ast)
            .newTable()
            .populate()
            .build();

         TypeChecker.visitAndCheck(symbolTable, ast);
      } catch (IOException e) {
         LOG.warning(() -> "Could not load input stream.");
      } catch (Exception e) {
         LOG.log(Level.WARNING, "[{0}] Could not compile. {1}", new Object[]{inputStream.toString(), e.getMessage()});
      }
   }

   protected static void compile(Stream<InputStream> inputStreams) {
      inputStreams
         .forEach(MiniJavac::compile);
   }

   protected static void parallelizedCompile(Stream<InputStream> inputStreamStream) throws InterruptedException {
      List<Thread> compileThreads = inputStreamStream
         .map(inputStream -> new Thread(() -> compile(inputStream)))
         .toList();
      compileThreads
         .forEach(Thread::start);

      for (Thread thread : compileThreads) {
         thread.join();
      }
   }

   public static void main(String[] args) throws Exception {
      if (args.length == 0) {
         LOG.warning(() -> "No MiniJava Files to compile.");
         System.out.println("No MiniJava Files was provided.");
         return;
      }

      System.out.println(MiniJava.HelloMiniJava());

      List<String> Args = Arrays.asList(args);
      List<String> flags = Args.stream()
         .filter(arg -> arg.startsWith("--") || arg.startsWith("-"))
            .map(arg -> arg.replace("-", ""))
            .map(String::toLowerCase)
         .toList();

      List<Path> filePaths  = Args.stream()
         .filter(arg -> arg.endsWith(MiniJava.JavaExt) || arg.endsWith(MiniJava.MiniJavaExt))
            .map(Paths::get)
         .filter(Files::exists)
         .toList();
      List<InputStream> inputStreams = new ArrayList<>();

      for (Path filePath : filePaths) {
         try(InputStream inputStream = Files.newInputStream(filePath)) {
            inputStreams.add(inputStream);
         }
      }

      if (flags.contains(Flags.PARALLELIZED.name().toLowerCase())) {
         parallelizedCompile(inputStreams.stream());
      } else {
         compile(inputStreams.stream());
      }
   }
}
