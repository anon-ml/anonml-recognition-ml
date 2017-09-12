package ml.anon.recognition.machinelearning;

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
import org.springframework.beans.factory.annotation.Autowired;

public class AnnotationTest {


  @Autowired
  private AnnotationService annotationService;

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
