import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Ignore;
import org.junit.Test;

import ml.anon.recognition.machinelearning.service.AnnotationService;

public class AnnotationTest {


  private AnnotationService annotationService = new AnnotationService();

  @Test
   @Ignore
  public void annotate() throws IOException {

    // hint: there should not be spaces after each word!

    annotationService.initGermaNER();

    String tokenizedFile = "";
    String basePath = "./src/main/resources/GermaNER/TestFiles/";


    // String tokenizedFile = "Gleich\ndarauf\nentwirft\ner\nseine\nSelbstdarstellung\n" + '"'
    // + "\nHans\nPeter\n" + '"' + "\nin\nenger\nAuseinandersetzung\nmit\ndiesem\nBild\nJesu\n.";

    tokenizedFile = this.readFile(basePath + "10Sen.txt");
    int n = 100;
    
    long SenStart10 = System.currentTimeMillis();
    for(int i = 0; i < n; ++i){
      annotationService.annotateTokenizedFile(tokenizedFile);
    }
    long SenEnd10 = System.currentTimeMillis();
    
    
    tokenizedFile = this.readFile(basePath + "50Sen.txt");
    long SenStart50 = System.currentTimeMillis();
    for(int i = 0; i < n; ++i){
      annotationService.annotateTokenizedFile(tokenizedFile);
    }
    long SenEnd50 = System.currentTimeMillis();

    
    tokenizedFile = this.readFile(basePath + "100Sen.txt");
    long SenStart100 = System.currentTimeMillis();
    for(int i = 0; i < n; ++i){
      annotationService.annotateTokenizedFile(tokenizedFile);
    }
    long SenEnd100 = System.currentTimeMillis();

    
    tokenizedFile = this.readFile(basePath + "500Sen.txt");
    long SenStart500 = System.currentTimeMillis();
    for(int i = 0; i < n; ++i){
      annotationService.annotateTokenizedFile(tokenizedFile);
    }
    long SenEnd500 = System.currentTimeMillis();

    
    tokenizedFile = this.readFile(basePath + "1000Sen.txt");
    long SenStart1000 = System.currentTimeMillis();
    for(int i = 0; i < n; ++i){
      annotationService.annotateTokenizedFile(tokenizedFile);
    }
    long SenEnd1000 = System.currentTimeMillis();
    
    
    System.out.println("Time for 10 Sentences: " + (SenEnd10 - SenStart10)/n + "ms");
    System.out.println("Time for 50 Sentences: " + (SenEnd50 - SenStart50)/n + "ms");
    System.out.println("Time for 100 Sentences: " + (SenEnd100 - SenStart100)/n + "ms");
    System.out.println("Time for 500 Sentences: " + (SenEnd500 - SenStart500)/n + "ms");
    System.out.println("Time for 1000 Sentences: " + (SenEnd1000 - SenStart1000)/n + "ms");

    

    // System.out.println("Result: \n \n" + result);
    // assertThat(result, is(containsString("PER Hans Peter")));
    // assertThat(result, is(containsString("PER Jesu")));

  }

  public String readFile(String fileLocation) {

    BufferedReader br = null;
    String file = "";
    try {
      br = new BufferedReader(new InputStreamReader(new FileInputStream(fileLocation), "UTF-8"));
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null) {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }
      file = sb.toString();
      br.close();

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return file;
  }
}
