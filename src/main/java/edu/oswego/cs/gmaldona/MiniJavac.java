package edu.oswego.cs.gmaldona;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import antlr4.MiniJavaParser.ProgramContext;
import antlr4.MiniJavaVisitor;
import minijava.lang.MiniJava;
import minijava.lang.parser.AST.ASTNode;
import minijava.lang.parser.MiniJavaVisitorImpl;
import minijava.lang.parser.Parser;
import minijava.lang.parser.SymbolTable;
import minijava.lang.parser.SymbolTableFactory;
import minijava.lang.typechecker.SyntacticChecker;
import minijava.lang.typechecker.TypeChecker;

public class MiniJavac implements MiniJava {

   enum Flags {
      PARALLELIZED,
      DEBUG
   }

   private static final Logger LOG = Logger.getLogger(MiniJavac.class.getName());

   private static MiniJavac compiler;

   private List<MiniJavac.Flags> flags;

   private MiniJavac() {
      this.flags = new ArrayList<>();
   }

   protected MiniJavac setFlags(MiniJavac.Flags flag0, MiniJavac.Flags ... flagsN) {
      List<MiniJavac.Flags> flags = new ArrayList<>();
      flags.add(flag0);
      flags.addAll(Arrays.asList(flagsN));
      return setFlags(flags);
   }

   protected MiniJavac setFlags(List<MiniJavac.Flags> flags) {
      for (MiniJavac.Flags flag : flags) {
         if (! this.flags.contains(flag)) {
            this.flags.add(flag);
         }
      }
      return this;
   }

   protected List<MiniJavac.Flags> getFlags() {
      return flags;
   }

   protected static List<Flags> getFlagsFromArgs(List<String> args) {
      List<String> definedFlags = Arrays.stream(Flags.values())
         .map(Flags::name)
         .map(String::toLowerCase)
         .toList();
      return args.stream()
         .filter(definedFlags::contains)
         .map(arg -> Flags.valueOf(arg.toUpperCase()))
         .toList();
   }

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
         SyntacticChecker.circularDependencyChecker(symbolTable);

      } catch (IOException e) {
         LOG.warning(() -> "Could not load input stream.");
         e.printStackTrace();
      } catch (Exception e) {
         LOG.log(Level.WARNING, "[{0}] Could not compile. {1}", new Object[]{inputStream.toString(), e.getMessage()});
         e.printStackTrace();
      } finally {
         try {
            inputStream.close();
         } catch (IOException e) {
            LOG.warning(() -> e.getCause() + ": Could not close input stream.");
         }
      }
   }

   protected void compile(String input) {
      ProgramContext         parseTree = Parser.parse(input);
      MiniJavaVisitor<ASTNode> visitor = new MiniJavaVisitorImpl();
      ASTNode                      ast = visitor.visit(parseTree);

      SymbolTable<?> symbolTable = new SymbolTableFactory(ast)
         .newTable()
         .populate()
         .build();

      TypeChecker.visitAndCheck(symbolTable, ast);
      SyntacticChecker.circularDependencyChecker(symbolTable);
   }

   protected void compile(List<Path> paths) {
      Stream<InputStream> inputStreams = paths.stream()
         .filter(Files::exists)
         .map(path -> {
            try {
               return Files.newInputStream(path);
            } catch (IOException ignored) {
               return null;
            }
         })
         .filter(Objects::nonNull);

      compile(inputStreams);
   }

   protected void compile(Stream<InputStream> inputStreams) {
      inputStreams
         .forEach(MiniJavac::compile);
   }

   protected void parallelizedCompile(Stream<InputStream> inputStreamStream) throws InterruptedException {
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
      List<MiniJavac.Flags> flags = getFlagsFromArgs(
         Args.stream()
            .filter(arg -> arg.startsWith("--") || arg.startsWith("-"))
            .map(arg -> arg.replace("-", ""))
            .map(String::toLowerCase)
            .toList()
      );

      List<Path> filePaths = Args.stream()
         .filter(arg -> arg.endsWith(MiniJava.JavaExt) || arg.endsWith(MiniJava.MiniJavaExt))
            .map(Paths::get)
         .filter(Files::exists)
         .toList();
      List<InputStream> inputStreams = new ArrayList<>();

      for (Path filePath : filePaths) {
         LOG.info(() -> "Found path: " + filePath);
         InputStream inputStream = Files.newInputStream(filePath);
         inputStreams.add(inputStream);
      }

      if (flags.contains(Flags.PARALLELIZED)) {
         MiniJavac.getInstance()
            .setFlags(flags)
            .parallelizedCompile(inputStreams.stream());
      } else {
         MiniJavac.getInstance()
            .setFlags(flags)
            .compile(inputStreams.stream());
      }
   }

   protected static MiniJavac getInstance() {
      if (Objects.isNull(compiler)) {
         compiler = new MiniJavac();
      }
      return compiler;
   }
}
