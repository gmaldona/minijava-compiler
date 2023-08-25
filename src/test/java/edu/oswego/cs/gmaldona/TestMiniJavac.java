package edu.oswego.cs.gmaldona;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMiniJavac {

   private static final Logger LOG = Logger.getLogger(TestMiniJavac.class.getName());

   private Stream<InputStream> MiniJavaExamples() {
      Path examplesDirectory = Paths.get("src/main/minijava/").toAbsolutePath();
      LOG.info(() -> "Examples Directory: " + examplesDirectory);
      List<InputStream> inputStreams = new ArrayList<>();
      try (DirectoryStream<Path> stream = Files.newDirectoryStream(examplesDirectory)) {
         for (Path path : stream) {
            if (path.toFile().isFile() && path.toFile().getName().endsWith(".java")) {
               LOG.info(() -> "Example MiniJava: " + path);
               inputStreams.add(Files.newInputStream(path));
            }
         }
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      return inputStreams.stream();
   }

   @Test
   @Ignore("Run manually")
   public void handPickJavaExamples() {
      enum JavaExamples {
         BinarySearch(Paths.get("src/main/minijava/BinarySearch.java")),
         Testing(Paths.get("src/main/minijava/testing.java"))
         ;
         private final Path path;
         JavaExamples(Path path) {
            this.path = path;
         }

         public Path toPath() {
            return path;
         }
      }

      MiniJavac.getInstance().
         compile(List.of(JavaExamples.BinarySearch.toPath()));

   }

   @Test
   public void setFlags() {
      MiniJavac compiler = MiniJavac.getInstance()
         .setFlags(MiniJavac.Flags.DEBUG,
                   MiniJavac.Flags.PARALLELIZED);
      List<MiniJavac.Flags> flags = compiler.getFlags();

      assertEquals(2, flags.size());

      MiniJavac.getInstance()
         .setFlags(MiniJavac.Flags.DEBUG,
                   MiniJavac.Flags.DEBUG,
                   MiniJavac.Flags.PARALLELIZED);

      assertEquals(2, flags.size());
   }

   @Test
   @Ignore
   public void singleCoreMiniJavaExamples() {
      MiniJavac.getInstance()
         .compile(MiniJavaExamples());
   }

   @Test
   @Ignore
   public void parallelizedMiniJavaExamples() throws InterruptedException {
      MiniJavac.getInstance()
         .setFlags(MiniJavac.Flags.PARALLELIZED)
         .compile(MiniJavaExamples());
   }

   @Test
   public void circularDependencies() {
      String program = """
            class Main {
               public static void main(String[] a) { System.out.println(new ClassA().start()); }
            }
            
            class A extends class B { }
            
            class B extends class A { } 
         """;

      MiniJavac.getInstance()
         .compile(program);
   }

}
