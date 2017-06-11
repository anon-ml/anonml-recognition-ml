import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.Test;

import ml.anon.recognition.machinelearning.service.AnnotationService;

public class AnnotationTest {


  private AnnotationService annotationService = new AnnotationService();

  @Test
  public void annotate() throws IOException {

    String tokenizedFile =
        "Gleich \ndarauf \nentwirft \ner \nseine \nSelbstdarstellung \n" + '"' + "\nHans \nPeter \n"
            + '"' + "\nin \nenger \nAuseinandersetzung \nmit \ndiesem \nBild \nJesu \n.";

    String result = annotationService.annotateTokenizedFile(tokenizedFile);
    System.out.println("Result: \n \n" + result);
    
    

    assertThat(result, is(containsString("Ecce B-PER")));
    assertThat(result, is(containsString("homo I-PER")));
    assertThat(result, is(containsString("Jesu B-PER")));

  }
}
