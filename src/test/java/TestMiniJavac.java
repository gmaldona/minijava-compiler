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
   @Ignore
   public void singleCoreMiniJavaExamples() {
      MiniJavac.compile(MiniJavaExamples());
   }

   @Test
   @Ignore
   public void parallelizedMiniJavaExamples() throws InterruptedException {
      MiniJavac. parallelizedCompile(MiniJavaExamples());
   }

}
