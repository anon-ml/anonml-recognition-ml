package ml.anon.recognition.machinelearning.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import ml.anon.recognition.machinelearning.service.AnnotationService;
import ml.anon.recognition.machinelearning.service.IAnnotationService;

/**
 * 
 * @author Matthias
 *
 */
@Controller
public class MLController {

  @Autowired
  IAnnotationService annotationService;
  
  @Autowired
  AnnotationService annotationServiceImpl;

  @RequestMapping(value = "/ml/annotate", method = RequestMethod.POST)
  public ResponseEntity<?> annotate(@RequestParam("tokenizedFile") String tokenizedFile)
      throws IOException {
    
    String annotatedFile = annotationService.annotateTokenizedFile(tokenizedFile);
    return ResponseEntity.ok().body(annotatedFile);
  }

  /**
   * For test purpose only!
   * @return
   */
  @RequestMapping(value = "/testAnnotate", method = RequestMethod.GET)
  public ResponseEntity<?> init() {
    System.out.println("testAnnotate accessed!");
    
    //TODO: org.springframework.web.client.HttpClientErrorException: 400 null
    RestTemplate restTemplate = new RestTemplate();
    
    
    HttpHeaders headers = new HttpHeaders();
    headers.set(HttpHeaders.CONTENT_TYPE, "text/plain");
    String map = "Gleich\ndarauf\nentwirft\ner\nseine\nSelbstdarstellung\n" + '"'
         + "\nHans\nPeter\n" + '"' + "\nin\nenger\nAuseinandersetzung\nmit\ndiesem\nBild\nJesu\n.";
    
    HttpEntity<String> entity = new HttpEntity<>(map, headers);
    
    return restTemplate.exchange("http://127.0.0.1:9003/ml/annotate", HttpMethod.POST, entity, String.class);
    
  }
  


}
